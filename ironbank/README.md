# MarkLogic Flux

[MarkLogic Flux](https://github.com/marklogic/flux) is the command-line tool for importing, exporting, copying and
reprocessing data in [MarkLogic Server](https://www.progress.com/marklogic). It runs Apache Spark in local mode inside
a single JVM; no Spark cluster is needed.

This image packages the Flux CLI as a **short-lived, non-root container**: every invocation runs one Flux command and
exits with Flux's exit code. There is no service, no listening port and no health check. Pair it with the
MarkLogic Server image (`ironbank/progress/marklogicserver/marklogicserver12`) for a fully Iron Bank–based stack.

- Upstream project: <https://github.com/marklogic/flux> (Apache-2.0)
- Documentation: <https://marklogic.github.io/flux/>
- Vendor: Progress Software Corporation

## Image contents

| Path | Contents | Writable at runtime |
|---|---|---|
| `/opt/flux/bin/flux` | Flux launcher (Gradle start script) | no |
| `/opt/flux/lib/` | Flux and its dependencies (Spark, Hadoop, MarkLogic client, ...) | no |
| `/opt/flux/ext/` | Extension jars: **empty** in this image; see [Extensions](#extensions-embedders-jdbc-drivers-custom-splitters) | no (mount point) |
| `/opt/flux/conf/` | `log4j2.properties` (text) and `log4j2-json.properties` (JSON) | no |
| `/usr/local/bin/flux` | Wrapper that applies the image defaults, then runs `/opt/flux/bin/flux` | no |
| `/usr/local/bin/flux-entrypoint` | Container entrypoint | no |
| `/home/flux` | Home of the `flux` user (uid 1000, gid 0) | only if needed, see below |
| `/workspace` | Working directory; mount your input/output data here | your mounts |
| `/tmp` | Spark scratch space and native-library extraction | **yes, required** |

The container runs as uid `1000`, gid `0`. Nothing in the image is writable by that user except `/home/flux`
and `/workspace`, and Flux never writes under `/opt/flux`.

Run the container **as uid 1000** (`runAsUser: 1000` in Kubernetes). Hadoop, which Spark uses for file access,
looks the effective uid up in `/etc/passwd` and fails with `Invalid UID, could not determine effective user` for an
arbitrary uid that has no entry (OpenShift-style random uids). If your platform assigns uids, mount a passwd file
that contains your uid over `/etc/passwd` (a ConfigMap with `subPath`).

## Running Flux

The arguments you pass to the container are the arguments you would pass to `./bin/flux`:

```sh
docker run --rm registry1.dso.mil/ironbank/progress/marklogicserver/flux:<tag> import-files \
  --path /workspace/data \
  --connection-string "user:password@marklogic-host:8000" \
  --collections example
```

- No arguments: prints Flux usage and exits with code **2** (the same as the desktop CLI).
- `version`: prints the Flux and Spark versions and exits 0. Note that this starts Spark, so even `version` needs a
  writable `/tmp`.
- Abbreviated command names work (`imp-f` for `import-files`).
- If the first argument is an executable (`sh`, `bash`, a mounted script), it is run as-is. Inside such a script,
  call `flux ...` to run as many Flux commands as you need, sequentially or in parallel:

  ```sh
  docker run --rm -v "$PWD/jobs:/workspace:ro" <image> sh /workspace/nightly.sh
  ```

### Exit codes

| Code | Meaning |
|---|---|
| 0 | Success. Note that Flux exits 0 even when some documents failed to write unless `--abort-on-write-failure` is used. |
| 1 | The command failed; the reason is printed to stderr (`--stacktrace` prints the full stack trace). |
| 2 | Usage error, unknown command or option, or `help`. |
| 143 / 130 | Terminated by SIGTERM / SIGINT. Batches already committed to MarkLogic stay committed. |

### The one writable path: `/tmp`

Spark spills shuffle data to `java.io.tmpdir`, and several libraries (snappy, zstd, lz4, Netty, the MiniLM
embedder's ONNX runtime) extract native `.so` files there. `/tmp` must therefore be **writable and mounted without
`noexec`**:

- Kubernetes: a disk-backed `emptyDir` mounted at `/tmp` with a `sizeLimit` (see the Job below). Avoid
  `medium: Memory` for large copies and exports: Spark spill then counts against the container's memory limit.
- Docker: `--tmpfs /tmp:rw,exec,size=2g` (Docker's `--tmpfs` default adds `noexec`, which breaks snappy with
  `failed to map segment from shared object`), or a bind mount.

For very large jobs, point Spark at a larger volume instead: `-e SPARK_LOCAL_DIRS=/scratch` plus
`-e JAVA_OPTS="-Djava.io.tmpdir=/scratch -Dorg.xerial.snappy.tempdir=/scratch"`.

`/home/flux` only needs to be writable when `--s3-use-profile` is used with AWS SSO (`~/.aws/sso/cache`).

### Hardened `docker run`

```sh
docker run --rm \
  --read-only --tmpfs /tmp:rw,exec,size=2g \
  --cap-drop ALL --security-opt no-new-privileges \
  --memory 4g --cpus 2 \
  -v "$PWD/secrets:/run/secrets/flux:ro" \
  -v "$PWD/data:/workspace/data:ro" \
  <image> import-files @/run/secrets/flux/options.txt --path /workspace/data
```

## Kubernetes

Run Flux as a `Job` (or a `CronJob` for scheduled loads), never as a `Deployment`. The manifest below uses the
restricted settings Big Bang's Kyverno policies expect: non-root, all capabilities dropped, read-only root
filesystem, `RuntimeDefault` seccomp, a single `emptyDir` at `/tmp`, credentials from a `Secret`.

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: flux-options
stringData:
  # One option (or option and value) per line; quote values that contain whitespace. LF line endings.
  options.txt: |
    --connection-string
    "flux-user:changeme@marklogic.ml.svc.cluster.local:8004"
    --spark-master-url
    local[2]
    --thread-count
    8
---
apiVersion: batch/v1
kind: Job
metadata:
  name: flux-import
spec:
  backoffLimit: 0            # imports are not idempotent unless URIs are deterministic (--uri-template)
  template:
    metadata:
      annotations:
        sidecar.istio.io/inject: "false"   # a classic Istio sidecar keeps Job pods from completing
    spec:
      restartPolicy: Never
      terminationGracePeriodSeconds: 120
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        runAsGroup: 0
        fsGroup: 1000
        seccompProfile:
          type: RuntimeDefault
      containers:
        - name: flux
          image: registry1.dso.mil/ironbank/progress/marklogicserver/flux:<tag>
          args:
            - import-files
            - "@/run/secrets/flux/options.txt"
            - --path
            - /workspace/data
            - --collections
            - example
          resources:
            requests:
              cpu: "2"
              memory: 4Gi
            limits:
              memory: 4Gi          # the JVM heap is sized to 75% of this limit
          securityContext:
            allowPrivilegeEscalation: false
            readOnlyRootFilesystem: true
            capabilities:
              drop: ["ALL"]
          volumeMounts:
            - name: tmp
              mountPath: /tmp
            - name: options
              mountPath: /run/secrets/flux
              readOnly: true
            - name: data
              mountPath: /workspace/data
              readOnly: true
      volumes:
        - name: tmp
          emptyDir:
            sizeLimit: 10Gi
        - name: options
          secret:
            secretName: flux-options
            defaultMode: 0440
        - name: data
          persistentVolumeClaim:
            claimName: flux-data
```

For a `CronJob`, wrap the same pod template in `spec.jobTemplate` and set `concurrencyPolicy: Forbid` so two runs
never overlap on the same data.

### Recommended resources

| | Minimum | Typical |
|---|---|---|
| CPU | 1 | 2–4 (`--spark-master-url local[N]` and `--thread-count` should match) |
| Memory | 2 GiB | 4–8 GiB; the heap defaults to 75 % of the limit (`-XX:MaxRAMPercentage=75`) |
| `/tmp` | 1 GiB | several times the size of the largest shuffle (`--repartition`, `copy`, exports) |
| Ports | none | Flux listens on nothing; Spark's driver port is bound to `127.0.0.1` |

Run one Flux command per container. Several containers (a Job with `parallelism`, or several `docker run`s) can
run at the same time against the same MarkLogic cluster; bound each one with `--spark-master-url local[N]`,
`--thread-count` and `--batch-size` so the cluster is not overwhelmed.

## Configuration

Flux is configured entirely through command-line options; it reads no environment variables of its own. The image
adds the following:

| Variable | Default | Purpose |
|---|---|---|
| `JAVA_OPTS`, `FLUX_OPTS` | unset | Extra JVM options (e.g. `-Xmx6g`, `-Djavax.net.ssl.trustStore=...`). Applied after the image defaults, so they win. |
| `FLUX_DEFAULT_JVM_OPTS` | `-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8 -Dorg.xerial.snappy.tempdir=/tmp` | Image defaults; set to an empty string to get the stock Flux behaviour. |
| `FLUX_LOG_LEVEL` | `INFO` | Level for the `com.marklogic.flux` and `com.marklogic.spark` loggers (`DEBUG` for troubleshooting). |
| `FLUX_LOG_FORMAT` | `text` | `json` writes one JSON object per line (Elastic Common Schema field names). |
| `FLUX_LOG_DIR` | unset | Also append the output of each invocation to `<dir>/flux-<utc timestamp>-<pid>.log`. The directory must be a writable mount. Output still goes to stdout. |
| `SPARK_LOCAL_IP` | `127.0.0.1` | Address Spark binds its driver to. Unset it (`-e SPARK_LOCAL_IP=`) only when connecting to an external Spark master. |
| `SPARK_LOCAL_DIRS` | unset (`/tmp`) | Spark scratch directory. |
| `DJL_CACHE_DIR` | `/tmp/djl.ai` | Cache for the MiniLM embedder's tokenizer natives. |
| `HADOOP_USER_NAME` | `flux` | User name Hadoop reports for file ownership. It does not remove the need for a passwd entry (see above). |

### Secrets and credentials

Put credentials in an **options file** mounted from a Secret and reference it with `@`, as in the Job above.
Anything passed as a command-line argument or in `JAVA_OPTS` is visible in `kubectl describe pod`, `docker inspect`
and `/proc`. Options files support every Flux option, one per line, with quoted values.

Password-type options (`--password`, `--jdbc-password`, `--keystore-password`, `--truststore-password`, the S3 and
Azure secret options) prompt interactively when given **without** a value. Always give them a value in a container,
or they fail for lack of a terminal.

### TLS to MarkLogic

MarkLogic app servers speak plain HTTP unless configured for TLS. When they are, mount a truststore (PKCS12
recommended; JKS is rejected on FIPS-enabled hosts) alongside the options file:

```
--ssl-protocol
TLSv1.2
--truststore-path
/run/secrets/flux/truststore.p12
--truststore-type
PKCS12
--truststore-password
changeme
```

If the server certificate chains to a CA already in the JVM's `cacerts`, use `--ssl-protocol default` instead.
Two-way TLS uses `--keystore-path` and `--keystore-password` the same way. On FIPS-enabled hosts prefer
`--auth-type basic` (over TLS) or certificate authentication; MarkLogic digest authentication requires MD5, which a
FIPS-mode JVM does not provide.

### Logging

All logs go to stdout (`conf/log4j2.properties`); the image writes no log files unless `FLUX_LOG_DIR` is set. To
change the configuration beyond `FLUX_LOG_LEVEL`/`FLUX_LOG_FORMAT`, mount your own file over
`/opt/flux/conf/log4j2.properties` (a ConfigMap with `subPath`) or set
`JAVA_OPTS=-Dlog4j2.configurationFile=/path/to/log4j2.properties`.

## Extensions: embedders, JDBC drivers, custom splitters

`/opt/flux/ext/` is empty in this image. Flux loads every jar in it, so mount a directory with whatever your job
needs:

- **Embedding models** for `--embedder`: `flux-embedding-model-minilm-<version>-all.jar` (runs fully offline; the
  model is inside the jar, ~200 MB), `flux-embedding-model-ollama-<version>-all.jar` and
  `flux-embedding-model-azure-open-ai-<version>-all.jar` (both call an external service). Download them from the
  [Flux release](https://github.com/marklogic/flux/releases) that matches the image tag.
- **JDBC drivers** for `import-jdbc` / `export-jdbc`.
- **Custom splitters or embedding-model integrations** you build yourself.

```sh
docker run --rm -v "$PWD/jars:/opt/flux/ext:ro" ... <image> import-files --embedder minilm ...
```

In Kubernetes, mount a volume (PVC, or an `emptyDir` populated by an init container) at `/opt/flux/ext`. Mounted
jars are not part of this image and are therefore not covered by its Iron Bank scan; bring them through your own
artifact approval process. Alternatively, build a derived image: `FROM <image>` then `COPY *.jar /opt/flux/ext/`.

## Networking

Flux only makes outbound connections: to MarkLogic (ports 8000–8002 by default, or your REST API app server port),
and, depending on the command, to JDBC databases, S3/Azure storage, an Ollama or Azure OpenAI endpoint, or a
Semaphore classifier. Imports and exports from local files and MarkLogic work with no other network access.

## Known limitations

- `help` exits with code 2; do not use it as a readiness check. Use `version`.
- Flux has no `--log-level` option; use `FLUX_LOG_LEVEL` or a mounted `log4j2.properties`.
- Running several Flux commands inside one JVM is unsupported; the entrypoint always starts a new JVM per `flux`
  call, which is why batch scripts are safe.
- Mounting a directory over `/opt/flux/ext` replaces the directory, so put every jar you need in that one directory.

## Building locally

The build needs a Flux distribution zip in this directory as `marklogic-flux.zip` (the Iron Bank pipeline downloads
it from `hardening_manifest.yaml`; the Flux repository's `./gradlew ironbankBuild` stages a locally built one, or a
downloaded release with `-PironbankFluxZip=path/to/marklogic-flux-<version>.zip`). Without registry1 credentials,
build against the public Red Hat image:

```sh
docker build \
  --build-arg BASE_REGISTRY=registry.access.redhat.com \
  --build-arg BASE_IMAGE=ubi9/openjdk-17-runtime \
  --build-arg BASE_TAG=1.21 \
  -t marklogic-flux:local .
```
