#!/bin/bash

# === Configuration ===
JAR_SOURCE="testFramework/WEB-INF/lib/framework.jar"          # Path to your JAR file
TARGET_DIR="/home/mamitiana/Documents/GitHub/Test/WEB-INF/lib"             

# === Script ===
echo "Deploying $JAR_SOURCE to Test"

# Copy the JAR
echo "Copying JAR file..."
sudo cp "$JAR_SOURCE" "$TARGET_DIR/"

echo "✅ Deployment complete!"
