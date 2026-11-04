#!/bin/bash
# Smoke tests for the locally built Flux container image.
#
# Prerequisites (see CONTRIBUTING.md):
#   docker compose up -d           (MarkLogic test stack from ../docker-compose.yml)
#   ./gradlew -i mlDeploy          (test-app on port 8003)
#   ./gradlew ironbankBuild        (builds the image and stages the embedder jars in ./ext)
#
# Usage: ironbank-local/smoke-test.sh [image]        default image: marklogic-flux-ironbank:local
#
# Every Flux run below uses the hardened settings a restricted Kubernetes cluster applies:
# read-only root filesystem, a single exec-capable tmpfs at /tmp, no capabilities, no privilege
# escalation, uid 1000. Steps marked INFO record behaviour without asserting it.
set -uo pipefail

IMAGE="${1:-${FLUX_IMAGE:-marklogic-flux-ironbank:local}}"
NETWORK="${FLUX_TEST_NETWORK:-docker-tests-flux_default}"
ML_PORT="${FLUX_TEST_ML_PORT:-8003}"

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "${here}/.." && pwd)"
# Git Bash on Windows rewrites /x/y arguments into Windows paths; docker needs them untouched.
if command -v cygpath >/dev/null 2>&1; then
  here="$(cygpath -m "${here}")"
  repo_root="$(cygpath -m "${repo_root}")"
  export MSYS_NO_PATHCONV=1
fi

export_dir="${here}/export"
rm -rf "${export_dir}" && mkdir -p "${export_dir}"
out="$(mktemp)"
trap 'rm -f "${out}"' EXIT

pass=0
fail=0
collection="ib-smoke-$$"

# Join the compose network when the test stack is up; otherwise fall back to the default bridge.
network_args=()
if docker network inspect "${NETWORK}" >/dev/null 2>&1; then
  network_args=(--network "${NETWORK}")
else
  echo "NOTE  docker network ${NETWORK} not found (is the compose stack up?); using the default network"
fi

run() {
  docker run --rm "${network_args[@]}" \
    --read-only --tmpfs /tmp:rw,exec,size=2g \
    --cap-drop ALL --security-opt no-new-privileges \
    -v "${here}/options:/run/secrets/flux:ro" \
    -v "${repo_root}/flux-cli/src/test/resources:/workspace/data:ro" \
    -v "${export_dir}:/export" \
    "$@"
}

# check <expected exit code> <description> [--grep <regex>] -- <docker run args...>
check() {
  local expected="$1" desc="$2"; shift 2
  local pattern=""
  if [[ "$1" == "--grep" ]]; then pattern="$2"; shift 2; fi
  [[ "$1" == "--" ]] && shift
  run "$@" >"${out}" 2>&1
  local rc=$?
  local ok=1
  [[ "${rc}" == "${expected}" ]] || ok=0
  if [[ -n "${pattern}" ]] && ! grep -Eq "${pattern}" "${out}"; then ok=0; fi
  if [[ "${ok}" == 1 ]]; then
    pass=$((pass + 1)); echo "PASS  ${desc} (exit ${rc})"
  else
    fail=$((fail + 1)); echo "FAIL  ${desc}: expected exit ${expected}${pattern:+ and output matching '${pattern}'}, got exit ${rc}"
    sed 's/^/      | /' "${out}" | tail -n 40
  fi
}

info() {
  local desc="$1"; shift
  [[ "$1" == "--" ]] && shift
  run "$@" >"${out}" 2>&1
  echo "INFO  ${desc} (exit $?)"
  sed 's/^/      | /' "${out}" | tail -n 8
}

echo "Image:   ${IMAGE}"
echo "Network: ${NETWORK}"
echo

# --- Image basics ---------------------------------------------------------------------------
check 0 "version prints Flux and Spark versions" --grep "Flux version: .*" -- "${IMAGE}" version
check 0 "version output mentions Spark"           --grep "Spark version: .*" -- "${IMAGE}" version
check 2 "no arguments prints usage and exits 2"   -- "${IMAGE}"
check 2 "help import-files lists its options"     --grep "[-][-]path" -- "${IMAGE}" help import-files
check 2 "abbreviated command import-parq resolves" --grep "Usage: .*import-parquet-files" -- "${IMAGE}" import-parq
check 2 "unknown option exits 2"                  -- "${IMAGE}" version --no-such-option
check 0 "entrypoint passes a shell through"       --grep "^1000$" -- "${IMAGE}" sh -c 'id -u'
check 7 "shell exit code is preserved"            -- "${IMAGE}" sh -c 'exit 7'
check 1 "cannot write under /opt/flux"            -- "${IMAGE}" sh -c 'touch /opt/flux/x'
check 1 "cannot write under /etc"                 -- "${IMAGE}" sh -c 'touch /etc/x'
check 0 "launcher and wrapper are root-owned r-x" --grep "^-rwxr-xr-x.* root +root .*/usr/local/bin/flux$" \
  -- "${IMAGE}" sh -c 'ls -l /opt/flux/bin/flux /usr/local/bin/flux'
check 0 "no jars baked into ext/"                 --grep "^0$" -- "${IMAGE}" sh -c 'ls /opt/flux/ext/*.jar 2>/dev/null | wc -l'
check 0 "bin/ holds only the Unix launcher"       --grep "^1 flux$" -- "${IMAGE}" sh -c 'echo "$(ls /opt/flux/bin | wc -l) $(ls /opt/flux/bin)"'

# --- JVM / logging defaults -----------------------------------------------------------------
check 0 "heap follows the container memory limit (75% of 1g)" --grep "MaxHeapSize *= *8053063[0-9]{2}" \
  -- --memory 1g -e FLUX_OPTS=-XX:+PrintFlagsFinal "${IMAGE}" version
check 0 "FLUX_LOG_FORMAT=json emits JSON lines" --grep '^\{"@timestamp"' \
  -- -e FLUX_LOG_FORMAT=json -e FLUX_LOG_LEVEL=DEBUG "${IMAGE}" version
check 0 "FLUX_LOG_DIR writes one file per invocation" --grep "^1$" \
  -- -e FLUX_LOG_DIR=/tmp/logs "${IMAGE}" sh -c 'flux version >/dev/null 2>&1; ls /tmp/logs | wc -l'
check 0 "two Flux commands in parallel in one container" \
  -- "${IMAGE}" sh -c 'flux version & flux version & wait'

# --- Against the MarkLogic test-app ---------------------------------------------------------
# (shell redirection rather than -o /dev/null: MSYS_NO_PATHCONV leaves /dev/null unconverted for curl.exe)
if ! curl -sf --digest -u admin:admin "http://localhost:${ML_PORT}/v1/search?pageLength=0" >/dev/null; then
  echo "SKIP  MarkLogic test-app not reachable on localhost:${ML_PORT}; skipping data commands"
else
  check 0 "import-files" -- "${IMAGE}" import-files @/run/secrets/flux/test-app.txt \
    --path /workspace/data/mixed-files --collections "${collection}"
  check 0 "export-files" -- "${IMAGE}" export-files @/run/secrets/flux/test-app.txt \
    --collections "${collection}" --path /export/files
  exported="$(find "${export_dir}/files" -type f 2>/dev/null | wc -l | tr -d ' ')"
  if [[ "${exported}" -gt 0 ]]; then
    pass=$((pass + 1)); echo "PASS  export wrote ${exported} files to the mounted volume"
  else
    fail=$((fail + 1)); echo "FAIL  export wrote no files to the mounted volume"
  fi
  check 0 "copy" -- "${IMAGE}" copy @/run/secrets/flux/test-app.txt \
    --collections "${collection}" \
    --output-connection-string "admin:admin@marklogic:${ML_PORT}" --output-collections "${collection}-copy"
  check 0 "reprocess" -- "${IMAGE}" reprocess @/run/secrets/flux/test-app.txt \
    --read-javascript "cts.uris(null, null, cts.collectionQuery('${collection}'))" \
    --write-javascript "declareUpdate(); xdmp.documentAddCollections(URI, '${collection}-reprocessed')"
  check 1 "wrong password fails with exit 1" --grep "Command failed" -- "${IMAGE}" import-files \
    @/run/secrets/flux/bad-password.txt --path /workspace/data/mixed-files --collections "${collection}"

  if ls "${here}"/ext/*.jar >/dev/null 2>&1; then
    check 0 "import with --embedder minilm from mounted ext/" -- -v "${here}/ext:/opt/flux/ext:ro" "${IMAGE}" \
      import-files @/run/secrets/flux/test-app.txt --path /workspace/data/mixed-files/hello.txt \
      --collections "${collection}-embed" --splitter-text --embedder minilm
    check 1 "--embedder minilm without the jar fails cleanly" -- "${IMAGE}" \
      import-files @/run/secrets/flux/test-app.txt --path /workspace/data/mixed-files/hello.txt \
      --collections "${collection}-embed" --splitter-text --embedder minilm
  else
    echo "SKIP  no embedder jars in ${here}/ext (run ./gradlew ironbankStage); skipping embedder tests"
  fi
fi

# --- Informational ----------------------------------------------------------------------------
info "arbitrary uid 12345:0 (OpenShift-style)" -- --user 12345:0 "${IMAGE}" version
info "noexec /tmp (expected to break native libraries)" -- --tmpfs /tmp:rw,noexec,size=1g "${IMAGE}" \
  import-parquet-files @/run/secrets/flux/test-app.txt --path /workspace/data/parquet --count

echo
echo "Passed: ${pass}  Failed: ${fail}"
[[ "${fail}" == 0 ]]
