# IronBank-Hardened MarkLogic Flux CLI for DoD Deployments

**Epic:** [MLE-27464](https://progresssoftware.atlassian.net/browse/MLE-27464)

## Problem statement

MarkLogic Flux, the CLI used for ingestion, embeddings, export, copy, and reprocessing, is not available as an
**IronBank-certified container image**. DoD environments generally permit only IronBank-approved images. MarkLogic
Server is already in IronBank, but Flux is not, which blocks a fully compliant MarkLogic stack. As MarkLogic moves to a
unified PDP SKU, customers expect *all* key components to be IronBank-compliant, not only the database.

Images approved in IronBank have **DoD-wide reciprocity**: they are trusted across classifications and can be deployed
rapidly, which reduces the time and cost for customers to obtain an Authority to Operate (ATO). Without a Flux image,
customers lose that benefit for the ingestion and embeddings part of their stack.

## Personas

- DoD DevSecOps engineers and platform teams
- Application developers on defense programs (using Flux for ingestion and embeddings)
- Cybersecurity, ATO, and RMF teams
- Federal system integrators and solution architects
- Progress Federal sales and SEs

## Desired outcome

- The Flux CLI is available as an IronBank-hardened image.
- DevSecOps teams can invoke Flux as an **ephemeral container**: it runs one command and then exits.
- Customers can assemble a fully IronBank-based stack: MarkLogic Server + Flux (and later Data Hub/FastTrack).
- Each future Flux release ships with a corresponding IronBank image, submitted on the day of public release.
- Customers benefit from IronBank's DoD-wide reciprocity, which shortens their ATO process for Flux-based solutions.

## Scope

- **In scope:** the MarkLogic Flux CLI image only.
- **Out of scope:** MarkLogic Server, Data Hub, and FastTrack images, which are handled separately.
- **Assumptions:**
  - The IronBank base images and hardening pattern used for MarkLogic Server are reused.
  - Architecture (James) finalizes base image specifics.
  - The Kube Ninjas team owns the existing build and publish procedures for Docker Hub and IronBank.
  - The image aims for functional parity with the standard Flux CLI.

## Part 1: Image design and behaviour

> **Status: draft for discussion.** The team has not yet discussed what the image will look like or how it will
> function. Each area below gives a proposed starting point, based on how Flux works today, and the questions the team
> needs to answer. Nothing here is decided.

### 1.1 Usage model

**Proposed starting point**
- The image is a **CLI tool, not a service**. Each container runs one Flux command and then exits.
- Arguments passed to the container are passed straight to Flux:

  ```
  docker run --rm <registry>/marklogic/flux:<version> import-files \
    --path /data/input \
    --connection-string "user:password@marklogic-host:8000"
  ```

- In Kubernetes, the image runs as a `Job` (or `CronJob` for scheduled loads), not as a `Deployment`.

**Questions**
- Are there use cases beyond one-off commands, such as running Flux on a schedule, or running several commands in one
  container through a script?
- Should the image also support `spark-submit` against an external Spark cluster, or only the standalone CLI?
- Which Flux commands must be validated for the first release (for example `import-files`, `import-jdbc`,
  `export-files`, `copy`, `reprocess`, and embedding generation)?

### 1.2 Base image and Java runtime

**Proposed starting point**
- Use an IronBank-approved base image that includes Java 17 (for example, a UBI9 OpenJDK 17 image), or the same base as
  the MarkLogic Server IronBank image with Java 17 added.

**Questions**
- Which exact base image? (Architecture and the Kube Ninjas)
- JDK or JRE? A JRE-only image is smaller and has fewer findings.
- Should the Flux image track the same base image updates as the MarkLogic Server image?

### 1.3 Image contents and layout

**Proposed starting point**

| Path | Contents |
|---|---|
| `/opt/flux/bin/flux` | Flux start script (the entrypoint) |
| `/opt/flux/lib/` | Flux and dependency jars, plus `native/libhadoop.so` |
| `/opt/flux/ext/` | Optional extension jars, such as embedding models (see 1.8) |
| `/opt/flux/conf/` | `log4j2.properties` and other config on the classpath |
| `/opt/flux/LICENSE.txt`, `NOTICE.txt` | License files |

- Remove Windows-only files (`hadoop.dll`, `winutils.exe`, `msvcr*.dll`) and anything else not needed on Linux.

**Questions**
- Should the image contain anything besides Flux (for example, a shell for debugging)? Fewer tools means fewer findings
  but harder troubleshooting.
- Are there Flux dependencies that can be safely excluded to reduce size and findings (for example, cloud storage SDKs
  if S3/Azure are not needed)?

### 1.4 Entrypoint and user

**Proposed starting point**
- `ENTRYPOINT ["/opt/flux/bin/flux"]` with no default `CMD`, or `CMD ["help"]` so running the image with no arguments
  prints usage.
- Run as a fixed non-root user and group (for example UID/GID `1001`), with a working directory such as `/home/flux`.
- `HEALTHCHECK NONE`, with a written justification: the container is short-lived.

**Questions**
- What UID/GID should be used? Should it match the MarkLogic Server image's conventions?
- Should running with no arguments print help or the version?
- Does OpenShift-style support for an arbitrary UID need to be considered?

### 1.5 Configuration and secrets

**How Flux works today**
- Flux is configured entirely through command-line options.
- Flux supports **options files** (`@options.txt`), which keep credentials and long options off the command line.
- JVM options can be passed with the `JAVA_OPTS` or `FLUX_OPTS` environment variables, which the start script already
  reads.

**Proposed starting point**
- Credentials (MarkLogic password, JDBC password, S3/Azure keys, embedding API keys) are supplied through an options
  file mounted from a secret, for example `/run/secrets/flux-options.txt`, and passed as `@/run/secrets/flux-options.txt`.
- Do not bake any credentials or default connection settings into the image.

**Questions**
- Is the options-file approach acceptable to DoD and security teams, or is environment-variable support needed?
  Flux options are not read from environment variables today, so that would need new Flux code or an entrypoint wrapper.
- Is there a standard secrets pattern in target environments (Kubernetes `Secret`, Vault, mounted files) that the docs
  should show?

### 1.6 Data input and output

**Proposed starting point**
- Use standard mount points:

  | Mount point | Purpose |
  |---|---|
  | `/data/input` | Files to import |
  | `/data/output` | Exported files |
  | `/tmp` (or a dedicated scratch path) | Spark temp files |

- Mounted directories must be readable or writable by the container's non-root user.
- Cloud storage (S3, Azure) and JDBC sources work over the network as they do today.

**Questions**
- Should the image define standard mount points, or let users choose their own paths?
- Which data sources and destinations matter most in DoD environments: local files, S3, Azure, JDBC?

### 1.7 Security and TLS

**Proposed starting point**
- Support connecting to MarkLogic over TLS, with truststores and keystores mounted from secrets (for example under
  `/opt/flux/certs` or `/run/secrets`).
- Support running with a **read-only root filesystem**, a writable temp volume, all capabilities dropped, and no
  privilege escalation.

**Questions**
- Which MarkLogic authentication types must be validated in the image? Flux supports basic, digest, certificate
  (two-way SSL), Kerberos, SAML, OAuth, and MarkLogic Cloud.
- Are FIPS-validated crypto or FIPS mode required in target environments? This could affect the base image and Java
  configuration.
- Should the image trust a set of DoD root CAs by default, or rely on users mounting their own truststore?

### 1.8 Embedding models and extensions

**How Flux works today**
- Embedding integrations (MiniLM, Ollama, Azure OpenAI) are separate jars added to `ext/`. They are not in the default
  Flux zip.
- MiniLM runs locally but is ~200 MB. Ollama and Azure OpenAI call external services.

**Options**

| Option | Pros | Cons |
|---|---|---|
| A. Include no embedding jars | Smallest image, fewest findings | No embedding support out of the box |
| B. Include all embedding jars | Full parity with Flux | Much larger image, more findings |
| C. Include only MiniLM | Works in disconnected environments | ~200 MB larger |
| D. Let users mount jars into `/opt/flux/ext` | Flexible, keeps image small | Mounted jars are not IronBank-scanned |

**Questions**
- Which option? Does mounting unscanned jars defeat the purpose of an IronBank image?
- Are there other extension points (custom splitters, classifiers, JDBC drivers) users will need to add? JDBC drivers
  are not bundled with Flux today, so `import-jdbc` and `export-jdbc` need a driver jar from somewhere.

### 1.9 Runtime behaviour

**Proposed starting point**
- **Exit codes:** Flux already exits with code 0 on success and non-zero on failure, so jobs and pipelines can detect
  failures.
- **Logging:** all logs go to stdout/stderr, configured through `conf/log4j2.properties`. No log files are written in
  the container.
- **Memory:** JVM heap is set with `JAVA_OPTS`/`FLUX_OPTS` (for example `-Xmx4g`), and the JVM respects container memory
  limits.
- **Spark:** runs in local mode inside the container, using the container's CPU allocation. Temp files go to `/tmp` or
  a path set with `--spark-conf spark.local.dir=...`.
- **Networking:** Spark binds to a local address, which may need `SPARK_LOCAL_IP=127.0.0.1` or
  `spark.driver.bindAddress` (see the Flux FAQ).

**Questions**
- Should the image set default JVM memory options (for example `-XX:MaxRAMPercentage`), or leave sizing to users?
- Should the image set `SPARK_LOCAL_IP` by default to avoid hostname issues in containers?
- Is structured (JSON) logging required for log aggregation in target environments?

### 1.10 Networking and disconnected environments

**Proposed starting point**
- Core Flux features (import/export with MarkLogic, local files, JDBC on the same network) work without internet
  access.
- Features that call external services (Azure OpenAI, Ollama, S3, Azure Storage, Semaphore classifier) need network
  access to those services.

**Questions**
- What does "functional parity" mean in classified environments where external services are unavailable?
- Should the docs list which Flux features work fully offline?

### 1.11 Image tags and versioning

**Proposed starting point**
- Image tags match the Flux version (for example `<major>.<minor>.<patch>`), with a `latest` tag if IronBank allows it.

**Questions**
- Do tags need to include the base image version, or is the Flux version enough?
- Does the IronBank image need to match a Docker Hub image exactly?

### 1.12 Summary of decisions needed for the image design

| # | Decision | Owner (proposed) |
|---|---|---|
| 1 | Usage model and commands to validate | Flux team |
| 2 | Base image, JDK vs JRE | Architecture, Kube Ninjas |
| 3 | Image contents and exclusions | Flux team |
| 4 | Entrypoint, UID/GID | Flux team, Kube Ninjas |
| 5 | Configuration and secrets pattern | Flux team, Security |
| 6 | Standard mount points | Flux team |
| 7 | TLS, authentication, and FIPS requirements | Flux team, Security |
| 8 | Embedding models and extensions (including JDBC drivers) | Flux team, Product |
| 9 | JVM, Spark, and logging defaults | Flux team |
| 10 | Offline feature scope | Product |
| 11 | Tag scheme | Kube Ninjas |

## Part 2: IronBank process and delivery plan

The rest of this document covers the technical background, IronBank compliance requirements, and the phased plan for
building, approving, and maintaining the image.

## Technical context from the Flux codebase

- **Runtime:** Java 17. Flux runs Apache Spark in-process (local mode) through the MarkLogic Spark connector.
- **Distribution:**
  - `./gradlew distZip` produces `marklogic-flux-<version>.zip`, containing `bin/flux`, `lib/*.jar`,
    `lib/native/libhadoop.so`, `ext/`, and the Windows-only `hadoop.dll`/`winutils.exe`/`msvcr*.dll`.
  - The required JVM `--add-opens` flags are already in the generated start script (`applicationDefaultJvmArgs` in
    `flux-cli/build.gradle`).
- **Entry point:** `com.marklogic.flux.cli.Main`, which the `bin/flux` start script invokes.
- **Embedding models:** the integrations (MiniLM, Ollama, Azure OpenAI) are separate jars dropped into `ext/`. They are
  not in the default zip; the `copyEmbeddingModelJarsIntoDistribution` task adds them.
- **Writable storage:** Spark needs a writable temp directory (`/tmp` or `spark.local.dir`).
- **CVE pinning:** dependency versions are pinned in the root `build.gradle` (Netty, Jackson, log4j, libthrift, Janino)
  and in `gradle.properties` (`hadoopVersion`, `awssdkVersion`, and others). Scan findings are mostly fixed here.
- **No Dockerfile today:** the repo has no Dockerfile. IronBank artifacts live in a separate Repo1 project.
- **Snapshot dependency:** Flux depends on a snapshot of the MarkLogic Spark connector (`connectorVersion` in
  `gradle.properties`). That coupling can slow down an urgent Flux patch release.

## Flux release artifacts used by the image

Each Flux release on [GitHub Releases](https://github.com/marklogic/flux/releases) publishes the artifacts below. Each
has a stable download URL, and GitHub provides a sha256 digest for each one, so they can be referenced directly in
`hardening_manifest.yaml`. Which release the image is built from will be decided later.

| Asset | Approx. size | Needed for the image |
|---|---|---|
| `marklogic-flux-<version>.zip` | ~280 MB | Yes: the Flux distribution |
| `flux-embedding-model-minilm-<version>-all.jar` | ~200 MB | Optional (see open question 2) |
| `flux-embedding-model-azure-open-ai-<version>-all.jar` | ~14 MB | Optional |
| `flux-embedding-model-ollama-<version>-all.jar` | <1 MB | Optional |
| `marklogic-flux-<version>-all.jar` | ~30 MB | No: the shadow jar for spark-submit |
| `marklogic-flux-getting-started-<version>.zip` | <1 MB | No |

## IronBank compliance requirements

These requirements come from the IronBank documentation and the internal Confluence summaries listed under
References.

- **Acceptance Baseline Criteria (ABC):** every container must meet all of the following:
  - It is rebased onto an IronBank base image.
  - The build runs without internet access. All resources are declared up front with sha256 checksums.
  - The application is supported by a vendor, an open source community, or a government entity.
  - It uses the latest release series for the application and all of its dependencies.
- **Scanning:**
  - Images are scanned with Prisma Cloud (formerly Twistlock), Anchore, OpenSCAP, and Rapidfort.
  - OpenSCAP checks STIG and NIST-based compliance.
  - Scanning is largely automated when approved components are used. Non-standard components and findings need manual
    review.
- **Overall Risk Assessment (ORA):** each container receives an ORA score that reflects project health and open
  vulnerabilities.
- **Body of Evidence (BoE):** the findings, justifications, and compliance evidence that DoD authorizing officials review
  for each image.
- **Continuous monitoring:** approved images are rescanned about every 12 hours. New findings must be addressed
  promptly.
- **Remediation deadlines (ABC):**

  | Severity | Fix or justify within |
  |---|---|
  | Critical | 5 days |
  | High | 10 days |
  | Medium | 30 days |
  | Low | 60 days |

  Justifications for unresolved findings are submitted through the Vulnerability Assessment Tracker (VAT).
- **Release timing:** updates must be submitted on the day of public release.
- **Runtime target:** images are intended to run on Kubernetes clusters that follow the DoD DevSecOps Reference
  Design.
- **Access:**
  - Repo1 and Registry1 are publicly accessible at IL2, and registration does not require a CAC.
  - IronBank does not manage licensing, which is not a concern because Flux is licensed under Apache 2.0.

## Phase overview

| Phase | Name | Stories |
|---|---|---|
| 1 | Discovery and onboarding | 1.1, 1.2 |
| 2 | Image build | 2.1, 2.2, 2.3 |
| 3 | Local validation | 3.1 |
| 4 | IronBank pipeline and approval | 4.1, 4.2 |
| 5 | End-to-end validation and documentation | 5.1, 5.2 |
| 6 | Ownership and lifecycle | 6.1, 6.2, 6.3 |

---

## Phase 1: Discovery and onboarding

### Story 1.1: Agree the IronBank approach with the Kube Ninjas and Architecture

**Description**
- Meet the Kube Ninjas to get their IronBank and Docker Hub build and publish procedures.
- Agree with Architecture (James) on the IronBank base image. Candidates: the UBI9 + OpenJDK 17 image, or the same UBI
  base as the MarkLogic Server image.
- Decide whether the image bundles the embedding-model jars, and which ones.
- Confirm whether a matching Docker Hub image is also published.
- Review the Part 1 image design with the team and agree the decisions listed in section 1.12.

**Acceptance criteria**
- The following decisions are documented in Confluence or the epic:
  - base image
  - image contents
  - Repo1 project path
  - owners
  - Docker Hub plan
- The Part 1 image design is updated from "draft" to "agreed", with each decision in section 1.12 resolved.

### Story 1.2: Onboard to IronBank / Repo1

**Description**
- Complete IronBank contributor onboarding: accounts, Repo1 project request, maintainers.

**Acceptance criteria**
- The Flux Repo1 project exists (for example `dsop/progress/marklogic/flux`).
- Team members have the access they need to open merge requests and run the pipeline.

**Depends on:** 1.1

---

## Phase 2: Image build

### Story 2.1: Write a hardened Dockerfile for the Flux CLI

**Description**
- `FROM` the approved IronBank base image, using `ARG BASE_REGISTRY`, `ARG BASE_IMAGE`, and `ARG BASE_TAG`.
- Install the Flux distribution under `/opt/flux` and remove the Windows-only binaries.
- Create and switch to a non-root `USER`. Provide a writable temp directory for Spark.
- Set `ENTRYPOINT ["/opt/flux/bin/flux"]` so arguments to `docker run` become Flux commands.
- Set `HEALTHCHECK NONE` with a documented justification: the image is a short-lived CLI, not a service.
- Keep packages to a minimum and lock down file permissions, following the IronBank Dockerfile requirements.

**Acceptance criteria**
- The image builds locally.
- `docker run --rm <image> version` prints the Flux version and exits with code 0.
- `docker run --rm <image> help` lists the Flux commands.
- The container runs as non-root.

**Depends on:** 1.1

### Story 2.2: Write `hardening_manifest.yaml`, the README, and the LICENSE

**Description**
- In `hardening_manifest.yaml`:
  - Declare the image name, tags, labels, and maintainers.
  - List every external resource (the Flux release zip and any embedding jars) with a URL and sha256. The IronBank
    build has no internet access.
- Add the README (usage) and LICENSE required by IronBank.

**Acceptance criteria**
- The manifest passes IronBank lint and validation.
- The README documents basic ephemeral usage.

**Depends on:** 1.2, 2.1

### Story 2.3: Make Flux release artifacts IronBank-friendly (Flux repo)

**Description**
- Releases already publish stable download URLs, and GitHub provides a sha256 digest for each asset (see "Flux release
  artifacts used by the image"). Confirm this is enough for `hardening_manifest.yaml`, or also publish a checksum file
  with each release.
- Optionally provide a Linux-only distribution variant without the Windows binaries.
- Optionally provide a slimmer distribution: the zip is ~280 MB, and a smaller image means fewer scan findings.

**Acceptance criteria**
- The release process produces artifacts and checksums that `hardening_manifest.yaml` can reference.

**Depends on:** 1.1 (runs in parallel with 2.1 and 2.2)

---

## Phase 3: Local validation

### Story 3.1: Validate the container locally against MarkLogic

**Description**
- Use the repo's `docker-compose.yml` (MarkLogic, Postgres, Ollama) and the deployed `test-app`.
- Run these commands through the container:
  - `import-files`
  - `import-jdbc` (Postgres `dvdrental`)
  - `export-files`
  - `copy`
  - `reprocess`
  - an import using `--embedder` (Ollama), if embedders are in the image
- Test with:
  - volume mounts for input and output
  - an `@options-file` for credentials
  - TLS truststores mounted into the container
  - `--read-only` root filesystem with a tmpfs `/tmp`
  - a non-root user

**Acceptance criteria**
- Each command succeeds and the container exits with code 0.
- Failures produce a non-zero exit code.
- Any gaps or limitations are recorded and fed into Phase 5 docs.

**Depends on:** 2.1

---

## Phase 4: IronBank pipeline and approval

### Story 4.1: Submit to the IronBank pipeline and fix scan findings

**Description**
- Run the Repo1 pipeline. Its scanners are Prisma Cloud (formerly Twistlock), Anchore, OpenSCAP (STIG and NIST
  checks), and Rapidfort.
- Expect most findings from Spark, Hadoop, Netty, Jackson, and the AWS/Azure SDKs.
- For each finding, do one of the following:
  - Fix it by bumping the dependency pins in the Flux root `build.gradle` or `gradle.properties` and releasing a patch.
  - Write a justification in VAT (Vulnerability Assessment Tracker). These justifications become part of the Body of
    Evidence that authorizing officials review, so write them for that audience.

**Acceptance criteria**
- The pipeline passes.
- Every finding is fixed or justified and accepted in VAT.
- The image achieves an Overall Risk Assessment (ORA) score acceptable to the IronBank team.

**Depends on:** 2.2, 3.1

### Story 4.2: Get the image approved and signed

**Description**
- Work with IronBank reviewers through approval.

**Acceptance criteria**
- The image is approved, signed, and pullable from `registry1.dso.mil` with a tag matching the Flux version.

**Depends on:** 4.1

---

## Phase 5: End-to-end validation and documentation

### Story 5.1: Validate a CI/CD job in a DoD-like environment

**Description**
- IronBank images are intended to run on Kubernetes clusters that follow the DoD DevSecOps Reference Design, so the
  primary reference is a Kubernetes `Job`. A GitLab CI job is a secondary example.
- The reference job does the following:
  1. Pulls the Flux image from IronBank.
  2. Runs a real ingestion or embedding command against a MarkLogic environment, preferably the MarkLogic Server
     IronBank image.
  3. Completes, with the container terminating on its own.
- The `Job` follows hardened settings: non-root, read-only root filesystem with an `emptyDir` for temp files,
  credentials from a Kubernetes `Secret`, and TLS material mounted from a `Secret`.

**Acceptance criteria**
- The Kubernetes `Job` runs successfully end to end and reaches `Completed` status.
- The job definitions (Kubernetes `Job` and GitLab CI job) are committed as reference examples.

**Depends on:** 4.2

### Story 5.2: Write the Flux docs for the IronBank image

**Description**
- Add a Flux docs page and a release-notes entry covering:
  - IronBank image coordinates and tags
  - ephemeral `docker run` examples, the Kubernetes `Job` reference from story 5.1, and a CI example
  - volume, env var, `@options-file`, TLS truststore, and secrets patterns for secure environments
  - networking caveats for embedders and cloud storage (S3/Azure) in disconnected environments
  - a reference architecture: MarkLogic Server + Flux, both from IronBank (optionally Data Hub/FastTrack)

**Acceptance criteria**
- The docs are published with the Flux release that introduces the image.

**Depends on:** 3.1, 5.1

---

## Phase 6: Ownership and lifecycle

### Story 6.1: Define the versioning and support policy (internal)

**Description**
- Decide:
  - which Flux versions get IronBank images
  - how long each is supported
  - the rebuild cadence when the base image updates
  - who triages scan findings
- The policy must reflect IronBank's obligations:
  - Updates are submitted on the day of each public Flux release.
  - Only the latest release series is expected to be maintained.
  - Findings are remediated or justified within the ABC deadlines: Critical in 5 days, High in 10, Medium in 30,
    Low in 60.
  - IronBank rescans approved images about every 12 hours.

**Acceptance criteria**
- An internal policy page is reviewed and approved by Product Management.
- The policy names an owning team and states the remediation deadlines and the same-day release obligation.

**Depends on:** 1.1

### Story 6.2: Make the IronBank update a mandatory release step

**Description**
- Add a **mandatory** step to the Flux release checklist: on the day of each public Flux release, open the
  IronBank merge request that updates the Repo1 manifest (version, URL, sha256).
- Automate the manifest update where possible, for example a script that computes the sha256 of the release artifacts
  and updates `hardening_manifest.yaml`.
- Assign owners for IronBank merge requests.

**Acceptance criteria**
- The Flux release checklist includes the IronBank step.
- The next Flux release after approval has its IronBank merge request opened on the day of public release.

**Depends on:** 4.2, 6.1

### Story 6.3: Set up continuous monitoring and scan-finding triage

**Description**
- IronBank rescans approved images about every 12 hours, so new findings can appear at any time, not only at release.
- Set up:
  - an owner or rotation for triaging new findings
  - alerting or a regular review of IronBank/VAT findings for the Flux image
  - a VAT justification workflow, including templates for common cases (for example, a vulnerable code path that
    Flux does not use)
  - a fast patch-release path for Critical and High CVEs: bump pins in the root `build.gradle` or `gradle.properties`,
    cut a Flux patch release, and update the IronBank merge request within the ABC deadline
- Document how the fast patch path deals with Flux's dependency ona a MarkLogic Spark connector snapshot
  (`connectorVersion`), for example by requiring a released connector version for IronBank builds.

**Acceptance criteria**
- The triage owner and alerting are in place.
- A runbook documents the triage, justification, and patch-release steps.
- A dry run (a simulated Critical finding) shows the team can ship a fix or justification within 5 days.

**Depends on:** 4.2, 6.1

---

## Dependency graph

| Story | Depends on |
|---|---|
| 1.1 Agree approach | — |
| 1.2 Onboard to IronBank | 1.1 |
| 2.1 Dockerfile | 1.1 |
| 2.2 Hardening manifest | 1.2, 2.1 |
| 2.3 Release artifacts | 1.1 |
| 3.1 Local validation | 2.1 |
| 4.1 Pipeline and findings | 2.2, 3.1 |
| 4.2 Approval and signing | 4.1 |
| 5.1 CI/CD (Kubernetes `Job`) validation | 4.2 |
| 5.2 Documentation | 3.1, 5.1 |
| 6.1 Versioning and support policy | 1.1 |
| 6.2 Mandatory release step | 4.2, 6.1 |
| 6.3 Monitoring and triage | 4.2, 6.1 |

Critical path: `1.1 → 1.2 → 2.2 → 4.1 → 4.2 → 5.1 → 5.2`

## Risks

- **Scan findings:** Spark, Hadoop, and the AWS/Azure SDKs bring in many transitive dependencies. Remediation in story
  4.1 is likely the longest item.
- **Ongoing remediation workload:**
  - IronBank's 12-hour rescans and ABC deadlines (Critical 5 days, High 10 days) create a permanent maintenance load,
    not a one-time effort.
  - Missing deadlines worsens the ORA score and can put the image's approval at risk.
- **Patch-release speed:** Flux depends on a MarkLogic Spark connector snapshot. If a fix needs a new connector
  release, meeting the 5-day Critical deadline may be hard.
- **Latest release series:** IronBank expects the latest release series. If a new Flux release ships during the approval
  process, the submission may need to move to it.
- **Image size:** bundling embedding models (especially MiniLM) significantly increases size and the number of
  findings.
- **Network-restricted environments:** embedders (Azure OpenAI, Ollama) and cloud storage (S3, Azure) need network
  access that classified environments may not allow, which limits "functional parity."
- **Read-only filesystems:** Spark's temp-file needs must be handled for hardened runtime settings.
- **Ticket wording:** the epic's example commands ("ingestion list / ingestion run / embeddings operation") are not
  Flux commands. The acceptance criteria should use real commands such as `import-files`, `import-jdbc`, `copy`,
  `reprocess`, and `import-* --embedder`.

## Main questions to answer before implementation

1. **What base image should Flux use?** The docs say to use an approved IronBank base image, but do not specify which
   one. Confirm with Architecture and the Kube Ninjas.
2. **Where will the image be built and maintained?** Get the Kube Ninjas' Repo1 path, access requirements, and
   build/publish procedures. The ticket specifically says to coordinate with them.
3. **What should the image include?** Decide whether embedding-model jars are in scope, and which ones. This affects
   the image size, dependencies, and scan work.
4. **Where should the Dockerfile and test examples live?** The plan proposes a separate Repo1 project, but whether a
   reference Dockerfile or local test harness should also live in the Flux repo still needs team agreement.
5. **Who owns ongoing scan findings and releases?** Assign who monitors findings, submits VAT justifications, handles
   urgent fixes, and opens the IronBank update for each Flux release.

The compliance summaries cover the general requirements - approved base image, disconnected builds, scanning,
remediation deadlines, and release updates - but not these Flux-specific decisions. The release version can stay
undecided for now.

### Needed before release and long-term support

1. **Release artifacts:** Are the Flux release artifact URLs and sha256 digests acceptable to the IronBank build
   process? Is a slimmer Linux-only distribution needed?
2. **Ongoing ownership:** Which team owns the 12-hour scan monitoring, VAT justifications, vulnerability remediation
   deadlines, urgent fixes, and release-day IronBank updates?
3. **Spark connector version:** Can IronBank builds use a released MarkLogic Spark connector version? This affects
   whether urgent Flux security patches can be released without depending on a connector snapshot.
4. **Docker Hub:** Is publishing a matching non-IronBank Docker Hub image part of this effort? Confirm with the Kube
   Ninjas.

## References

- IronBank overview: <https://docs-ironbank.dso.mil/overview/>
- Dockerfile requirements: <https://docs-ironbank.dso.mil/hardening/dockerfile-requirements/>
- Choosing base images: <https://docs-ironbank.dso.mil/hardening/choosing-base-image/>
- Contributor onboarding: <https://docs-ironbank.dso.mil/quickstart/contributor-onboarding/>
- Pipeline details: <https://docs-ironbank.dso.mil/quickstart/pipeline/>
- FAQ: <https://docs-ironbank.dso.mil/faq/>
- Acceptance Baseline Criteria (ABC): <https://docs-ironbank.dso.mil/reference/abc/>
- IronBank components: <https://docs-ironbank.dso.mil/quickstart/components/>
- Internal: [Iron Bank Overview: Documentation Summary](https://progresssoftware.atlassian.net/wiki/spaces/ENGINEERING/pages/2313617476/Iron+Bank+Overview+Documentation+Summary)
- Internal: Iron Bank Compliance Process: Summary (Confluence, ENGINEERING space)
