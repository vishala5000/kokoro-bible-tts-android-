#!/bin/bash
set -e

echo "📥 Downloading Sherpa-ONNX Kokoro Model..."
mkdir -p app/src/main/assets/kokoro

echo "Downloading model.onnx..."
wget -q --show-progress -L -O app/src/main/assets/kokoro/model.onnx \
  "https://huggingface.co/csukuangfj/sherpa-onnx-tts-samples/resolve/main/kokoro/v019/model.onnx"

echo "Downloading tokens.txt..."
wget -q --show-progress -L -O app/src/main/assets/kokoro/tokens.txt \
  "https://huggingface.co/csukuangfj/sherpa-onnx-tts-samples/resolve/main/kokoro/v019/tokens.txt"

echo "Downloading voices.bin..."
wget -q --show-progress -L -O app/src/main/assets/kokoro/voices.bin \
  "https://huggingface.co/csukuangfj/sherpa-onnx-tts-samples/resolve/main/kokoro/v019/voices.bin"

echo "✅ Model download complete!"
ls -lh app/src/main/assets/kokoro/
