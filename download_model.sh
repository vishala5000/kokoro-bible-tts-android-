#!/bin/bash
set -e

echo "📥 Downloading Official Sherpa-ONNX Kokoro Model & Android Library..."

# Create directories
mkdir -p app/src/main/assets/kokoro
mkdir -p app/libs

# 1. Download the official Sherpa-ONNX Android AAR library
echo "Downloading sherpa-onnx AAR library..."
wget -q --show-progress -L -O app/libs/sherpa-onnx.aar \
  "https://huggingface.co/csukuangfj/sherpa-onnx-libs/resolve/main/android/aar/sherpa-onnx-1.10.44.aar"

# 2. Download the official Sherpa-ONNX Kokoro multi-language model tarball
echo "Downloading kokoro-multi-lang-v1_0.tar.bz2 (~150MB)..."
wget -q --show-progress -L -O kokoro-multi-lang-v1_0.tar.bz2 \
  "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/kokoro-multi-lang-v1_0.tar.bz2"

echo "Extracting model files..."
tar -xf kokoro-multi-lang-v1_0.tar.bz2

# Copy the required files to the Android assets directory
echo "Copying files to Android assets..."
cp kokoro-multi-lang-v1_0/model.onnx app/src/main/assets/kokoro/
cp kokoro-multi-lang-v1_0/tokens.txt app/src/main/assets/kokoro/

# Copy the voices file (Sherpa packages this for Kokoro)
if [ -f "kokoro-multi-lang-v1_0/voices.bin" ]; then
  cp kokoro-multi-lang-v1_0/voices.bin app/src/main/assets/kokoro/
fi

# Clean up temporary files
rm -rf kokoro-multi-lang-v1_0.tar.bz2 kokoro-multi-lang-v1_0

echo "✅ Model and library download complete!"
ls -lh app/libs/
ls -lh app/src/main/assets/kokoro/
