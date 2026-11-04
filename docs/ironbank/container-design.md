# Flux container image: design record (phase 1)

Epic [MLE-27464](https://progresssoftware.atlassian.net/browse/MLE-27464). This document records the decisions behind
the Flux CLI container image in [`ironbank/`](../../ironbank/), resolves the open questions in
[`ironbank_flux_image_req.md`](ironbank_flux_image_req.md) §1.12, and lists what phase 2 (Iron Bank submission and
hardening) still has to do. It supersedes `Flux and IronBank.md`, whose Copilot-generated spec conflicts with Iron
Bank rules in four places (labels in the Dockerfile, `/app`, `chmod -R g=u`, `CMD ["--help"]`).

## 1. What the image is

A **CLI invocation image**: `docker run <image> import-files ...` runs one Flux command in a fresh JVM and exits with
Flux's exit code. No service, no listening port, no health check. The companion project flux-box (Spring API) is a
separate effort and does not use this image.

The repository layout is the Iron Bank (Repo1) project layout, so the `ironbank/` directory can be copied verbatim
into the Repo1 project in phase 2:

| Path | Role |
|---|---|
| `ironbank/Dockerfile` | Two-stage build; identical for local and Iron Bank builds |
| `ironbank/hardening_manifest.yaml` | Iron Bank manifest (tags, labels, base image args, resources, maintainers) |
| `ironbank/README.md` | Iron Bank README: usage, Kubernetes `Job`, resources, configuration contract |
| `ironbank/LICENSE` | Plain-text Apache-2.0 notice |
| `ironbank/testing_manifest.yaml` | Iron Bank functional tests (`version`, uid, read-only install, `help` exit 2) |
| `ironbank/renovate.json` | Renovate custom manager that bumps the Flux release in the manifest |
| `ironbank/scripts/flux-entrypoint`, `scripts/flux` | Entrypoint router and wrapper (see §3) |
| `ironbank/config/log4j2*.properties`, `config/lib-excludes.txt` | Logging configs; jars pruned at build time |
| `ironbank/ext-opt/` | Optional jars baked into `/opt/flux/ext` (empty by default) |
| `ironbank-local/` | Local validation only (compose override, smoke tests, options files); never goes to Repo1 |
| root `build.gradle` → `ironbank*` tasks | Staging, local build, manifest helper, smoke test |

## 2. Decisions (resolves §1.12 of the requirements doc)

| # | Decision | Choice | Why |
|---|---|---|---|
| 1 | Usage model | One command per container; `Job`/`CronJob` in Kubernetes; batch scripts via the entrypoint passthrough | Flux shares one SparkSession and static state per JVM, so several commands in one JVM are unsafe (FLX-022). Separate JVMs/containers are the isolation boundary. |
| 2 | Base image / Java | `redhat/openjdk/openjdk17-runtime-ubi9-slim:1.17` on registry1; locally `registry.access.redhat.com/ubi9/openjdk-17-runtime:1.21` | Flux is built and CI-tested on 17; the UBI9 JRE images are the Iron Bank-preferred Java bases; glibc 2.34 satisfies the MiniLM ONNX natives. Switching to 21 is a build-arg change. |
| 3 | Image contents | Full Flux distribution minus Windows binaries, `lib/native`, and non-linux/amd64 Netty natives; `ext/` **empty** | Keeps parity with the desktop CLI. Pruning is a glob list (`config/lib-excludes.txt`) with a justification per line, which phase 2 extends. |
| 4 | Entrypoint / user | `flux-entrypoint` (router) → `flux` (wrapper) → `/opt/flux/bin/flux`; uid 1000, gid 0; `CMD ["help"]` | Matches the Iron Bank MarkLogic Server image's uid; gid 0 is the OpenShift arbitrary-uid convention; `help` exits 2 like the desktop CLI (documented). |
| 5 | Configuration / secrets | `@options-file` mounted from a Secret at `/run/secrets/flux`; no env-var option support added | Flux reads no environment variables; options files keep secrets out of `ps`, `docker inspect` and `kubectl describe`. The undocumented `-Dmarklogic.client.*` properties remain available for non-secrets. |
| 6 | Mount points | `/workspace` (data), `/run/secrets/flux` (options, truststore), `/opt/flux/ext` (jars), `/tmp` (scratch) | One writable path (`/tmp`) is enough for `readOnlyRootFilesystem`. |
| 7 | TLS / auth / FIPS | Mount a PKCS12 truststore; document basic or certificate auth for FIPS hosts | FIPS is not an Iron Bank requirement yet ("future"); a FIPS-mode JVM refuses MD5 (digest auth) and JKS. |
| 8 | Embedders / JDBC drivers | Not baked. `/opt/flux/ext` is empty and is the single mount point for all extension jars | Same mechanism as a desktop install; keeps the image ~215 MB smaller and out of the embedder jars' scan surface. `-PironbankWithEmbedders=true` bakes them for local testing; a scanned variant would be a second Repo1 tag (phase 2 question). |
| 9 | JVM / Spark / logging defaults | `-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Dorg.xerial.snappy.tempdir=/tmp`; `SPARK_LOCAL_IP=127.0.0.1`; `DJL_CACHE_DIR=/tmp/djl.ai`; stdout logging with `FLUX_LOG_LEVEL`, `FLUX_LOG_FORMAT=json`, optional `FLUX_LOG_DIR` | Container-aware heap, fail-fast on OOM, no hostname lookups, one writable directory; Iron Bank requires logs on stdout. |
| 10 | Offline scope | Imports/exports/copy/reprocess and MiniLM embeddings work with no network beyond MarkLogic; Ollama, Azure OpenAI, S3/Azure storage, Semaphore and JDBC need their endpoints | Documented in the README "Networking" section. |
| 11 | Tags | Flux version (`2.2.0`) + `latest`; base image updates do not change the tag | Iron Bank convention: most specific tag first, `latest` last. |

## 3. Runtime contract

```
docker run IMG <flux args>          -> /usr/local/bin/flux-entrypoint -> flux -> /opt/flux/bin/flux -> exec java
docker run IMG sh /workspace/x.sh   -> sh (the script calls `flux ...` as often as it likes)
```

- **Entrypoint rule**: no args → `flux help`; first arg starts with `-` or `@` → Flux; first arg is an executable on
  PATH → run as-is; anything else → Flux (covers abbreviated commands such as `imp-f`). No static command list.
- **Wrapper**: derives `JAVA_HOME` if unset (the launcher's fallback uses `which`, absent in ubi9-minimal); builds
  `JAVA_OPTS = FLUX_DEFAULT_JVM_OPTS + -Duser.home=$HOME [+ JSON log config] + user JAVA_OPTS`; warns if tmp is not
  writable; `exec`s the launcher so java is PID 1. With `FLUX_LOG_DIR` it instead runs the launcher in the background
  through `tee`, forwards SIGTERM/SIGINT and exits with Flux's code.
- **No tini**: java is exec'd and spawns no children; Spark's shutdown hook handles SIGTERM.
- **Writable paths**: `/tmp` (rw **and exec**: Spark scratch plus snappy/zstd/lz4/Netty/ONNX native extraction).
  Docker's `--tmpfs` adds `noexec` by default, so the README mandates `--tmpfs /tmp:rw,exec,size=…` or an `emptyDir`.
  `/home/flux` only for `--s3-use-profile` with SSO. Nothing under `/opt/flux` is ever written.
- **Exit codes**: 0 success (including silent partial write failures unless `--abort-on-write-failure`), 1 failure,
  2 usage/`help`, 143/130 on signals.

## 4. Answers to the questions raised at the start

- **Embedding "downloads"**: there are none. The MiniLM jar contains the ONNX model; what has to be provided is the
  jar itself, which users mount into `/opt/flux/ext` like a JDBC driver (or bake with `-PironbankWithEmbedders=true`).
  The jar extracts native libraries into `/tmp` at runtime, hence the exec requirement above.
- **Running commands in parallel**: one command per container. Use Kubernetes `Job.spec.parallelism`, several
  `docker run`s, or a mounted batch script (`flux a & flux b & wait`), and bound each run with
  `--spark-master-url local[N]`, `--thread-count`, `--batch-size` and container CPU/memory limits.
- **Logs per invocation**: always on stdout (collected by `docker logs` / Fluent Bit in Big Bang). `FLUX_LOG_DIR`
  additionally writes `flux-<utc timestamp>-<pid>.log` per invocation into a mounted directory; `FLUX_LOG_FORMAT=json`
  produces ECS JSON lines for log pipelines.
- **Iron Bank MarkLogic Server image**: nothing special is needed. It runs as uid 1000 on ubi9-minimal, serves plain
  HTTP on 8000–8002 unless TLS is configured, and initialises from `MARKLOGIC_INIT`/`MARKLOGIC_ADMIN_*` (or `_FILE`
  secrets). When its app servers use TLS, mount the CA as a PKCS12 truststore. Both images using uid 1000 makes a
  shared `fsGroup` straightforward. Its own gap: it logs to files under `/var/opt/MarkLogic/Logs`, not stdout.
- **Hardening-friendly design**: everything a hardened cluster enforces (non-root, read-only rootfs, dropped
  capabilities, seccomp, one emptyDir) already holds for the local build, and the Repo1 rules (offline build with
  manifest resources, no labels in the Dockerfile, single package layer, root-owned binaries) are followed from day one,
  so phase 2 changes values, not structure.

## 5. Phase 2: Iron Bank submission and hardening

1. **Onboarding**: Repo1 account and onboarding form; request `dsop/progress/marklogicserver/flux` (manifest name
   `progress/marklogicserver/flux`, `image.type: opensource`, `licenses: Apache-2.0`, `product.name: MarkLogic Server`);
   coordinate with the Kube Ninjas, who maintain the MarkLogic Server Repo1 project.
2. **Resources**: GitHub publishes sha256 digests for every release asset (`gh release view <tag> --json assets`);
   `./gradlew ironbankManifestResources` prints the block for a locally staged zip. Iron Bank builds must use a
   released Flux (built from a released Spark connector; `develop` depends on `-SNAPSHOT` artifacts on the internal
   Artifactory). Add the release-day manifest MR to the Flux release checklist.
3. **Jar trimming** (largest lever on the ORA score): extend `config/lib-excludes.txt` from `jdeps`/runtime tracing.
   Candidates: all `netty-tcnative-*` (OpenSSL is disabled by `-Dio.netty.handler.ssl.noOpenSsl=true`), `leveldbjni`,
   unused Hadoop YARN/MapReduce, Tika and Jena modules. Keep the justification comments; they become VAT evidence.
4. **Pipeline**: lint fixes, local OpenSCAP run (Iron Bank tutorial), functional tests from `testing_manifest.yaml`,
   VAT justification templates (shaded-pom false positives, unused transitives, pending upstream fixes). Expect
   justification writing to dominate the effort: ~310 jars including Spark, Hadoop, Tika, POI, PDFBox, AWS and Azure SDKs.
5. **FIPS readiness**: run the smoke suite on a FIPS-enabled host (`update-crypto-policies --set FIPS`); document
   basic+TLS or certificate authentication (digest needs MD5) and PKCS12 stores; evaluate a `-fips-bc` tag using the
   NiFi pattern (BC-FIPS jars as manifest resources) if customers require it.
6. **Lifecycle**: Renovate for the base tag and Flux releases, a triage rota for the ~12-hourly rescans
   (ABC clocks: justify Critical 5 d / High 10 d / Medium 30 d / Low 60 d; remediate 15/35/180/360 d), and an
   internal support-policy page (which Flux versions get images, for how long).

## 6. Open questions for the team

- Repo1 group and name; is the new `marklogic-server-k8s-utility-container` project (created 2026-10-20, still the
  template) intended to host Flux or should Flux have its own repo?
- Is a single release zip as the only manifest resource acceptable (Maven Central is not proxied in the pipeline)? Any
  practical resource size limit (the zip is ~283 MB)?
- Will "Not Vulnerable (unused module)" justifications be accepted for Spark/Hadoop transitives, or is removal expected?
- Is a scanned image variant with MiniLM baked in wanted (second tag/repo)?
- FIPS timeline, and whether a `-fips` tag with FIPS-host functional tests is required.
- Arbitrary-uid support (OpenShift): see the smoke-test result in §7; if Hadoop's user lookup fails without a passwd
  entry, the README must mandate `runAsUser: 1000`.
- Upstream nice-to-haves: `help` exit code, a `--log-level` option, a `FLUX_CLASSPATH` hook in the start script for a
  second extension directory, a `.tar.gz` release asset (would remove the `unzip` install from the build stage).

## 7. Validation results (2026-11-05)

Environment: Windows 11, Rancher Desktop (dockerd 29.5), image built from the **Flux 2.1.2 release assets**
(`marklogic-flux-2.1.2.zip` and the three embedder jars from GitHub, sha256 verified against GitHub's published
digests) because `develop` depends on `-SNAPSHOT` artifacts that need the Progress network. Base image:
`registry.access.redhat.com/ubi9/openjdk-17-runtime:1.21` (RHEL 9.5, glibc 2.34, OpenJDK 17.0.21). MarkLogic:
`progressofficial/marklogic-db:12.1.0-ubi9-rootless-2.3.1` with the `test-app` deployed on port 8003.

`ironbank-local/smoke-test.sh`: **25 passed, 0 failed.** Every run used `--read-only --tmpfs /tmp:rw,exec,size=2g
--cap-drop ALL --security-opt no-new-privileges` as uid 1000.

| Area | Result |
|---|---|
| `version` | exit 0, prints Flux 2.1.2 / Java 17.0.21 / Spark 4.2.0 (Spark starts on loopback inside the tmpfs) |
| Exit codes | no args → 2; `help import-files` → 2 with options listed; `import-parq` abbreviation resolves; unknown option → 2; `sh -c 'exit 7'` → 7; wrong password → 1 with `Command failed` |
| Read-only install | `touch /opt/flux/x` and `/etc/x` fail; launcher and wrapper are `root:root -rwxr-xr-x`; `bin/` holds only `flux`; no jars in `ext/` |
| JVM / logging | `--memory 1g` → `MaxHeapSize` 805 MB (75 %); `FLUX_LOG_FORMAT=json` emits `{"@timestamp"…}` lines; `FLUX_LOG_DIR` writes one file per invocation; two `flux version` in parallel in one container succeed |
| Data commands | `import-files` (mixed-files fixture) → `export-files` wrote 6 files to the mounted volume → `copy` → `reprocess`, all exit 0 |
| Embedders | `import-files --splitter-text --embedder minilm` with the jar mounted at `/opt/flux/ext` succeeds (ONNX/DJL natives extracted under `/tmp`); the same command without the mount fails cleanly with exit 1 |
| Arbitrary uid (`--user 12345:0`) | **fails**: `Error: Invalid UID, could not determine effective user` (Hadoop needs a passwd entry). README now mandates `runAsUser: 1000` or a mounted passwd file. `HADOOP_USER_NAME` alone is not enough. |
| `noexec` `/tmp` | Netty warns and falls back to NIO; a parquet `--count` still succeeds, so the failure mode is library-dependent (snappy writes are the known hard failure). The README keeps the exec requirement. |

Image size: 1.58 GB locally = base 275 MB + `microdnf update` layer 369 MB (the public 1.21 base is several months
behind the RHEL 9 repositories, so the update re-ships the JRE) + Flux 307 MB. On an up-to-date Iron Bank base the
update layer is small; trimming `lib/` (phase 2) is the other lever.

Not validated in phase 1: TLS to MarkLogic (the test-app's SSL server on 8005 is configured dynamically by the test
suite), the registry1 base image (needs Iron Bank credentials), a Kubernetes `Job` run (Rancher Desktop's k3s is
available for this next), and FIPS mode.
