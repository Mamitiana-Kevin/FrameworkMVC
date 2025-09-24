#!/bin/bash
# ------------------------------------------------------------------------
# Script de déploiement Linux/Ubuntu pour compiler le framework
# et préparer le projet de test (Tomcat)
# ------------------------------------------------------------------------

# === Configuration (adapte selon ton arborescence) ===
FRAMEWORK_DIR="$HOME/itu/L3/S5/framework"
BUILD_DIR="$FRAMEWORK_DIR/build"
TEST_DIR="/var/lib/tomcat10/webapps/testFramework"
SERVLET_JAR="$FRAMEWORK_DIR/jakarta.servlet-api_5.0.0.jar"

# === Création des dossiers ===
mkdir -p "$BUILD_DIR/classes"

# === Compilation récursive des sources Java du framework ===
echo "Compilation du framework..."
find "$FRAMEWORK_DIR" -name "*.java" > "$BUILD_DIR/sources.txt"

javac -classpath "$SERVLET_JAR" -d "$BUILD_DIR/classes" @"$BUILD_DIR/sources.txt"
if [ $? -ne 0 ]; then
    echo "Erreur de compilation."
    exit 1
fi

# === Création du JAR ===
echo "Création du framework.jar..."
cd "$BUILD_DIR" || exit 1
rm -f framework.jar
jar cvf framework.jar -C classes .

# === Copie du JAR dans le projet de test ===
echo "Copie du framework.jar dans le projet Test..."
mkdir -p "$TEST_DIR/WEB-INF/lib"
cp -f framework.jar "$TEST_DIR/WEB-INF/lib/"

# === Option : redémarrage de Tomcat si chemin fourni ===
if [ -n "$1" ]; then
    echo "Redémarrage de Tomcat..."
    "$1/bin/shutdown.sh"
    "$1/bin/startup.sh"
fi

echo "Déploiement terminé ✅"
