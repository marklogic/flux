# MarkLogic Flux container image — design document

| | |
|---|---|
| Epic | [MLE-27464 IronBank-Hardened MarkLogic Flux CLI for DoD Deployments](https://progresssoftware.atlassian.net/browse/MLE-27464) |
| Status | Phase 1 (container design, local build, validation) complete — **for architecture review**. Phase 2 (Iron Bank submission and hardening) not started. |
| Branch | `feature/ironbank-container` (from `develop`) |
| Artifacts | [`ironbank/`](../../ironbank/) (Repo1 project), [`ironbank-local/`](../../ironbank-local/) (validation harness), `ironbank*` Gradle tasks in the root `build.gradle` |
| Supersedes | `Flux and IronBank.md` (Copilot draft) and the open decisions in [`ironbank_flux_image_req.md`](ironbank_flux_image_req.md) §1.12 |
| Date | 2026-11-05 |

## Table of contents

1. [Purpose and audience](#1-purpose-and-audience)
2. [Goals and non-goals](#2-goals-and-non-goals)
3. [What constrains the design](#3-what-constrains-the-design)
4. [Architecture overview](#4-architecture-overview)
5. [Design decisions](#5-design-decisions)
6. [Security posture against Iron Bank and Big Bang requirements](#6-security-posture-against-iron-bank-and-big-bang-requirements)
7. [Validation](#7-validation)
8. [Risks and mitigations](#8-risks-and-mitigations)
9. [Phase 2: Iron Bank submission and hardening](#9-phase-2-iron-bank-submission-and-hardening)
10. [Open questions](#10-open-questions)
11. [Appendices](#11-appendices)

---

## 1. Purpose and audience

MarkLogic Server is available from Iron Bank (`registry1.dso.mil/ironbank/progress/marklogicserver/marklogicserver12`),
but Flux — the CLI customers use for ingestion, embeddings, export, copy and reprocessing — is not. DoD programmes
that may only deploy Iron Bank images therefore cannot run a complete MarkLogic stack. This document describes the
container image that closes that gap, records every design decision with the alternatives that were considered, and
states what has been validated and what is still open.

It is written for the architecture review that precedes Iron Bank onboarding (Architecture / Kube Ninjas / Flux
team / Security). The Iron Bank reviewers will see [`ironbank/README.md`](../../ironbank/README.md), which is the
user-facing subset of this document.

## 2. Goals and non-goals

**Goals**

- A container that behaves exactly like the desktop `flux` CLI: `docker run <image> <flux arguments>` runs one
  command and exits with Flux's exit code. "Functional parity" with the Flux zip is the yardstick.
- Hardened by construction, not by a later rewrite: the local build already satisfies the Iron Bank Dockerfile
  requirements and runs under the pod security settings that Big Bang's Kyverno policies enforce (non-root, read-only
  root filesystem, no capabilities, seccomp, one writable volume).
- A repository layout that **is** the Iron Bank (Repo1) project, so phase 2 is "fill in values, trim jars, write
  justifications", not "re-architect".
- Reproducible local validation (build, smoke test, Kubernetes Job) that runs with and without access to the Progress
  network or registry1.

**Non-goals**

- A long-running service, API or scheduler around Flux. flux-box (the Spring-based API) is a separate project.
- Running Flux against an external Spark cluster from the container (that path is `spark-submit`, documented
  separately in the Flux docs).
- Changing Flux itself. Phase 1 needs no Flux code change; a few optional upstream improvements are listed in §10.
- Iron Bank approval. Phase 2 does that; this document prepares it.

## 3. What constrains the design

### 3.1 How Flux behaves at runtime (verified in the code base)

| Fact | Consequence for the image | Source |
|---|---|---|
| The distribution is `bin/flux` (a Gradle-generated POSIX start script with the classpath hard-coded as `lib/*:ext/*:conf`), ~318 jars (~300 MB) in `lib/`, `conf/log4j2.properties`, an empty `ext/`, Windows-only `bin/*.dll`, `*.exe`, `flux.bat`, and `lib/native/libhadoop.so` which is never loaded on Linux | Ship the distribution as-is under `/opt/flux`; remove the Windows payload; the classpath cannot be changed without patching the launcher | `flux-cli/scripts/start-script.txt:150`, `flux-cli/build.gradle:133-149` |
| The launcher needs `sh xargs sed tr uname ls readlink` and uses `which java` only when `JAVA_HOME` is unset | Assert those tools at build time; derive `JAVA_HOME` in the wrapper (`which` is absent from UBI minimal images) | `start-script.txt:152-172, 247-277` |
| Nine `--add-opens` flags and `-Dio.netty.handler.ssl.noOpenSsl=true` are baked into `DEFAULT_JVM_OPTS`; `JAVA_OPTS` and `FLUX_OPTS` are appended; `JAVA_OPTS` appears *before* `-classpath` on the command line; no `-Xmx` is set | Keep the launcher (the flags stay in sync with the Flux build); add defaults through `JAVA_OPTS`; `-cp` cannot be injected via `JAVA_OPTS` | `flux-cli/build.gradle:182-210` |
| `help` exits with code **2**; `version` **starts Spark** (binds a driver port, writes `spark-*` to the temp dir); no Flux command exits 0 without a writable temp directory; abbreviated command names are accepted | `CMD ["help"]` is for humans only; `version` is the readiness/functional test; the entrypoint cannot use a static command list | `cli/Main.java:130-133, 199, 216-220`, `impl/VersionCommand.java:32` |
| Every invocation creates one `SparkSession` through `getOrCreate`, shares static progress counters and global Hadoop/Azure credentials; the second command in the same JVM silently ignores its `--spark-conf` | **One command per JVM.** Parallelism means separate processes or containers | fortran review FLX-022 |
| Spark's local dir defaults to `java.io.tmpdir`; snappy, zstd-jni, lz4, Netty and ONNX Runtime extract native `.so` files into the temp dir and `dlopen` them | The temp directory must be writable **and mounted without `noexec`** | `docs/import/handling-errors.md:34-44`, `impl/SparkUtil.java:347-383` |
| Nothing writes under the install directory; the Spark UI is disabled; there is no Derby/Hive metastore; `HOME` is touched only by `--s3-use-profile` (`~/.aws`) and the MiniLM embedder (`~/.djl.ai`, overridable with `DJL_CACHE_DIR`) | `/opt/flux` can be root-owned and read-only; one writable volume (`/tmp`) suffices | `SparkUtil.java:362-374`, `S3Params.java:30-35` |
| Spark binds a driver port even in local mode; container hostnames are often unresolvable | `SPARK_LOCAL_IP=127.0.0.1` by default | `docs/faq.md:23-35` |
| Configuration is CLI options only: `@options-file`, plus undocumented `-Dmarklogic.client.<option>` system properties. **No environment-variable support.** Password-style options prompt interactively when given without a value | Secrets travel in a mounted options file; the README warns that prompts need a TTY | `impl/ConnectionParams.java:19-308`, `cli/Main.java:37, 204` |
| The embedder integrations are separate shadow jars for `ext/`: MiniLM (~202 MB, **contains** the ONNX model and tokenizer — nothing is downloaded at runtime), Ollama (<1 MB) and Azure OpenAI (~14 MB). MiniLM's natives need glibc ≥ 2.28 | "Embedding downloads" are a non-issue; the jar is what must be provided; UBI 9 (glibc 2.34) is fine, `ubi-micro`/distroless are not | `flux-embedding-model-minilm/build.gradle:12-15` |
| Logging is a console appender only with no `--log-level` option; `log4j-layout-template-json` is already on the classpath | Logging stays on stdout; levels and JSON format are switched by environment variables read by the log4j2 config | `flux-cli/src/dist/conf/log4j2.properties` |
| Exit codes: 0 success (even with partial write failures unless `--abort-on-write-failure`), 1 failure, 2 usage | Documented verbatim; automation must use `--abort-on-*` | `cli/Main.java` |
| `develop` depends on `-SNAPSHOT` artifacts (Spark connector, Java Client) on the internal Artifactory; releases publish `marklogic-flux-<v>.zip` and the embedder jars on GitHub with sha256 digests | Iron Bank (offline) builds consume the **released** zip; the local build can use either | `build.gradle:46-48`, `gradle.properties:4`, GitHub releases |

### 3.2 Iron Bank requirements that shape the image

From <https://docs-ironbank.dso.mil> (Dockerfile requirements, manifest, repository structure, ABC/ORA policy,
pipeline README) as of 2026-11-05:

- **Offline build.** Every external file is a `hardening_manifest.yaml` `resources:` entry with a sha256 and is
  pre-staged into the build context under `filename`. No `curl`/`wget`/`ADD`; no network from the Dockerfile; Maven
  Central is **not** proxied, so jars cannot be fetched individually.
- **Base image.** `ARG BASE_REGISTRY` / `BASE_IMAGE` / `BASE_TAG`, `FROM ${BASE_REGISTRY}/${BASE_IMAGE}:${BASE_TAG}`,
  UBI 9 preferred, "most specific Iron Bank base image available" (a Red Hat OpenJDK image for Java applications).
- **Dockerfile rules.** Single `dnf/microdnf update … && clean all && rm -rf /var/cache/dnf` layer; do not remove
  base-image packages; non-root numeric `USER` ≥ 1000 (no `gosu`, no user switching in scripts); binaries root-owned
  read/execute; no `chmod 777`, no SUID/SGID; no labels in the Dockerfile (they live in the manifest);
  `HEALTHCHECK` not required ("no longer part of the OCI format"); `EXPOSE` documentary only; multi-stage builds
  recommended; scripts committed under `scripts/`, config under `config/`.
- **Repository files.** `Dockerfile`, `hardening_manifest.yaml`, plain-text `LICENSE`, `README.md` (with
  Kubernetes run instructions — "instructions for environments outside of Kubernetes will be rejected"), optional
  `testing_manifest.yaml`, `renovate.json`, `scripts/`, `config/`. No source code.
- **Manifest.** `apiVersion: v1`; `name` without the `ironbank/` prefix; most specific tag first, `latest` last;
  `args` holds only `BASE_IMAGE`/`BASE_TAG`; five required OCI labels plus `mil.dso.ironbank.image.type`
  (`opensource`/`commercial`), keywords and product name; maintainers with Repo1 usernames.
- **Logs** go to STDOUT (ABC 4.2.4.6).
- **Vulnerability lifecycle (ABC).** Every jar inside the image is scanned (Anchore/Syft SBOM, Prisma, OpenSCAP,
  ClamAV). Findings must be justified within Critical 5 d / High 10 d / Medium 30 d / Low 60 d and remediated or
  mitigated within 15 / 35 / 180 / 360 d; images are rescanned about every 12 hours; updates "must be submitted no
  later than public release day"; an image with no update for six months is EoL. The ORA score starts at 100 and
  loses points per open finding, so **fewer jars = better score**.
- **FIPS** is not required today (FAQ 3.4: "Not at this time, although this may be implemented in the future").

### 3.3 Runtime constraints on Platform One / Big Bang clusters

Kyverno policies enforce `runAsNonRoot` (uid > 0), dropped capabilities, no privilege escalation, no host namespaces,
`RuntimeDefault` seccomp, restricted volume types, and commonly (audit by default, enforced by many tenants)
`readOnlyRootFilesystem`, CPU/memory limits and image-registry restrictions. Istio sidecars may be injected into Job
pods, which keeps Jobs from completing. The usual pattern for Iron Bank images is `readOnlyRootFilesystem: true`
plus an `emptyDir` at `/tmp`.

### 3.4 The existing Iron Bank MarkLogic Server image

`dsop/progress/marklogicserver/marklogic12` (registry name `progress/marklogicserver/marklogicserver12`, tags
`12.1.0`, `latest`) is built on `redhat/ubi/ubi9-minimal`, runs as uid 1000, exposes 7997–8010, speaks plain HTTP on
8000–8002 unless TLS is configured, and initialises from `MARKLOGIC_INIT`, `MARKLOGIC_ADMIN_USERNAME/PASSWORD`
(or `_FILE` secrets). Its maintainers (Kube Ninjas) are the natural owners of the Flux Repo1 project. Nothing in it
requires special handling from Flux beyond a mounted truststore when TLS is on; sharing uid 1000 makes `fsGroup`
settings in a shared pod trivial.

## 4. Architecture overview

```
docker run / Kubernetes Job
        │  args: import-files @/run/secrets/flux/options.txt --path /workspace/data ...
        ▼
/usr/local/bin/flux-entrypoint   (router: flux args | executable passthrough)
        │ exec
        ▼
/usr/local/bin/flux              (wrapper: JAVA_HOME, JAVA_OPTS defaults, log format, FLUX_LOG_DIR tee)
        │ exec
        ▼
/opt/flux/bin/flux               (unchanged Gradle launcher: --add-opens, classpath lib/*:ext/*:conf)
        │ exec
        ▼
java … com.marklogic.flux.cli.Main   ──► Spark local[N] in-process ──► MarkLogic (HTTP/HTTPS 8000+)
        │
        ├── reads   /workspace/**  (user data, read-only mounts)         /run/secrets/flux/* (options, truststore)
        ├── loads   /opt/flux/ext/*.jar  (embedders, JDBC drivers — mounted, empty in the image)
        └── writes  /tmp  (Spark scratch, native .so extraction, DJL cache)   — the only required writable path
```

Image filesystem (root-owned, read-only in use):

```
/opt/flux/            bin/flux  lib/*.jar  ext/README.txt  conf/log4j2.properties  conf/log4j2-json.properties  LICENSE.txt  NOTICE.txt
/usr/local/bin/       flux  flux-entrypoint
/home/flux            uid 1000:0, 0770 (HOME; written only by --s3-use-profile)
/workspace            uid 1000:0, 0770 (WORKDIR; user mounts)
/tmp                  must be mounted writable + exec
```

Repository layout (phase 1):

```
ironbank/                      == the Repo1 project, copied verbatim in phase 2
  Dockerfile                   two stages; identical for local and Iron Bank builds
  hardening_manifest.yaml      tags, args, labels, resources (release zip), maintainers (TODOs for onboarding)
  README.md  LICENSE           Iron Bank README (usage, Kubernetes Job, resources); plain-text Apache-2.0
  testing_manifest.yaml        pipeline functional tests (version, uid, read-only install, help exit 2)
  renovate.json                bumps the Flux release URL in the manifest (github-releases datasource)
  scripts/flux-entrypoint      router
  scripts/flux                 wrapper
  config/log4j2.properties     upstream config + ${env:FLUX_LOG_LEVEL}
  config/log4j2-json.properties JsonTemplateLayout (ECS)
  config/lib-excludes.txt      jars pruned at build time, one justification per line
  ext-opt/                     optional jars baked into /opt/flux/ext (empty by default)
  marklogic-flux.zip           staged input (gitignored) = the manifest resource
ironbank-local/                never copied to Repo1
  docker-compose.ironbank.yml  `flux` service beside the MarkLogic/Postgres/Ollama test stack, hardened settings
  smoke-test.sh                25 docker-level checks
  k8s/marklogic.yaml, k8s/flux-jobs.yaml, k8s-smoke.sh   throw-away MarkLogic pod + two Flux Jobs
  options/*.txt  ext/          options files (Secret stand-ins); staged embedder jars for the mounted-jar tests
build.gradle                   ironbankStage, ironbankBuild, ironbankManifestResources, ironbankSmokeTest, ironbankK8sSmokeTest
.gitattributes                 LF enforcement for everything that ends up inside the image
```

## 5. Design decisions

Each decision lists the choice, the alternatives considered, why the choice won, and its consequences.

### 5.1 Usage model: a CLI invocation image, one command per container

**Decision.** The container runs one Flux command and exits. In Kubernetes it is a `Job`/`CronJob`, never a
`Deployment`. Several commands in one container are possible only through a mounted script that starts a new
`flux` process per command.

**Alternatives.** (a) A long-running "runner" service accepting commands over HTTP or a queue — that is flux-box's
territory and would add a listening port, a health check and a far larger attack surface to justify. (b) A single JVM
executing several commands — unsafe because Flux shares its `SparkSession`, static counters and global Hadoop/Azure
credentials across commands (§3.1).

**Why.** It is what the epic asks for ("runs a command, then exits"), it is what Iron Bank expects of a tool image
(no ports, no HEALTHCHECK), and it keeps Flux's process-per-command isolation.

**Consequences.** Parallelism is horizontal (Job `parallelism`, several `docker run`s, or `flux a & flux b & wait`
in a batch script); each run must be bounded (`--spark-master-url local[N]`, `--thread-count`, `--batch-size`,
CPU/memory limits). Jobs are not idempotent unless URIs are deterministic (`--uri-template`, `--uri-prefix`), so the
README recommends `backoffLimit: 0` and a generous `terminationGracePeriodSeconds`.

### 5.2 Repository layout mirrors the Repo1 project

**Decision.** `ironbank/` contains exactly the files Iron Bank expects at the project root, in their final names;
everything used only for local validation lives in `ironbank-local/`; build automation lives in the root
`build.gradle`.

**Alternatives.** A `docker/` directory with a developer Dockerfile and a separate, later Iron Bank Dockerfile; or
keeping the Iron Bank project only on Repo1.

**Why.** One Dockerfile avoids drift between "the image we test" and "the image we submit". Keeping the project in
the Flux repository keeps it versioned with the code it packages and lets the Flux release checklist update it.
Phase 2 is then a copy plus value changes.

**Consequences.** `ironbank/.dockerignore` is an allow-list so the local and pipeline build contexts are identical.
`.gitattributes` forces LF line endings for `ironbank/**`, and the Dockerfile sets file modes itself, because a
Windows checkout cannot carry exec bits and CRLF would break the scripts inside the image.

### 5.3 Base image and Java version

**Decision.** `redhat/openjdk/openjdk17-runtime-ubi9-slim:1.17` from registry1 (JRE only, `ubi9-minimal`, `microdnf`,
`JAVA_HOME` and `LANG=C.UTF-8` preset). Locally, without registry1 credentials, the public
`registry.access.redhat.com/ubi9/openjdk-17-runtime:1.21` (same lineage: RHEL 9.5, glibc 2.34).

**Alternatives.**
- *OpenJDK 21.* Supported by Flux's docs and the "latest release series" Iron Bank prefers, but Flux is built and
  CI-tested on 17 only. Deferred: switching is a `BASE_IMAGE`/`BASE_TAG` change, to be done after the smoke suite is
  run on 21.
- *`ubi9` (full) + `java-17-openjdk-headless`.* More packages to scan for no benefit; the Iron Bank "most specific
  base image" rule points at the OpenJDK images.
- *`ubi-micro`, distroless, Alpine.* Attractive for size, but the launcher needs a shell and coreutils, Spark/Hadoop
  need glibc (Alpine is musl), and the MiniLM/ONNX natives need glibc ≥ 2.28. `ubi-micro` has no package manager, so
  the required `microdnf update` layer is impossible.
- *The MarkLogic Server base (`ubi9-minimal`) plus a JRE install.* Equivalent to the OpenJDK image but we would own
  the JRE packaging and its findings; the Red Hat OpenJDK image is maintained by Red Hat and already in Iron Bank.
- *A JDK (`-devel`) image.* Larger, more findings; Flux needs only a JRE.

**Consequences.** The Dockerfile sets its own `USER 1000`, `HOME` and `ENV`, so the base's own user (185 in the
public image, 1001 in the Iron Bank slim image) is irrelevant. The public base is several months behind the RHEL 9
repositories, so the mandatory `microdnf update` layer is large locally (369 MB, re-shipping the JRE); on an
up-to-date Iron Bank base it is small.

### 5.4 Build structure: two stages, offline, nothing clever

**Decision.** Stage 1 (`unpack`) installs `unzip`, extracts `marklogic-flux.zip`, prunes, copies in configs and
scripts, asserts the launcher's tool dependencies exist, and fixes ownership/modes. Stage 2 runs one
`microdnf update && clean` layer, creates the user and directories, and `COPY --from=unpack --chown=0:0`s the result.
Both stages use the same `BASE_*` triple.

**Alternatives.**
- *Build Flux from source inside the Dockerfile* (the Copilot draft's `gradle:8 … ./gradlew installDist`). Impossible
  in an offline pipeline and against the "no source code in the repository" rule.
- *Declare every jar as a manifest resource.* Maven Central is not proxied and ~318 resources would be unmaintainable;
  the release zip is the natural unit (plus it is what customers download, so provenance is identical).
- *`ADD` the zip (auto-extract).* Prohibited by Iron Bank.
- *Install `unzip` in the final image.* Unnecessary runtime package; stage 1 keeps it out. (`ubi9-minimal` ships
  `tar` and `gzip` but no `unzip`; a `.tar.gz` release asset would remove even this install — see §10.)

**Why.** Every Iron Bank Java example (Spark, NiFi, Maven, sonar-scanner) follows this shape; it keeps the runtime
layer free of build tools and produces a deterministic, reviewable layer history.

**Consequences.** The manifest resource uses a **version-less filename** (`marklogic-flux.zip`) so a Flux release
changes only `url`, `sha256`, `tags` and the version label — the Dockerfile never changes, which is what Renovate and
the release-day MR need. The build fails early (`Base image is missing: xargs`) if a future base drops a tool the
launcher needs.

### 5.5 Image contents and pruning

**Decision.** Ship the full distribution (functional parity), minus: `bin/flux.bat`, `bin/*.dll`, `bin/*.exe`
(Windows), `lib/native/` (never loaded on Linux because the launcher sets no `java.library.path`), and the Netty
native jars for macOS/Windows/aarch64/riscv64 listed in `config/lib-excludes.txt`. Each exclusion carries a comment
saying why it is safe.

**Alternatives.** (a) Trim aggressively now with `jdeps`/runtime tracing (Hadoop YARN/MapReduce, Tika, Jena, cloud
SDKs). Rejected for phase 1: it needs the Flux test suite as evidence and is exactly the work VAT findings will
prioritise in phase 2. (b) Ship a different, slimmer distribution from the Flux build. Possible later; a single
artifact shared with desktop users is better for provenance.

**Why.** The glob list is the hook phase 2 needs: a finding against a provably unused jar becomes one line plus its
justification, which doubles as the VAT text. The phase-1 list is restricted to payloads that *cannot load* on
linux/amd64, so it cannot change behaviour.

**Consequences.** `/opt/flux` is ~307 MB in the image. The linux-x86_64 `netty-tcnative` jar is kept even though
`-Dio.netty.handler.ssl.noOpenSsl=true` disables it, to stay byte-identical to desktop behaviour; it is the first
phase-2 candidate.

### 5.6 User model: fixed uid 1000, gid 0

**Decision.** A `flux` user, uid 1000, primary gid 0, created by appending to `/etc/passwd` (ubi9-minimal has no
`shadow-utils`), with a guard that fails the build if a future base already uses uid 1000. `/opt/flux` and the
scripts are `root:root`, directories 0755, files 0644, executables 0755. `/home/flux` and `/workspace` are
`1000:0`, 0770.

**Alternatives.** (a) Reuse the base image's user (185 or 1001) — differs between the public and Iron Bank bases and
between base versions. (b) Group-writable `/opt/flux` with `chmod -R g=u` (the Copilot draft) — violates "binaries
root-owned, read/execute only" and is unnecessary because Flux never writes there. (c) Supporting arbitrary uids
(OpenShift) by appending a passwd entry at start-up, as the Iron Bank Spark image does — impossible under
`readOnlyRootFilesystem` with a root-owned `/etc/passwd`, and making `/etc/passwd` writable would itself be a
finding.

**Why.** Uid 1000 matches the Iron Bank MarkLogic Server image, satisfies the numeric-uid ≥ 1000 rule, and gid 0
is the OpenShift convention that lets group permissions work if a platform does assign a random uid.

**Consequences.** Validation showed that Hadoop (via Spark's file access) refuses an effective uid that has no passwd
entry: `Error: Invalid UID, could not determine effective user`. `HADOOP_USER_NAME` does not help. The README
therefore mandates `runAsUser: 1000` (or a mounted passwd file containing the platform's uid). This is a documented
limitation rather than a workaround in the image.

### 5.7 Entrypoint and wrapper

**Decision.** Two small bash scripts:

- `flux-entrypoint` (router): no arguments → `flux help`; first argument starting with `-` or `@` → Flux; first
  argument that resolves to an executable on `PATH` → run as-is; anything else → Flux.
- `flux` (wrapper, on `PATH`): derives `JAVA_HOME` if unset; builds `JAVA_OPTS = FLUX_DEFAULT_JVM_OPTS +
  -Duser.home=$HOME [+ JSON log config] + user JAVA_OPTS`; warns if the temp directory is not writable; `exec`s the
  launcher. With `FLUX_LOG_DIR` set it runs the launcher in the background through `tee`, forwards SIGTERM/SIGINT,
  and exits with Flux's code.
- `ENTRYPOINT ["/usr/local/bin/flux-entrypoint"]`, `CMD ["help"]`.

**Alternatives.**
- *`ENTRYPOINT ["/opt/flux/bin/flux"]` directly.* No way to apply image defaults (`JAVA_OPTS` is replaced, not
  merged, by the launcher), no batch-script passthrough, no `FLUX_LOG_DIR`.
- *Bypass the launcher and `exec java` with a hand-written flag list.* Duplicates the nine `--add-opens` flags that
  the Flux build owns; they would drift. Keeping `bin/flux` keeps them in sync automatically.
- *A static list of Flux subcommands in the router.* Breaks picocli's abbreviated commands (`import-parq`) and needs
  maintenance on every new command. "Is `$1` an executable?" needs neither; `help`/`version`/`copy` are not
  executables, so they route to Flux. (`command -v help` in bash returns the *builtin* name, which fails the `-x`
  test, so even that collision is handled.)
- *`JAVA_TOOL_OPTIONS` for defaults.* Affects every JVM in the container (including `keytool`), prints a "Picked up"
  line to stderr on every run, and cannot be ordered before the user's `JAVA_OPTS`.
- *`tini`/`--init`.* Not needed: java is `exec`'d and becomes PID 1, spawns no children, and the JVM runs Spark's
  shutdown hook on SIGTERM. `tini` would also be one more manifest resource to justify.
- *`CMD ["version"]` so a bare run exits 0.* `help` is what a human expects from a bare `docker run`; the exit code
  2 is documented and the Iron Bank test uses `version`.

**Why.** Users keep the exact CLI syntax; batch scripts get real process isolation per command; defaults are
overridable in the ordinary way (later JVM flags win).

**Consequences.** `bash` is the shebang (ubi9's `/bin/sh` is bash, but `PIPESTATUS`/process substitution are
bash-only). The tee path is the only case where java is not PID 1; the wrapper's trap/`wait` loop preserves the exit
code and forwards signals, verified by the smoke test.

### 5.8 Writable filesystem contract

**Decision.** Exactly one path must be writable: `/tmp`, mounted **without `noexec`** (Kubernetes disk-backed
`emptyDir` with a `sizeLimit`; Docker `--tmpfs /tmp:rw,exec,size=…`). `HOME=/home/flux` exists but is only needed
writable for `--s3-use-profile` with SSO. `DJL_CACHE_DIR=/tmp/djl.ai` keeps the MiniLM cache inside the same volume.
`SPARK_LOCAL_DIRS`/`-Djava.io.tmpdir` are the documented knobs for large jobs.

**Alternatives.** (a) `HOME=/tmp` (sonar-scanner's trick) — simpler but makes `~/.aws` mounts land in `/tmp` and is
surprising to users. (b) Setting `spark.local.dir` in the image — redundant (`java.io.tmpdir` already is `/tmp`) and
would hide the `SPARK_LOCAL_DIRS` escape hatch. (c) A dedicated `/scratch` volume — more to document and mount for
no gain; `/tmp` is what Big Bang charts already mount.

**Why.** One `emptyDir` is the minimum a `readOnlyRootFilesystem` pod needs, and it is the convention on P1 clusters.

**Consequences.** Docker's `--tmpfs` default (`noexec`) is a trap; the README and the wrapper's warning address it.
Memory-backed tmpfs counts against the memory limit, so the README mandates disk-backed `emptyDir` for large copies
and exports.

### 5.9 JVM defaults

**Decision.** `FLUX_DEFAULT_JVM_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8
-Dorg.xerial.snappy.tempdir=/tmp"`, applied before user `JAVA_OPTS`/`FLUX_OPTS`.

**Why.** Flux sets no heap size; the JVM's container default (25 % of the limit) wastes three quarters of a Job's
memory. `ExitOnOutOfMemoryError` makes a Job fail fast instead of limping with a dead executor thread. The snappy
tempdir is the documented fix for `noexec` temp directories. Setting the whole string in one variable lets users
restore stock behaviour with `FLUX_DEFAULT_JVM_OPTS=""`.

**Consequences.** Validated: `--memory 1g` yields `MaxHeapSize` ≈ 805 MB. `ExitOnOutOfMemoryError` differs from
the desktop CLI, where Spark may retry a task after an executor-thread OOM; for a Job, failing is the right outcome.

### 5.10 Logging

**Decision.** Everything to stdout through the distribution's `log4j2.properties`, with three additions:
`FLUX_LOG_LEVEL` (log4j2 `${env:…:-INFO}` lookup on the Flux and connector loggers), `FLUX_LOG_FORMAT=json`
(a second config using `JsonTemplateLayout` with ECS field names; the layout jar is already on the classpath), and
`FLUX_LOG_DIR` (per-invocation file `flux-<utc timestamp>-<pid>.log` in a mounted directory, in addition to stdout).

**Alternatives.** (a) stdout only — the Iron Bank minimum; the user asked for per-invocation capture, and audit
scenarios want a file per run. (b) A log4j2 file appender — would write inside the container by default (prohibited)
and could not be switched on without a config mount. (c) Adding `--log-level` to Flux — an upstream change, listed in
§10; the env lookup gives the same effect today.

**Why.** Big Bang collects container stdout with Fluent Bit; JSON lines parse without extra configuration;
`FLUX_LOG_DIR` is opt-in and never changes the stdout stream.

**Consequences.** Users who need a different layout mount their own `log4j2.properties` over
`/opt/flux/conf/log4j2.properties` (ConfigMap `subPath`) or point `-Dlog4j2.configurationFile` at it.

### 5.11 Configuration and secrets

**Decision.** Credentials go in an options file mounted from a Secret and referenced as
`@/run/secrets/flux/options.txt`. Nothing is baked into the image. The undocumented `-Dmarklogic.client.*` system
properties remain available through `JAVA_OPTS` for non-secrets. No environment-variable support is added.

**Alternatives.** (a) Teach Flux picocli `${env:…}` defaults for the connection options — an upstream feature, and
environment variables are visible in `kubectl describe` and `/proc` anyway. (b) An entrypoint that translates
`FLUX_*` variables into arguments — same exposure, plus a second option syntax to document and keep in sync with Flux.

**Why.** Options files are already a Flux feature, support every option, keep values out of the process list and
`docker inspect`, and map directly onto Kubernetes Secrets. The one caveat (password options prompt when given
without a value) is documented.

**Consequences.** Secret files must use LF line endings and quoted values; the README shows the exact format. The
Kubernetes example uses `defaultMode: 0440`.

### 5.12 Extensions: embedders, JDBC drivers, custom splitters

**Decision.** `/opt/flux/ext/` is **empty** in the image and is the single mount point for all extension jars.
Local builds can bake the three embedder jars with `-PironbankWithEmbedders=true` (they land in `ext-opt/` and are
copied in stage 1). An Iron Bank-scanned "with embedders" variant, if wanted, is a second Repo1 tag in phase 2.

This decision was revised during review: the first draft baked all three embedder jars.

**Alternatives.**
- *Bake all three.* Full parity in one image, MiniLM works offline out of the box; but +216 MB, the ONNX/DJL natives
  and langchain4j enter the scan surface, and a directory mount over `ext/` (the natural way to add a JDBC driver)
  hides the baked jars — users would need `subPath` mounts or derived images.
- *Bake embedders into `lib/` and leave `ext/` for mounts.* The launcher's classpath orders `lib/*` before `ext/*`,
  but order *within* one wildcard directory is unspecified by the JVM. The MiniLM shadow jar bundles langchain4j
  1.14.1-beta24 classes while `lib/` carries langchain4j 1.21.0 via the connector; today `lib/` reliably wins, in
  `lib/` it would be a directory-listing coin flip.
- *A second directory added to the classpath by the wrapper.* `JAVA_OPTS` is placed before `-classpath` in the
  launcher, so a `-cp` there loses; `-Xbootclasspath/a:` would work but puts jars on the bootstrap loader and breaks
  `ServiceLoader`/log4j plugin discovery; building a shadow `APP_HOME` of symlinks in `/tmp` works but confuses every
  stack trace and support conversation.
- *Two images.* Doubles the Iron Bank maintenance load (two sets of findings, two release-day MRs).

**Why.** It is exactly how a desktop Flux install works (the docs already tell users to drop jars into `ext/`), it
treats embedders and JDBC drivers uniformly, it keeps the image small and its findings list short, and the
MiniLM "download" concern turned out to be moot (the model is inside the jar). The cost — a mounted jar is not covered
by the image's Iron Bank scan — already applies to JDBC drivers and is stated in the README.

**Consequences.** Validated both ways: with the MiniLM jar mounted, `--embedder minilm` works under
`readOnlyRootFilesystem` (natives extracted under `/tmp`); without it, Flux fails cleanly with exit 1. A one-line
`FLUX_CLASSPATH` hook in the Flux start script would allow a second directory later (§10) but is not needed.

### 5.13 Networking

**Decision.** No `EXPOSE`; `SPARK_LOCAL_IP=127.0.0.1` so Spark's driver and block-manager ports bind to loopback;
the Spark UI is already disabled by Flux. Outbound only: MarkLogic, and per command JDBC, S3/Azure, Ollama/Azure
OpenAI, Semaphore.

**Alternatives.** Leaving `SPARK_LOCAL_IP` unset risks `BindException`/hostname-resolution failures in containers
(Flux FAQ). The only downside is the unusual case of pointing `--spark-master-url` at a remote standalone cluster
from inside the container, for which the README documents `-e SPARK_LOCAL_IP=`.

### 5.14 TLS, authentication and FIPS

**Decision.** Mount a PKCS12 truststore (and keystore for two-way TLS) from a Secret and pass `--truststore-path`
etc. in the options file; `--ssl-protocol default` when the CA is already in the JVM's `cacerts`. No certificates of
any kind in the image. For FIPS-enabled hosts, document `--auth-type basic` (over TLS) or certificate authentication.

**Why.** Iron Bank forbids keys/certs in images; Red Hat's OpenJDK auto-enables FIPS mode on FIPS hosts, where the
provider refuses MD5 (needed by MarkLogic digest authentication, Flux's default with `--connection-string`) and
JKS stores. FIPS is not an Iron Bank requirement today, so the image stays FIPS-*compatible* rather than
FIPS-*configured*; a `-fips-bc` variant (BouncyCastle FIPS jars as manifest resources, NiFi pattern) can follow if
customers require it.

### 5.15 Parallelism and resources

**Decision.** Document the resource model instead of enforcing it: one JVM per command, heap = 75 % of the memory
limit, `--spark-master-url local[N]` matched to the CPU request, `--thread-count`/`--batch-size` to bound MarkLogic
load, `/tmp` sized for shuffle spill, Jobs with `parallelism` for horizontal scaling.

**Why.** The right numbers depend on the command and the MarkLogic cluster; the image's job is to make the knobs
discoverable and the defaults safe.

**Consequences.** Validated: two `flux` processes run concurrently in one container (each binds its own loopback
ports and temp directories), and two Jobs run concurrently against one MarkLogic pod. Concurrent imports of the same
paths overwrite each other's URIs — hence the `--uri-prefix` guidance.

### 5.16 Kubernetes deployment model

**Decision.** The README's reference manifest is a `Job` with `restartPolicy: Never`, `backoffLimit: 0`,
`terminationGracePeriodSeconds: 120`, `sidecar.istio.io/inject: "false"`, pod security context (`runAsNonRoot`,
`runAsUser: 1000`, `runAsGroup: 0`, `fsGroup`, `RuntimeDefault` seccomp), container security context
(`allowPrivilegeEscalation: false`, `readOnlyRootFilesystem: true`, `drop: ["ALL"]`), a disk-backed `emptyDir` at
`/tmp`, the options Secret at `/run/secrets/flux`, and data from a PVC at `/workspace`. `CronJob` = the same template
with `concurrencyPolicy: Forbid`.

**Alternatives.** Bundling `scuttle` (as the Iron Bank Spark image does) to terminate an injected Istio sidecar so
Jobs complete — rejected: an extra network-aware binary to justify; the inject annotation or Istio native sidecars
(Kubernetes ≥ 1.29) solve it without image changes.

**Consequences.** Validated end to end on Rancher Desktop's k3s (§7.3).

### 5.17 Manifest, labels, tags, Renovate and functional tests

**Decision.** `hardening_manifest.yaml` is complete except for the values that only onboarding can settle (Repo1
`name`, maintainers' usernames, the release sha256). Labels live only in the manifest; tags are the Flux version then
`latest`; `mil.dso.ironbank.image.type: opensource`, `licenses: Apache-2.0`, `product.name: MarkLogic Server` so the
image groups with MarkLogic Server on ironbank.dso.mil. `renovate.json` holds a regex manager with the
`github-releases` datasource on the resource URL. `testing_manifest.yaml` runs `flux version`, checks the uid, checks
that `/opt/flux` is not writable, and checks that `help` exits 2; the Kubernetes test runs `version`.

**Why.** All of this is Iron Bank's prescribed structure; the ORA score rewards a dependency-update tool and
passing pipelines, and the functional tests make a base-image bump self-verifying.

### 5.18 Local build and validation workflow

**Decision.** Gradle tasks in the root build: `ironbankStage` (stages `marklogic-flux.zip` from `:flux-cli:distZip`
or, off the Progress network, from `-PironbankFluxZip=<downloaded release zip>`; stages the embedder shadow jars for
the mounted-jar tests; optionally bakes them), `ironbankBuild` (`docker build` with overridable `BASE_*`, defaulting
to the public Red Hat image; refuses CRLF files), `ironbankManifestResources` (prints the manifest `resources` block
with the sha256), `ironbankSmokeTest` and `ironbankK8sSmokeTest`. A compose override adds a hardened `flux` service
next to the existing MarkLogic/Postgres/Ollama test stack.

**Why.** Developers without registry1 or VPN access can still build and test the exact Dockerfile; the smoke tests
encode the runtime contract (exit codes, read-only install, heap, logging, data commands, embedders) so regressions
in Flux or the base image surface before a Repo1 pipeline run.

## 6. Security posture against Iron Bank and Big Bang requirements

| Requirement | How the image meets it | Evidence |
|---|---|---|
| Offline build; all inputs declared with checksums | One resource (`marklogic-flux.zip`) with sha256; no network in the Dockerfile; `.dockerignore` allow-list | `hardening_manifest.yaml`, Dockerfile |
| Approved base, `BASE_*` args | `redhat/openjdk/openjdk17-runtime-ubi9-slim` | Dockerfile `ARG`s |
| Single update/clean layer; no removal of base packages; no build tools at runtime | Stage 2 has one `microdnf update … clean all` layer; `unzip` only in stage 1 | `docker history` |
| Non-root numeric user ≥ 1000; no `gosu`/user switching | `USER 1000`, passwd entry, no switching in scripts | smoke test `id -u` = 1000 |
| Root-owned read/execute binaries; no 777; no SUID/SGID | `chown 0:0`, 0755/0644, exec bits only on launcher and scripts | smoke test `ls -l` |
| No labels/HEALTHCHECK in Dockerfile; no `ADD` | None present | Dockerfile |
| Logs to STDOUT | Console appender only; files only via opt-in `FLUX_LOG_DIR` | smoke tests |
| No credentials or certificates in the image | None; Secrets mounted at runtime | README |
| `runAsNonRoot`, drop ALL, no privilege escalation, seccomp, `readOnlyRootFilesystem` | All exercised by the smoke tests and the Kubernetes Jobs | §7 |
| Kubernetes run instructions in README | `Job` and `CronJob` guidance, resources table | README |
| Functional tests | `testing_manifest.yaml` | — |
| Dependency updates | `renovate.json` | — |

## 7. Validation

### 7.1 Environment

Windows 11 host with Rancher Desktop (dockerd 29.5, k3s v1.36 on the docker runtime). The image was built from the
**Flux 2.1.2 release assets** (`marklogic-flux-2.1.2.zip` and the three embedder jars; sha256 verified against the
digests GitHub publishes) because `develop` needs `-SNAPSHOT` artifacts from the Progress network. Base:
`registry.access.redhat.com/ubi9/openjdk-17-runtime:1.21` (RHEL 9.5, glibc 2.34, OpenJDK 17.0.21). MarkLogic:
`progressofficial/marklogic-db:12.1.0-ubi9-rootless-2.3.1`, the public twin of the Iron Bank server image.

### 7.2 Docker-level smoke tests (`ironbank-local/smoke-test.sh`): 25 passed, 0 failed

Every run used `--read-only --tmpfs /tmp:rw,exec,size=2g --cap-drop ALL --security-opt no-new-privileges` as uid 1000.

| Area | Result |
|---|---|
| `version` | exit 0, prints Flux 2.1.2 / Java 17.0.21 / Spark 4.2.0 (Spark starts on loopback inside the tmpfs) |
| Exit codes | no args → 2; `help import-files` → 2 with options listed; `import-parq` abbreviation resolves; unknown option → 2; `sh -c 'exit 7'` → 7; wrong password → 1 with `Command failed` |
| Read-only install | `touch /opt/flux/x` and `/etc/x` fail; launcher and wrapper are `root:root -rwxr-xr-x`; `bin/` holds only `flux`; no jars in `ext/` |
| JVM / logging | `--memory 1g` → `MaxHeapSize` 805 MB (75 %); `FLUX_LOG_FORMAT=json` emits `{"@timestamp"…}` lines; `FLUX_LOG_DIR` writes one file per invocation; two `flux version` in parallel in one container succeed |
| Data commands (test-app on 8003) | `import-files` (mixed-files fixture) → `export-files` wrote 6 files to the mounted volume → `copy` → `reprocess`, all exit 0 |
| Embedders | `import-files --splitter-text --embedder minilm` with the jar mounted at `/opt/flux/ext` succeeds; the same command without the mount fails cleanly with exit 1 |
| Arbitrary uid (`--user 12345:0`) | **fails**: `Error: Invalid UID, could not determine effective user` → README mandates `runAsUser: 1000` |
| `noexec` `/tmp` | Netty warns and falls back to NIO; a parquet `--count` still succeeds, so the failure is library-dependent (snappy writes are the known hard failure); the exec requirement stays |

Image size locally: 1.58 GB = base 275 MB + `microdnf update` 369 MB (stale public base re-ships the JRE) +
Flux 307 MB.

### 7.3 Kubernetes Jobs (`ironbank-local/k8s-smoke.sh`)

`k8s/marklogic.yaml` runs a throw-away MarkLogic 12 pod (ubi9 rootless, uid 1000, emptyDir data, readiness on 7997)
and `k8s/flux-jobs.yaml` runs two Jobs with the README's restricted pod settings (`runAsNonRoot`, `runAsUser: 1000`,
`fsGroup`, `RuntimeDefault` seccomp, `readOnlyRootFilesystem`, drop ALL, `emptyDir` at `/tmp`, options from a Secret,
input from a ConfigMap, `imagePullPolicy: Never`):

| Job | Form | Result |
|---|---|---|
| `flux-import` | Flux arguments in `args` | Completed; 3 documents written (`Success count: 3`), confirmed through the REST API |
| `flux-batch` | `sh /workspace/batch/batch.sh` from a ConfigMap | Completed; import → export (3 files into the emptyDir) → copy, one JVM per `flux` call |

Lessons recorded in the README: two Jobs importing the same paths concurrently overwrite each other's URIs
(use `--uri-prefix`/`--uri-template`); ConfigMap symlink layouts (`..data/`) are handled and URIs use the mount
path. Both pods reach `Completed` well inside the 180 s the Iron Bank Kubernetes functional test allows.

### 7.4 Not validated in phase 1

TLS to MarkLogic (the test-app's SSL server on 8005 is configured dynamically by the test suite; needs a certificate
template set up by the smoke script), the registry1 base image (needs Iron Bank credentials), FIPS mode, and a run
on OpenJDK 21.

## 8. Risks and mitigations

| # | Risk | Mitigation |
|---|---|---|
| R1 | **Scan volume.** ~310 jars (Spark, Hadoop, Tika, POI, PDFBox, AWS/Azure SDKs, Netty, Jackson) will produce many findings regardless of upstream pinning; jar-embedded natives (snappy, zstd, lz4, Netty, ONNX) need justification too | `lib-excludes.txt` as the trimming hook; VAT templates (shaded-pom false positive, unused transitive, pending upstream); Flux's existing CVE pins in `build.gradle`/`gradle.properties` |
| R2 | **Permanent maintenance load.** 12-hourly rescans, ABC clocks, release-day MRs, 6-month EoL | Triage rota, Renovate, release-checklist step, support policy (phase 2 §9.6) |
| R3 | **Patch-release speed.** An urgent CVE fix may need a released Spark connector; `develop` uses snapshots | Require released connector versions for Iron Bank builds; document the fast path |
| R4 | **Exit-code surprises.** `help` exits 2; `version` needs a writable `/tmp` | Documented; `version` is the readiness test |
| R5 | **Arbitrary uid platforms** (OpenShift) fail in Hadoop's user lookup | README mandates `runAsUser: 1000` or a mounted passwd file |
| R6 | **`noexec` or RAM-backed `/tmp`** breaks native libraries or inflates memory usage | Wrapper warning, README mandates exec-capable disk-backed volume, `SPARK_LOCAL_DIRS` for big jobs |
| R7 | **Secrets on the command line** are visible in `/proc`, `docker inspect`, `kubectl describe` | Options files from Secrets; sysprops documented as non-secret only |
| R8 | **Directory mounts over `ext/`** replace the directory | Inherent to the fixed classpath; `ext/` is empty in the image so there is nothing to hide; optional upstream `FLUX_CLASSPATH` hook |
| R9 | **SIGTERM mid-import** leaves committed batches; retries duplicate data unless URIs are deterministic | `backoffLimit: 0`, grace period, `--uri-template`/`--uri-prefix` guidance |
| R10 | **Istio sidecars** keep Job pods running | Inject annotation / native sidecars; `scuttle` deliberately not bundled |
| R11 | **Windows checkout hygiene** (CRLF, exec bits) | `.gitattributes`, modes set in the Dockerfile, `ironbankBuild` refuses CRLF |
| R12 | **Base-image drift** (uid, `JAVA_HOME`, tools) between tags | Own `USER`/`ENV`; passwd guard; tool assertion fails the build early |
| R13 | **Image size** (~1.6 GB locally) | Small update layer on an up-to-date base; phase-2 trimming; no embedders baked |

## 9. Phase 2: Iron Bank submission and hardening

1. **Onboarding.** Repo1 account and onboarding form; request `dsop/progress/marklogicserver/flux`; coordinate
   with the Kube Ninjas; fill maintainers in the manifest.
2. **Resources.** Pick the Flux release (released connector), set `url`/`sha256`/`tags`/version label from the GitHub
   digests (`gh release view <tag> --json assets` or `./gradlew ironbankManifestResources`); add the release-day MR
   to the Flux release checklist; enable Renovate.
3. **Jar trimming.** Extend `config/lib-excludes.txt` from `jdeps`/runtime tracing with the Flux test suite as
   evidence; candidates: all `netty-tcnative-*`, `leveldbjni`, unused Hadoop YARN/MapReduce, Tika/Jena modules; keep a
   `documentation/jar-inventory.md`.
4. **Pipeline.** Fix lint findings, run OpenSCAP locally, enable the functional tests, work the VAT findings with the
   justification templates; expect this to be the longest item.
5. **FIPS readiness.** Smoke suite on a FIPS-enabled host; confirm basic/certificate auth and PKCS12 guidance;
   decide on a `-fips-bc` variant.
6. **Lifecycle.** Triage rota for rescans, support policy page (which Flux versions, how long), base-tag bumps, and
   the Flux documentation page plus release note announcing the image.
7. **Remaining validation.** TLS smoke test (certificate template on the test-app's 8005 server), the registry1
   base, OpenJDK 21.

## 10. Open questions

For the Iron Bank team / Kube Ninjas:

- Repo1 group and registry name; is the new `marklogic-server-k8s-utility-container` project (created 2026-10-20,
  still the template) intended for Flux?
- Is one release zip as the sole resource acceptable, and is there a practical size limit (~283 MB)?
- Will "Not Vulnerable (unused module)" justifications be accepted for Spark/Hadoop transitives, or is removal
  expected?
- Is a scanned variant with MiniLM baked in wanted (second tag/repo)?
- FIPS timeline; is a `-fips` tag with FIPS-host functional tests required?

For the Flux team (optional upstream improvements; none blocks phase 1):

- `help` returning exit 2 (automation-unfriendly); a `--log-level` option.
- A `FLUX_CLASSPATH` hook in `scripts/start-script.txt` (`…:$APP_HOME/conf${FLUX_CLASSPATH:+:$FLUX_CLASSPATH}`) to
  allow a second extension directory.
- A `.tar.gz` release asset (removes the `unzip` install from the build stage).
- Published `.sha256` files next to release assets (GitHub's digests suffice, but explicit files are easier to cite).
- Arbitrary-uid support (passwd-independent Hadoop user resolution) if OpenShift customers appear.

## 11. Appendices

### A. Environment variables understood by the image

| Variable | Default | Purpose |
|---|---|---|
| `JAVA_OPTS`, `FLUX_OPTS` | unset | Extra JVM options; applied after the image defaults |
| `FLUX_DEFAULT_JVM_OPTS` | `-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8 -Dorg.xerial.snappy.tempdir=/tmp` | Image defaults; empty string restores stock behaviour |
| `FLUX_LOG_LEVEL` | `INFO` | `com.marklogic.flux` / `com.marklogic.spark` log level |
| `FLUX_LOG_FORMAT` | `text` | `json` for ECS JSON lines |
| `FLUX_LOG_DIR` | unset | Per-invocation log file in a mounted directory (stdout unchanged) |
| `SPARK_LOCAL_IP` | `127.0.0.1` | Spark driver bind address |
| `SPARK_LOCAL_DIRS` | unset (`/tmp`) | Spark scratch directory |
| `DJL_CACHE_DIR` | `/tmp/djl.ai` | MiniLM tokenizer cache |
| `HADOOP_USER_NAME` | `flux` | Hadoop's reported user name (does not replace the passwd entry) |
| `HOME` | `/home/flux` | Home directory |
| `FLUX_HOME` | `/opt/flux` | Installation directory |

### B. Facts that corrected earlier assumptions

- The MiniLM model is inside the jar; there is no runtime download to mount.
- `version` starts Spark; nothing in Flux exits 0 without a writable temp directory.
- `help` exits 2, not 0.
- `copyemb distZip` can drop the embedder jars (the delete task is not ordered after the copy); the image never relies
  on that path.
- The reviews in the sibling `fortran` repository are a Claude Code pass over `main@7b71412d`, not Gemini output.

### C. References

- Iron Bank documentation: overview, Dockerfile requirements, hardening manifest, repository structure,
  justifications, ABC and ORA policies, EoL policy, pipeline README — <https://docs-ironbank.dso.mil/>,
  <https://repo1.dso.mil/ironbank-tools/ironbank-pipeline>
- Big Bang Kyverno policies — <https://docs-bigbang.dso.mil/latest/packages/kyverno-policies/docs/policies/>
- Iron Bank MarkLogic Server project — <https://repo1.dso.mil/dsop/progress/marklogicserver/marklogic12>
- Reference Iron Bank Java/CLI images consulted: `sonarsource/sonar-scanner-cli`, `microfocus/fortify/fcli`,
  `opensource/amazon/aws-cli`, `opensource/spark-operator/spark-py`, `opensource/apache/nifi` (and `nifi-fips-bc`),
  `opensource/maven/maven-openjdk-21`
- Flux documentation — <https://marklogic.github.io/flux/>; releases — <https://github.com/marklogic/flux/releases>
- Internal: `ironbank_flux_image_req.md`, Confluence "Iron Bank Overview" and "Iron Bank Compliance Process" summaries
