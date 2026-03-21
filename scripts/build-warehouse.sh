#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="${ROOT_DIR}/backend"
SERVICE_DIR="${BACKEND_DIR}/warehouse-service"
TARGET_DIR="${SERVICE_DIR}/target"
APP_JAR_PATH="${SERVICE_DIR}/app.jar"
IMAGE_NAME="${1:-warehouse:test}"

log() {
  printf '[build-warehouse] %s\n' "$*"
}

fail() {
  printf '[build-warehouse] ERROR: %s\n' "$*" >&2
  exit 1
}

command -v mvn >/dev/null 2>&1 || fail "mvn command not found in PATH"
command -v docker >/dev/null 2>&1 || fail "docker command not found in PATH"

log "Building warehouse-service fat jar from backend aggregator"
(
  cd "${BACKEND_DIR}"
  mvn -pl warehouse-service -am clean package -DskipTests
)

FAT_JAR="$(find "${TARGET_DIR}" -maxdepth 1 -type f -name '*.jar' ! -name '*.jar.original' | head -n 1)"
[ -n "${FAT_JAR}" ] || fail "fat jar not found under ${TARGET_DIR}"

cp "${FAT_JAR}" "${APP_JAR_PATH}"
log "Prepared app jar at ${APP_JAR_PATH}"

log "Building Docker image ${IMAGE_NAME}"
(
  cd "${ROOT_DIR}"
  docker build -f backend/warehouse-service/Dockerfile -t "${IMAGE_NAME}" .
)

log "Build completed successfully"
