# MarkLogic Flux CLI - Iron Bank image.
#
# This Dockerfile follows the Iron Bank Dockerfile requirements
# (https://docs-ironbank.dso.mil/hardening/dockerfile-requirements/):
#   - the base image comes from registry1 via BASE_REGISTRY/BASE_IMAGE/BASE_TAG build args;
#   - the build makes no network calls: the Flux distribution is a hardening_manifest.yaml
#     resource that the pipeline places next to this file as marklogic-flux.zip;
#   - no ADD, no curl/wget, no labels (labels live in hardening_manifest.yaml), no HEALTHCHECK
#     (not required for a short-lived CLI), no EXPOSE (Flux opens no listening ports);
#   - the container runs as a fixed non-root user and never writes under /opt/flux.
#
# The same file builds locally against the public Red Hat image, e.g.
#   docker build --build-arg BASE_REGISTRY=registry.access.redhat.com \
#                --build-arg BASE_IMAGE=ubi9/openjdk-17-runtime --build-arg BASE_TAG=1.21 .
ARG BASE_REGISTRY=registry1.dso.mil
ARG BASE_IMAGE=ironbank/redhat/openjdk/openjdk17-runtime-ubi9-slim
ARG BASE_TAG=1.17

# ---------------------------------------------------------------------------------------------
# Stage 1: unpack the Flux distribution, drop what Linux never uses, fix ownership and modes.
# unzip is only installed in this stage; the runtime stage never contains it.
# ---------------------------------------------------------------------------------------------
FROM ${BASE_REGISTRY}/${BASE_IMAGE}:${BASE_TAG} AS unpack

USER 0
WORKDIR /build

COPY marklogic-flux.zip ./
COPY config/ config/
COPY scripts/ scripts/
# Optional extension jars baked into /opt/flux/ext (empty in the Iron Bank build; see README).
COPY ext-opt/ ext-opt/

RUN microdnf install -y --nodocs unzip \
    && microdnf clean all \
    && rm -rf /var/cache/dnf /var/cache/yum \
    && unzip -q marklogic-flux.zip \
    && mv marklogic-flux-*/ flux \
    # Windows-only launcher and Hadoop binaries; libhadoop.so is never loaded on Linux because
    # the launcher does not set java.library.path (Hadoop uses its pure-Java fallback).
    && rm -f flux/bin/flux.bat flux/bin/*.dll flux/bin/*.exe \
    && rm -rf flux/lib/native \
    # Platform-specific jars that cannot load on linux/amd64 (see config/lib-excludes.txt).
    && tr -d '\r' < config/lib-excludes.txt | while read -r pattern; do \
         case "$pattern" in ''|'#'*) ;; *) rm -f flux/lib/$pattern ;; esac; \
       done \
    && find ext-opt -name '*.jar' -exec cp {} flux/ext/ \; \
    && cp config/log4j2.properties config/log4j2-json.properties flux/conf/ \
    # The Gradle-generated launcher needs these tools; fail the build early if the base lacks one.
    && for tool in sh xargs sed tr uname ls readlink; do \
         command -v "$tool" >/dev/null 2>&1 || { echo "Base image is missing: $tool" >&2; exit 1; }; \
       done \
    && chown -R 0:0 flux scripts \
    && find flux -type d -exec chmod 0755 {} + \
    && find flux -type f -exec chmod 0644 {} + \
    && chmod 0755 flux/bin/flux scripts/flux scripts/flux-entrypoint

# ---------------------------------------------------------------------------------------------
# Stage 2: runtime image.
# ---------------------------------------------------------------------------------------------
FROM ${BASE_REGISTRY}/${BASE_IMAGE}:${BASE_TAG}

USER 0

# Apply the latest base package updates, then create the runtime user. ubi9-minimal has no
# shadow-utils, so the passwd entry is appended directly (the same pattern other Iron Bank
# minimal-based images use). The guard keeps a future base image from silently sharing uid 1000.
RUN microdnf update -y --nodocs \
    && microdnf clean all \
    && rm -rf /var/cache/dnf /var/cache/yum \
    && if grep -q '^[^:]*:[^:]*:1000:' /etc/passwd; then \
         echo "uid 1000 already exists in the base image" >&2; exit 1; \
       fi \
    && echo "flux:x:1000:0:MarkLogic Flux:/home/flux:/usr/sbin/nologin" >> /etc/passwd \
    && mkdir -p /home/flux /workspace \
    && chown 1000:0 /home/flux /workspace \
    && chmod 0770 /home/flux /workspace

COPY --from=unpack --chown=0:0 /build/flux /opt/flux
COPY --from=unpack --chown=0:0 /build/scripts/flux /build/scripts/flux-entrypoint /usr/local/bin/

# FLUX_DEFAULT_JVM_OPTS is prepended to JAVA_OPTS by /usr/local/bin/flux; user-supplied
# JAVA_OPTS / FLUX_OPTS come later on the java command line and therefore win.
#   -XX:MaxRAMPercentage=75   size the heap from the container memory limit, not the host
#   -XX:+ExitOnOutOfMemoryError  a Job should fail fast rather than limp on after an OOM
#   -Dorg.xerial.snappy.tempdir  snappy extracts its native library here (must be rw + exec)
# SPARK_LOCAL_IP keeps Spark's driver port on loopback so container hostnames need no DNS.
# DJL_CACHE_DIR keeps the MiniLM embedder's native tokenizer cache under /tmp, the only
# directory the image requires to be writable.
# HADOOP_USER_NAME is the user name Hadoop reports for file ownership. The container must still
# run as uid 1000 (which has a passwd entry): Hadoop refuses an unknown effective uid.
ENV FLUX_HOME=/opt/flux \
    HOME=/home/flux \
    LANG=C.UTF-8 \
    HADOOP_USER_NAME=flux \
    SPARK_LOCAL_IP=127.0.0.1 \
    DJL_CACHE_DIR=/tmp/djl.ai \
    FLUX_DEFAULT_JVM_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8 -Dorg.xerial.snappy.tempdir=/tmp"

WORKDIR /workspace
USER 1000

ENTRYPOINT ["/usr/local/bin/flux-entrypoint"]
# `flux help` prints usage and, like the desktop CLI, exits with code 2.
CMD ["help"]
