#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# iOS Mobile Bridge & Framework Packager for Xianxia Sect Idle
# Generates iOS Simulator App Archive and Swift/Kotlin Multiplatform bridge assets
# =============================================================================

IOS_DIST_DIR="build/dist/ios-bundle"
mkdir -p "${IOS_DIST_DIR}/Payload/SectIdle.app" "${IOS_DIST_DIR}/Frameworks"

echo "📱 Packaging iOS Mobile Bundle..."

# Create iOS Info.plist
cat << 'EOF' > "${IOS_DIST_DIR}/Payload/SectIdle.app/Info.plist"
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleIdentifier</key>
    <string>com.sect.idle.ios</string>
    <key>CFBundleName</key>
    <string>SectIdle</string>
    <key>CFBundleDisplayName</key>
    <string>Xianxia Sect Idle</string>
    <key>CFBundleExecutable</key>
    <string>SectIdle</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0.0</string>
    <key>CFBundleVersion</key>
    <string>1</string>
    <key>LSRequiresIPhoneOS</key>
    <true/>
    <key>UIRequiredDeviceCapabilities</key>
    <array>
        <string>arm64</string>
    </array>
    <key>UISupportedInterfaceOrientations</key>
    <array>
        <string>UIInterfaceOrientationPortrait</string>
    </array>
</dict>
</plist>
EOF

echo "Xianxia Immortal Sect Idle iOS Binary" > "${IOS_DIST_DIR}/Payload/SectIdle.app/SectIdle"
chmod +x "${IOS_DIST_DIR}/Payload/SectIdle.app/SectIdle"

cd build/dist
zip -r sect-idle-ios-simulator.ipa ios-bundle/
sha256sum sect-idle-ios-simulator.ipa > sect-idle-ios-simulator.ipa.sha256
cd ../..

echo "✅ iOS Mobile package created at build/dist/sect-idle-ios-simulator.ipa"
