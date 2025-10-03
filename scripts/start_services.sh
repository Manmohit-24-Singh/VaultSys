#!/bin/bash
# -----------------------------------------------------------
# Script: start_services.sh
# Description: Executes the compiled VaultSys Java application.
# -----------------------------------------------------------

echo "🚀 Starting VaultSys Banking System..."

# --- Configuration ---
BASE_DIR=$(cd "$(dirname "$0")/.." && pwd)
BIN_DIR="$BASE_DIR/bin"
JAR_NAME="vaultsys.jar"

# --- 1. Check for JAR file ---
if [ ! -f "$BIN_DIR/$JAR_NAME" ]; then
    echo "❌ Error: Application JAR '$JAR_NAME' not found."
    echo "Please run './scripts/setup.sh' first to build the application."
    exit 1
fi

# --- 2. Run the JAR ---
# The -jar option ignores the Class-Path in the manifest, so we must use
# the -cp (classpath) option and specify the main class explicitly for a robust run.
# HOWEVER, since we built the JAR to include the Class-Path in the manifest
# referencing `./lib/postgresql-42.7.3.jar`, we can use the simpler -jar command,
# assuming we run from the bin directory.
cd "$BIN_DIR"
echo "--- Application Console ---"
# java -cp ".:lib/*" com.vaultsys.Main # Alternative
java -jar "$JAR_NAME"

# Check the exit status of the Java process
if [ $? -eq 0 ]; then
    echo "✅ VaultSys application exited successfully."
else
    echo "⚠️ VaultSys application exited with an error. See log/console output for details."
fi

# Return to base directory
cd "$BASE_DIR"