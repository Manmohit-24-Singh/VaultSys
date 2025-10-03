#!/bin/bash
# -----------------------------------------------------------
# Script: build_java.sh
# Description: Compiles all Java source code and packages
#              it into a single executable JAR file.
# -----------------------------------------------------------

echo "   ☕ Starting Java Build Process..."

# Directory structure
BASE_DIR=$(cd "$(dirname "$0")/.." && pwd)
SRC_DIR="$BASE_DIR/src/main/java"
BIN_DIR="$BASE_DIR/bin"
LIB_DIR="$BASE_DIR/lib"
JDBC_JAR="$LIB_DIR/postgresql-42.7.3.jar"
MAIN_CLASS="com.vaultsys.Main"
JAR_NAME="vaultsys.jar"
CLASSPATH=".:$JDBC_JAR" # Include the current directory and the JDBC JAR

# 1. Clean up previous build
mkdir -p "$BIN_DIR"
rm -rf "$BIN_DIR/*"
echo "   -> Cleaned $BIN_DIR."

# 2. Compile Java source files
echo "   -> Compiling Java source files..."
find "$SRC_DIR" -name "*.java" > "$BASE_DIR/sources.txt"

# Compile with the classpath including the JDBC driver
javac -cp "$CLASSPATH" -d "$BIN_DIR" @$BASE_DIR/sources.txt

if [ $? -ne 0 ]; then
    echo "   ❌ Java compilation failed."
    rm "$BASE_DIR/sources.txt"
    exit 1
fi
echo "   ✅ Compilation successful."

# 3. Create the Manifest file
echo "Main-Class: $MAIN_CLASS" > "$BIN_DIR/MANIFEST.MF"
echo "Class-Path: ./lib/$(basename $JDBC_JAR)" >> "$BIN_DIR/MANIFEST.MF"
echo "   -> Manifest created."

# 4. Package into an executable JAR
# The -C $BIN_DIR command tells jar to change directory to bin/ before adding files
jar cvfm "$BIN_DIR/$JAR_NAME" "$BIN_DIR/MANIFEST.MF" -C "$BIN_DIR" .
if [ $? -ne 0 ]; then
    echo "   ❌ JAR packaging failed."
    rm "$BASE_DIR/sources.txt"
    exit 1
fi

# 5. Copy dependencies (the JAR needs its dependencies)
mkdir -p "$BIN_DIR/lib"
cp "$JDBC_JAR" "$BIN_DIR/lib/"
cp "$BASE_DIR/application.properties" "$BIN_DIR/" # Copy config for runtime

# 6. Cleanup
rm "$BASE_DIR/sources.txt"
rm "$BIN_DIR/MANIFEST.MF"

echo "   ✅ Build successful. Executable JAR is available at $BIN_DIR/$JAR_NAME"