#!/bin/bash

# Script to start the Desktop Head Unit (DHU) for testing Android Auto.
# Project: AutoSample

ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
DHU_PATH="$ANDROID_HOME/extras/google/auto/desktop-head-unit"
ADB="$ANDROID_HOME/platform-tools/adb"

echo "=== Android Auto - Desktop Head Unit ==="
echo ""

# Check if the DHU exists.
if [ ! -f "$DHU_PATH" ]; then
    echo "ERROR: DHU was not found in $DHU_PATH"
    echo "Install via SDK Manager: Extras > Android Auto Desktop Head Unit Emulator"
    exit 1
fi

# Checks connected devices
echo "Checking connected devices..."
DEVICES=$($ADB devices | grep -v "List" | grep "device$" | wc -l | tr -d ' ')

if [ "$DEVICES" -eq "0" ]; then
    echo ""
    echo "WARNING: No Android device connected!"
    echo ""
    echo "To test your Android Auto app:"
    echo "1. Connect your Android phone via USB."
    echo "2. Enable USB Debugging in Developer Options."
    echo "3. On your phone, open Android Auto and go to Settings."
    echo "4. Tap 10 on 'Version' to activate developer mode."
    echo "5. Go to menu (3 dots) > Developer settings"
    echo "6. Enable 'Unknown sources' and 'Head unit server'"
    echo "7. Run this script again."
    echo ""
    exit 1
fi

echo "Device(s) found: $DEVICES"
echo ""

# Configure port forwarding for the head unit server.
echo "Configuring port forwarding..."
$ADB forward tcp:5277 tcp:5277

echo ""
echo "Starting Desktop Head Unit..."
echo "Press Ctrl+C to quit."
echo ""

# Inicia o DHU
"$DHU_PATH"