#!/bin/bash
# -----------------------------------------------------------
# Script: setup.sh
# Description: Performs one-time setup: checks environment,
#              downloads dependencies, initializes database,
#              and builds the application.
# -----------------------------------------------------------

echo "🛠️ Starting VaultSys System Setup..."

# --- Configuration ---
# Use .env file or local environment variables for credentials
export DB_HOST=${DB_HOST:-localhost}
export DB_PORT=${DB_PORT:-5432}
export DB_NAME=${DB_NAME:-vaultsys_db}
export DB_USER=${DB_USER:-vaultsys_user}
# NOTE: Set the DB_PASSWORD in your environment before running!
# export DB_PASSWORD="your_secure_password"

# Directory structure
BASE_DIR=$(cd "$(dirname "$0")/.." && pwd)
SCRIPTS_DIR="$BASE_DIR/scripts"
LIB_DIR="$BASE_DIR/lib"
JAR_NAME="vaultsys.jar"

# --- 1. Environment Checks ---
echo ""
echo "--- 1. Checking Environment and Dependencies ---"

# Check for Java
if ! command -v java &> /dev/null || ! command -v javac &> /dev/null; then
    echo "❌ Error: Java Development Kit (JDK) is not installed. Please install Java 17+."
    exit 1
fi
echo "✅ Java/JDK found."

# Check for PostgreSQL client
if ! command -v psql &> /dev/null; then
    echo "⚠️ Warning: PostgreSQL client (psql) not found. Database setup might fail."
fi
echo "✅ psql client found (or proceeding without)."

# --- 2. Dependency Management ---
mkdir -p "$LIB_DIR"
JDBC_JAR="postgresql-42.7.3.jar" # Using a common recent version

if [ ! -f "$LIB_DIR/$JDBC_JAR" ]; then
    echo "⬇️ Downloading PostgreSQL JDBC driver..."
    # A public, direct URL is best practice for dependency scripts
    wget -q -O "$LIB_DIR/$JDBC_JAR" \
        "https://jdbc.postgresql.org/download/$JDBC_JAR"
    if [ $? -ne 0 ]; then
        echo "❌ Error: Failed to download JDBC driver. Please download $JDBC_JAR manually and place it in the '$LIB_DIR' folder."
        exit 1
    fi
    echo "✅ JDBC driver downloaded successfully."
else
    echo "✅ JDBC driver already present."
fi

# --- 3. Database Initialization ---
echo ""
echo "--- 3. Initializing Database Schema and Data ---"
bash "$SCRIPTS_DIR/init_database.sh"

if [ $? -ne 0 ]; then
    echo "❌ Database initialization failed. Setup aborting."
    exit 1
fi
echo "✅ Database initialization complete."

# --- 4. Application Build ---
echo ""
echo "--- 4. Compiling and Packaging Java Application ---"
bash "$SCRIPTS_DIR/build_java.sh"

if [ $? -ne 0 ]; then
    echo "❌ Java build failed. Setup aborting."
    exit 1
fi
echo "✅ VaultSys setup completed successfully! Run ./scripts/start_services.sh"