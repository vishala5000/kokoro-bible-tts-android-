#!/bin/bash
set -e

echo "📥 Downloading Kokoro 82M Model..."

# Create assets directory
mkdir -p app/src/main/assets/kokoro

# Download ONNX model
echo "Downloading kokoro-v1.0.onnx..."
wget -q --show-progress -O app/src/main/assets/kokoro/kokoro-v1.0.onnx \
  "https://huggingface.co/onnx-community/kokoro-82m-v1.0/resolve/main/onnx/model.onnx"

# Download voices
echo "Downloading voices-v1.0.bin..."
wget -q --show-progress -O app/src/main/assets/kokoro/voices-v1.0.bin \
  "https://huggingface.co/onnx-community/kokoro-82m-v1.0/resolve/main/voices/voices-v1.0.bin"

echo "✅ Model download complete!"
ls -lh app/src/main/assets/kokoro/
