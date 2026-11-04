#!/bin/bash
# Runs the Flux image as Kubernetes Jobs (the deployment model Iron Bank images are meant for)
# against a throw-away MarkLogic pod, on whatever cluster `kubectl` currently points at
# (Rancher Desktop's k3s, kind, ...). The image must be visible to the cluster's container
# runtime: with Rancher Desktop in dockerd mode a locally built image is (imagePullPolicy: Never).
#
# Usage: ironbank-local/k8s-smoke.sh [--keep]
#   FLUX_IMAGE        image to run (default marklogic-flux-ironbank:local)
#   MARKLOGIC_IMAGE   MarkLogic image (default progressofficial/marklogic-db:12.1.0-ubi9-rootless-2.3.1)
#   --keep            leave the flux-smoke namespace in place for inspection
set -uo pipefail

export FLUX_IMAGE="${FLUX_IMAGE:-marklogic-flux-ironbank:local}"
export MARKLOGIC_IMAGE="${MARKLOGIC_IMAGE:-progressofficial/marklogic-db:12.1.0-ubi9-rootless-2.3.1}"
keep=0
[[ "${1:-}" == "--keep" ]] && keep=1

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "${here}/.." && pwd)"
# kubectl.exe under Git Bash needs Windows-style paths for --from-file.
if command -v cygpath >/dev/null 2>&1; then
  repo_root="$(cygpath -m "${repo_root}")"
fi
ns=flux-smoke
fail=0

# Substitute only the two image variables; everything else in the manifests is literal.
render() { sed -e "s|\${FLUX_IMAGE}|${FLUX_IMAGE}|g" -e "s|\${MARKLOGIC_IMAGE}|${MARKLOGIC_IMAGE}|g" "$1"; }

echo "Cluster: $(kubectl config current-context)   Flux image: ${FLUX_IMAGE}"

echo "== MarkLogic"
# A previous run without --keep may still be tearing the namespace down.
if [[ "$(kubectl get namespace "${ns}" -o jsonpath='{.status.phase}' 2>/dev/null)" == "Terminating" ]]; then
  echo "waiting for namespace ${ns} to finish terminating..."
  kubectl wait --for=delete "namespace/${ns}" --timeout=300s >/dev/null 2>&1
fi
render "${here}/k8s/marklogic.yaml" | kubectl apply -f - >/dev/null || exit 1
if ! kubectl -n "${ns}" rollout status deployment/marklogic --timeout=600s; then
  echo "FAIL  MarkLogic did not become ready"; kubectl -n "${ns}" describe pod -l app=marklogic | tail -n 20; exit 1
fi

echo "== Input data (ConfigMap flux-data from flux-cli/src/test/resources/mixed-files)"
kubectl -n "${ns}" create configmap flux-data \
  --from-file=hello.json="${repo_root}/flux-cli/src/test/resources/mixed-files/hello.json" \
  --from-file=hello.txt="${repo_root}/flux-cli/src/test/resources/mixed-files/hello.txt" \
  --from-file=hello.xml="${repo_root}/flux-cli/src/test/resources/mixed-files/hello.xml" \
  --dry-run=client -o yaml | kubectl apply -f - >/dev/null || exit 1

echo "== Flux Jobs"
kubectl -n "${ns}" delete job flux-import flux-batch --ignore-not-found >/dev/null
render "${here}/k8s/flux-jobs.yaml" | kubectl apply -f - >/dev/null || exit 1

for job in flux-import flux-batch; do
  if kubectl -n "${ns}" wait --for=condition=complete "job/${job}" --timeout=600s >/dev/null 2>&1; then
    echo "PASS  ${job} reached Completed"
  else
    fail=1
    echo "FAIL  ${job} did not complete:"
    kubectl -n "${ns}" get pods -l "job-name=${job}" -o wide
    kubectl -n "${ns}" describe pods -l "job-name=${job}" | tail -n 25
  fi
  echo "----- ${job} logs"
  kubectl -n "${ns}" logs "job/${job}" --tail=40 | sed 's/^/      | /'
done

# The import Job's documents must be visible through the MarkLogic REST API.
count="$(kubectl -n "${ns}" exec deploy/marklogic -- \
  curl -s --digest -u admin:admin 'http://localhost:8000/v1/search?collection=k8s-import&format=json&pageLength=0' \
  | sed -n 's/.*"total":\([0-9]*\).*/\1/p')"
if [[ "${count:-0}" == 3 ]]; then
  echo "PASS  3 documents in collection k8s-import"
else
  fail=1; echo "FAIL  expected 3 documents in collection k8s-import, got '${count}'"
fi

if [[ "${keep}" == 0 ]]; then
  kubectl delete namespace "${ns}" --wait=false >/dev/null && echo "(namespace ${ns} deleted)"
else
  echo "(namespace ${ns} kept)"
fi
[[ "${fail}" == 0 ]]
