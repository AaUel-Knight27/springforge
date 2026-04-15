#!/bin/bash
# SpringForge Install Script
# Builds the CLI JAR and installs the 'forge' command

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
INSTALL_DIR="/usr/local/bin"
JAR_DIR="/opt/springforge"

echo ""
echo "  ⚡ Installing SpringForge CLI..."
echo "  ─────────────────────────────────"
echo ""

# Build the JAR
echo "  → Building JAR..."
cd "$PROJECT_DIR"
mvn clean package -q -DskipTests

JAR_FILE=$(find target -name "springforge-cli-*.jar" -not -name "*original*" | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "  ❌ Build failed: JAR not found"
    exit 1
fi

echo "  ✅ Built: $JAR_FILE"

# Install JAR
echo "  → Installing to $JAR_DIR..."
sudo mkdir -p "$JAR_DIR"
sudo cp "$JAR_FILE" "$JAR_DIR/springforge.jar"

# Create wrapper script
echo "  → Creating 'forge' command..."
sudo tee "$INSTALL_DIR/forge" > /dev/null << 'EOF'
#!/bin/bash
java -jar /opt/springforge/springforge.jar "$@"
EOF
sudo chmod +x "$INSTALL_DIR/forge"

echo ""
echo "  🎉 SpringForge installed successfully!"
echo ""
echo "  Usage:"
echo "    forge --help"
echo "    forge init my-app"
echo "    forge add service user"
echo "    forge add entity User username:string email:string --service user"
echo ""
