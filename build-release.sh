#!/bin/bash

# Script to build release AAB for Play Store upload
# Make sure to set your keystore passwords first

echo "Finance Tracker - Release Build Script"
echo "====================================="

# Check if keystore file exists
if [ ! -f "finance-tracker-release-key.jks" ]; then
    echo "❌ Error: Keystore file not found!"
    echo "Please make sure finance-tracker-release-key.jks exists in the project root"
    exit 1
fi

# Set keystore passwords (you'll need to replace these with your actual passwords)
export KEYSTORE_PASSWORD="your_keystore_password_here"
export KEY_PASSWORD="your_key_password_here"

echo "📦 Building release AAB..."
echo "Note: You need to manually set the passwords in this script first!"

# Build the release AAB
./gradlew bundleRelease

if [ $? -eq 0 ]; then
    echo "✅ Release AAB built successfully!"
    echo "📍 Location: app/build/outputs/bundle/release/app-release.aab"
    echo ""
    echo "🚀 Next steps:"
    echo "1. Upload the AAB file to Google Play Console"
    echo "2. Fill in the store listing details"
    echo "3. Set up pricing and distribution"
    echo "4. Submit for review"
else
    echo "❌ Build failed! Please check the error messages above."
fi