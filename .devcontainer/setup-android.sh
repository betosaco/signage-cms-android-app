#!/usr/bin/env bash
set -euo pipefail

# Colors for output
GREEN='\033[0;32m'
NC='\033[0m'

log() {
  echo -e "${GREEN}[SETUP] $1${NC}"
}

log "Updating package lists..."
apt-get update -y

log "Installing required packages (curl, unzip, git, python3, openjdk-17-jdk)..."
apt-get install -y curl unzip git python3 openjdk-17-jdk

log "Setting JAVA_HOME..."
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$PATH"

log "Downloading Android SDK command line tools..."
ANDROID_SDK_ROOT="/home/codespace/android-sdk"
mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
cd /tmp
SDK_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip"
curl -L "$SDK_TOOLS_URL" -o cmdline-tools.zip
unzip -q cmdline-tools.zip -d "$ANDROID_SDK_ROOT/cmdline-tools"
mv "$ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools" "$ANDROID_SDK_ROOT/cmdline-tools/latest"

log "Setting ANDROID_HOME and updating PATH..."
export ANDROID_HOME="$ANDROID_SDK_ROOT"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

log "Accepting Android SDK licenses..."
yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses

log "Installing Android SDK components (platform-tools, platforms;android-33, build-tools;33.0.0)..."
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$ANDROID_HOME" \
  "platform-tools" \
  "platforms;android-33" \
  "build-tools;33.0.0"

log "Verifying Android SDK installation..."
ls -l "$ANDROID_HOME/platform-tools"

log "Installing Gradle wrapper..."
cd /workspaces/$(basename "$PWD")
if [ ! -f gradlew ]; then
  gradle wrapper || true
fi

log "Setting permissions for SDK tools..."
chown -R codespace:codespace "$ANDROID_HOME"
chmod -R 755 "$ANDROID_HOME"

log "Creating symlinks for common tools..."
ln -sf "$ANDROID_HOME/platform-tools/adb" /usr/local/bin/adb
ln -sf "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" /usr/local/bin/sdkmanager

log "Configuring git..."
git config --global user.name "Codespace User"
git config --global user.email "codespace@users.noreply.github.com"

log "Verifying installations..."
java -version
javac -version
adb version || true
sdkmanager --list || true
python3 --version

log "Android development environment setup complete!" 