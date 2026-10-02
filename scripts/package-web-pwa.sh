#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# Web PWA Game Installer & Package Builder for Xianxia Sect Idle
# Packages standalone Web Game PWA archive, offline assets, and installer manifest
# =============================================================================

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_DIR="${REPO_ROOT}/build/dist/web-pwa"
OUTPUT_DIR="${REPO_ROOT}/build/dist"

mkdir -p "${DIST_DIR}" "${OUTPUT_DIR}"

echo "📦 Packaging Xianxia Sect Idle PWA Web Game Bundle..."

# Copy PWA assets to distribution staging folder
cp -r "${REPO_ROOT}/pwa/"* "${DIST_DIR}/"

# Create Zip Archive
cd "${DIST_DIR}"
zip -r "${OUTPUT_DIR}/sect-idle-web-pwa.zip" ./*

# Create Tarball Archive
tar -czvf "${OUTPUT_DIR}/sect-idle-web-pwa.tar.gz" ./*

# Calculate SHA256 Checksums
cd "${OUTPUT_DIR}"
sha256sum sect-idle-web-pwa.zip > sect-idle-web-pwa.zip.sha256
sha256sum sect-idle-web-pwa.tar.gz > sect-idle-web-pwa.tar.gz.sha256

echo "✅ Web PWA Game artifacts created:"
echo "   - build/dist/sect-idle-web-pwa.zip"
echo "   - build/dist/sect-idle-web-pwa.tar.gz"
echo "   - build/dist/sect-idle-web-pwa.zip.sha256"
echo "   - build/dist/sect-idle-web-pwa.tar.gz.sha256"
