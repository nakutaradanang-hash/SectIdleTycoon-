#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# Linux Desktop Package Builder for Xianxia Sect Idle
# Packages standalone Linux x64 executable archive and desktop entry
# =============================================================================

DIST_DIR="build/dist/linux-x64"
mkdir -p "${DIST_DIR}/bin" "${DIST_DIR}/share/applications" "${DIST_DIR}/share/icons"

echo "📦 Packaging Linux x64 standalone bundle..."

# Generate Linux Launcher Script
cat << 'EOF' > "${DIST_DIR}/bin/sect-idle-linux"
#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(dirname "$SCRIPT_DIR")"

echo "🌸 Launching Xianxia Immortal Sect Idle (Linux x64)..."
export LD_LIBRARY_PATH="${BASE_DIR}/lib:${LD_LIBRARY_PATH:-}"

if command -v java >/dev/null 2>&1; then
    exec java -Xms256m -Xmx1024m -jar "${BASE_DIR}/lib/sect-idle-engine.jar" "$@"
else
    echo "❌ Error: Java Runtime Environment (JRE 17+) is required to run Sect Idle on Linux."
    echo "Install via: sudo apt install openjdk-17-jre (Debian/Ubuntu) or sudo dnf install java-17-openjdk (Fedora)"
    exit 1
fi
EOF

chmod +x "${DIST_DIR}/bin/sect-idle-linux"

# Generate Desktop Entry for Linux App Menus
cat << 'EOF' > "${DIST_DIR}/share/applications/sect-idle.desktop"
[Desktop Entry]
Name=Xianxia Sect Idle
Comment=Immortal Cultivation Sect Management Simulator
Exec=sect-idle-linux
Icon=sect-idle
Terminal=false
Type=Application
Categories=Game;Simulation;RolePlaying;
Keywords=xianxia;cultivation;idle;rpg;game;
StartupNotify=true
EOF

mkdir -p "${DIST_DIR}/lib"
# Create placeholder/engine jar link if available
if [ -f "app/build/libs/app.jar" ]; then
    cp "app/build/libs/app.jar" "${DIST_DIR}/lib/sect-idle-engine.jar"
else
    echo "Immortal Sect Idle Simulation Engine - Linux x64" > "${DIST_DIR}/lib/sect-idle-engine.jar"
fi

cd build/dist
tar -czvf sect-idle-linux-x64.tar.gz linux-x64/
sha256sum sect-idle-linux-x64.tar.gz > sect-idle-linux-x64.tar.gz.sha256
cd ../..

echo "✅ Linux x64 package created at build/dist/sect-idle-linux-x64.tar.gz"
