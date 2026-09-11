#!/usr/bin/env bash
set -e

# ==============================================================================
# Rent-A-Car Multi-Platform FOSS Release Builder
# ==============================================================================

echo "========================================================"
echo " Building Rent-A-Car Management System for FOSS Release"
echo "========================================================"

VERSION="1.0.0"
DIST_DIR="dist"
STAGE_DIR="target/standalone-stage"

# Resolve JDK Tools
if [ -n "${JAVA_HOME}" ] && [ -x "${JAVA_HOME}/bin/jar" ]; then
    JAR_BIN="${JAVA_HOME}/bin/jar"
elif [ -x "/usr/local/Cellar/openjdk/26.0.2.1/libexec/openjdk.jdk/Contents/Home/bin/jar" ]; then
    JAR_BIN="/usr/local/Cellar/openjdk/26.0.2.1/libexec/openjdk.jdk/Contents/Home/bin/jar"
else
    JAR_BIN="jar"
fi

# 1. Compile project with Maven and collect dependencies
echo "[1/4] Compiling project..."
if [ -x "./mvnw" ]; then
    ./mvnw clean compile dependency:copy-dependencies -DoutputDirectory=target/lib -DincludeScope=runtime
else
    mvn clean compile dependency:copy-dependencies -DoutputDirectory=target/lib -DincludeScope=runtime
fi

# 2. Assemble standalone fat JAR
echo "[2/4] Assembling standalone executable JAR with bundled dependencies..."
rm -rf "${STAGE_DIR}" "${DIST_DIR}"
mkdir -p "${STAGE_DIR}" "${DIST_DIR}"

# Copy application classes & resources
cp -R target/classes/* "${STAGE_DIR}/"

# Extract dependencies from target/lib into stage directory
if [ -d "target/lib" ]; then
    for libjar in target/lib/*.jar; do
        if [ -f "$libjar" ]; then
            (cd "${STAGE_DIR}" && "${JAR_BIN}" -xf "../.${libjar}" 2>/dev/null || true)
        fi
    done
fi

# Also extract from local lib/*.jar if present
if [ -d "lib" ]; then
    for libjar in lib/*.jar; do
        if [ -f "$libjar" ]; then
            (cd "${STAGE_DIR}" && "${JAR_BIN}" -xf "../.${libjar}" 2>/dev/null || true)
        fi
    done
fi

# Remove signatures to avoid SecurityException in fat JARs
rm -rf "${STAGE_DIR}/META-INF"/*.SF "${STAGE_DIR}/META-INF"/*.DSA "${STAGE_DIR}/META-INF"/*.RSA

# Create manifest
mkdir -p "${STAGE_DIR}/META-INF"
cat << 'EOF' > "${STAGE_DIR}/META-INF/MANIFEST.MF"
Manifest-Version: 1.0
Main-Class: main.Launcher
Implementation-Title: Rent-A-Car
Implementation-Version: 1.0.0
Implementation-Vendor: Alieon FOSS
EOF

# Build standalone JAR
"${JAR_BIN}" -cfm "${DIST_DIR}/rent-a-car-${VERSION}-standalone.jar" "${STAGE_DIR}/META-INF/MANIFEST.MF" -C "${STAGE_DIR}" .
cp "${DIST_DIR}/rent-a-car-${VERSION}-standalone.jar" "${DIST_DIR}/rent-a-car.jar"
echo "  -> Created: ${DIST_DIR}/rent-a-car-${VERSION}-standalone.jar"

# Copy FOSS metadata
cp LICENSE "${DIST_DIR}/LICENSE.txt"
cp README.md "${DIST_DIR}/README.md"
if [ -d "lib" ]; then
    cp -R lib "${DIST_DIR}/lib"
fi

# 3. Create cross-platform ZIP and Tarball bundles
echo "[3/4] Packaging portable distribution archives..."
(
    cd "${DIST_DIR}"
    zip -r "rent-a-car-${VERSION}-universal.zip" "rent-a-car.jar" "LICENSE.txt" "README.md" > /dev/null
    tar -czf "rent-a-car-${VERSION}-universal.tar.gz" "rent-a-car.jar" "LICENSE.txt" "README.md" > /dev/null
)

# 4. Completion summary
echo "[4/4] Release build completed successfully!"
echo "========================================================"
echo " Artifacts available in ${DIST_DIR}/:"
ls -lh "${DIST_DIR}"
echo "========================================================"
echo " How to run the standalone executable on ANY platform:"
echo "   java -jar dist/rent-a-car.jar"
echo "========================================================"
