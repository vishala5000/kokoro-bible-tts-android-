#!/bin/bash
set -e

echo "📥 Downloading Optimized INT8 Sherpa-ONNX Kokoro Model (Lightning Fast)..."

# Create directories
mkdir -p app/src/main/assets/kokoro
mkdir -p app/libs

# 1. Download the official Sherpa-ONNX Android AAR library
echo "Downloading sherpa-onnx AAR library..."
wget -q --show-progress -L -O app/libs/sherpa-onnx.aar \
  "https://huggingface.co/csukuangfj/sherpa-onnx-libs/resolve/main/android/aar/sherpa-onnx-1.10.44.aar"

# 2. Download the INT8 quantized Kokoro multi-language model (Much faster on mobile CPUs)
echo "Downloading kokoro-int8-multi-lang-v1_0.tar.bz2 (~40MB)..."
wget -q --show-progress -L -O kokoro-int8-multi-lang-v1_0.tar.bz2 \
  "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/kokoro-int8-multi-lang-v1_0.tar.bz2"

echo "Extracting model files..."
tar -xf kokoro-int8-multi-lang-v1_0.tar.bz2

# Copy the required files to the Android assets directory
echo "Copying files to Android assets..."
cp kokoro-int8-multi-lang-v1_0/model.int8.onnx app/src/main/assets/kokoro/model.onnx
cp kokoro-int8-multi-lang-v1_0/tokens.txt app/src/main/assets/kokoro/tokens.txt

# Copy the voices file
if [ -f "kokoro-int8-multi-lang-v1_0/voices.bin" ]; then
  cp kokoro-int8-multi-lang-v1_0/voices.bin app/src/main/assets/kokoro/voices.bin
fi

# Clean up temporary files
rm -rf kokoro-int8-multi-lang-v1_0.tar.bz2 kokoro-int8-multi-lang-v1_0

echo "✅ Optimized INT8 Model and library download complete!"
ls -lh app/libs/
ls -lh app/src/main/assets/kokoro/
