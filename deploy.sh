#!/bin/bash

echo "============================================"
echo "   Nettoyage et redéploiement complet..."
echo "============================================"
echo

# Étape 1 : Nettoyage
echo "1. Nettoyage des anciens fichiers..."
rm -rf build/classes
rm -f build/framework.jar
rm -f testFramework/WEB-INF/lib/framework.jar

# Étape 2 : Création des répertoires
echo "2. Création des répertoires..."
mkdir -p build/classes
mkdir -p testFramework/WEB-INF/lib

# Étape 3 : Compilation de toutes les classes du framework
echo "3. Compilation des sources..."
javac -classpath "jakarta.servlet-api_5.0.0.jar" -d "build/classes" \
    $(find framework -name "*.java")

if [ $? -ne 0 ]; then
    echo "❌ ERREUR: Échec de la compilation!"
    exit 1
fi

# Étape 4 : Création du JAR
echo "4. Création du JAR..."
cd build || exit
jar cf framework.jar -C classes .
cd ..

# Étape 5 : Copie du JAR
echo "5. Copie du JAR dans le projet web..."
cp build/framework.jar testFramework/WEB-INF/lib/

# Étape 6 : Affichage du contenu du JAR
echo "6. Contenu du JAR créé :"
echo "--------------------Contenu du jar------------------------"
jar tf testFramework/WEB-INF/lib/framework.jar
echo "--------------------------------------------"

echo
echo "✅ Redéploiement terminé avec succès!"
exit 0
