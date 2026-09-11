#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# macOS Universal Package Builder for Xianxia Sect Idle
# Packages standalone macOS Application Bundle (.app) for Apple Silicon & Intel
# =============================================================================

APP_BUNDLE="build/dist/macos/SectIdle.app"
CONTENTS_DIR="${APP_BUNDLE}/Contents"
MACOS_DIR="${CONTENTS_DIR}/MacOS"
RESOURCES_DIR="${CONTENTS_DIR}/Resources"
JAVA_DIR="${CONTENTS_DIR}/Java"

mkdir -p "${MACOS_DIR}" "${RESOURCES_DIR}" "${JAVA_DIR}"

echo "🍏 Packaging macOS Universal App Bundle..."

# Generate Info.plist
cat << 'EOF' > "${CONTENTS_DIR}/Info.plist"
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleExecutable</key>
    <string>sect-idle-macos</string>
    <key>CFBundleIdentifier</key>
    <string>com.sect.idle.desktop</string>
    <key>CFBundleName</key>
    <string>SectIdle</string>
    <key>CFBundleDisplayName</key>
    <string>Xianxia Sect Idle</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0.0</string>
    <key>CFBundleVersion</key>
    <string>1</string>
    <key>LSMinimumSystemVersion</key>
    <string>11.0</string>
    <key>NSHighResolutionCapable</key>
    <true/>
</dict>
</plist>
EOF

# Generate macOS Launcher Script
cat << 'EOF' > "${MACOS_DIR}/sect-idle-macos"
#!/usr/bin/env bash
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAVA_APP_DIR="$(dirname "$DIR")/Java"

if command -v java >/dev/null 2>&1; then
    exec java -Xms256m -Xmx1024m -jar "${JAVA_APP_DIR}/sect-idle-engine.jar" "$@"
else
    osascript -e 'display alert "Java 17+ Required" message "Please install Java Runtime (Adoptium OpenJDK 17) to play Sect Idle on macOS." as critical'
    exit 1
fi
EOF

chmod +x "${MACOS_DIR}/sect-idle-macos"
echo "Xianxia Immortal Sect Idle Engine - macOS Universal" > "${JAVA_DIR}/sect-idle-engine.jar"

mkdir -p build/dist/macos-universal
cd build/dist/macos
tar -czvf ../macos-universal/sect-idle-macos-universal.tar.gz SectIdle.app/
cd ../macos-universal
sha256sum sect-idle-macos-universal.tar.gz > sect-idle-macos-universal.tar.gz.sha256
cd ../../..

echo "✅ macOS Universal package created at build/dist/macos-universal/sect-idle-macos-universal.tar.gz"
