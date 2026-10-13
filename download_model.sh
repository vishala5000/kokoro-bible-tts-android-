#!/bin/bash
set -e

echo "📥 Downloading Kokoro 82M Model..."

# Create assets directory
mkdir -p app/src/main/assets/kokoro

# Download ONNX model from Hugging Face (using -L to follow redirects)
echo "Downloading kokoro-v1.0.onnx (~326MB)..."
wget -q --show-progress -L -O app/src/main/assets/kokoro/kokoro-v1.0.onnx \
  "https://huggingface.co/fastrtc/kokoro-onnx/resolve/main/kokoro-v1.0.onnx"

# Download voices file
echo "Downloading voices-v1.0.bin (~28MB)..."
wget -q --show-progress -L -O app/src/main/assets/kokoro/voices-v1.0.bin \
  "https://huggingface.co/fastrtc/kokoro-onnx/resolve/main/voices-v1.0.bin"

echo "✅ Model download complete!"
echo ""
echo "📊 Model files:"
ls -lh app/src/main/assets/kokoro/
