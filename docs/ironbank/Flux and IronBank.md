# Flux and IronBank

> **Superseded (2026-11-05):** the design that was actually built is in [container-design.md](container-design.md)
> and `ironbank/`. This Copilot draft is kept for history; it conflicts with Iron Bank rules in four places
> (labels in the Dockerfile, `/app` layout, `chmod -R g=u`, `CMD ["--help"]`).

Did some chatting with Teams Copilot on what an “IronBank-hardened container image” would look like for Flux. Here’s the spec we came up with:

# MarkLogic Flux – Iron Bank Hardened Container Spec

## 1. Overview

**Artifact:** `flux:ironbank` container image  
**Type:** CLI / batch execution container  
**Purpose:** Secure, compliant execution of Flux import/export commands in DoD environments

### Key Characteristics

- No network services / no exposed ports
- Ephemeral execution model
- Non-root runtime
- Minimal attack surface
- Fully argument-driven CLI interface

---

## 2. Filesystem Layout in Container

Flux distribution is preserved as-is:

```
/app
  ├── bin/
  │     └── flux            # main launcher script
  ├── lib/
  ├── conf/
  └── (other Gradle outputs)

Optional runtime workspace:
/workspace                 # user-mounted working directory
/tmp                       # temporary runtime storage
```

---

## 3. Container Runtime Contract

### Invocation Model

The container MUST behave like the native CLI:

Shell

docker run flux:ironbank \<flux-args\>

Show more lines

### Entrypoint

Dockerfile

ENTRYPOINT \["/app/bin/flux"\]

CMD \["--help"\]

Show more lines

### Example Usage

#### Basic command

Shell

docker run --rm flux:ironbank import --host [db.example.com](http://db.example.com)

Show more lines

#### With file input

Shell

docker run --rm \\

-v $(pwd):/workspace \\

flux:ironbank \\

import \\

--input /workspace/data.json

Show more lines

---

## 4. Base Image Requirements

### Approved base

- Red Hat UBI 9 minimal or OpenJDK runtime:
    - `registry.access.redhat.com/ubi9/openjdk-17-runtime`
- OR Iron Bank–provided base image

### Constraints

- No full OS images (e.g., Ubuntu, Debian)
- No unnecessary utilities (curl, wget, compilers)
- Only runtime dependencies allowed

---

## 5. Security Hardening Requirements

### 5.1 Non-root execution

Dockerfile

RUN useradd -u 1001 fluxuser

USER 1001

Show more lines

- No root at runtime
- All writable dirs owned by UID 1001

---

### 5.2 File permissions

Shell

chown -R 1001:0 /app

chmod -R g=u /app

\`\`

Show more lines

- Group-writable instead of world-writable
- No SUID/SGID binaries

---

### 5.3 Read-only root filesystem compatibility

Container MUST support:

Shell

--read-only --tmpfs /tmp

Show more lines

Implications:

- Flux must not write into `/app`
- Temp files must go to:
    - `/tmp`, or
    - user-specified directory

---

### 5.4 No package manager residue

Remove:

- `yum`, `dnf`, caches, metadata

Ensure:

- Clean filesystem layers
- No leftover build tools

---

## 6. Flux Application Behavior Requirements

### 6.1 Non-interactive operation

Flux MUST:

- Fully operate via CLI args
- Never prompt interactively (stdin)

---

### 6.2 Config flexibility

Must support:

- CLI overrides ✅ (preferred)
- File-based config via mounted volumes ✅
- Environment variables ✅ (optional)

Example:

Shell

docker run -v $(pwd)/conf:/config flux:ironbank \\

import --config /config/flux.properties

Show more lines

---

### 6.3 File system expectations

| Path | Purpose | Writable |
| --- | --- | --- |
| `/app` | Application | ❌ |
| `/workspace` | User data | ✅ (mounted) |
| `/tmp` | Temp | ✅ |

Flux must NOT:

- Write under `/app`
- Assume relative paths exist without mounts

---

### 6.4 Exit codes

| Code | Meaning |
| --- | --- |
| 0 | Success |
| non-zero | Failure |

Critical for automation pipelines.

---

## 7. Build Process Requirements

### 7.1 Multi-stage build (recommended)

Example pattern:

Dockerfile

FROM gradle:8 AS builder

COPY . /src

RUN ./gradlew installDist

FROM [registry.access.redhat.com/ubi9/openjdk-17-runtime](http://registry.access.redhat.com/ubi9/openjdk-17-runtime)

WORKDIR /app

COPY --from=builder /src/build/install/flux/ /app/

\`\`

Show more lines

---

### 7.2 Image contents

Allowed:

- Flux distribution
- Java runtime
- minimal required OS libs

Not allowed:

- source code
- build tools
- test artifacts

---

## 8. Vulnerability & Compliance

### 8.1 CVE requirements

- Zero Critical vulnerabilities
- Zero High vulnerabilities (or waiver required)
- Continuous scanning (Grype/Anchore)

---

### 8.2 SBOM

Must include:

- Full dependency inventory
- Generated with Syft or equivalent

---

### 8.3 Supply chain integrity

- Image must be:
    - Reproducible (preferred)
    - Signed (Cosign)
    - Traceable to source

---

## 9. Metadata & Labels

Include standard OCI + DoD labels:

Dockerfile

LABEL org.opencontainers.image.title="MarkLogic Flux"

LABEL org.opencontainers.image.description="CLI tool for MarkLogic data import/export"

LABEL org.opencontainers.image.version="X.Y.Z"

LABEL org.opencontainers.image.vendor="MarkLogic"

\`\`

Show more lines

---

## 10. Runtime Usage Patterns (Documented)

### Required documentation must include:

#### 10.1 Basic usage

Shell

docker run --rm flux:ironbank --help

Show more lines

#### 10.2 File-based workflows

Shell

docker run --rm \\

-v $(pwd):/workspace \\

flux:ironbank export --dir /workspace/out

Show more lines

#### 10.3 Secure execution flags

Shell

docker run --rm \\

--read-only \\

--tmpfs /tmp \\

-u 1001 \\

-v $(pwd):/workspace \\

flux:ironbank import ...

Show more lines

---

## 11. What is Explicitly NOT Required

Because Flux is a CLI:

- ❌ No exposed ports
- ❌ No HTTP endpoints
- ❌ No health checks
- ❌ No long-running process
- ❌ No service orchestration

---

## 12. Acceptance Criteria

The container is considered “Iron Bank ready” when:

✅ Runs as non-root  
✅ No High/Critical CVEs  
✅ Minimal base image used  
✅ No shell required for operation  
✅ Supports read-only filesystem  
✅ Works via `docker run flux:ironbank <args>`  
✅ Fully non-interactive  
✅ SBOM generated  
✅ Signed and documented

---

# ✅ Bottom Line

For Flux, an Iron Bank image is:

> A **minimal, locked-down, non-root, scan-clean container that executes **`/app/bin/flux`** with user-provided arguments and nothing else.**

It behaves like a **drop-in CLI wrapper**, not a service—and that’s exactly what Iron Bank expects for tools like this.
