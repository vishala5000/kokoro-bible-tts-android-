package com.bible.tts

import android.content.Context
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsKokoroModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class KokoroTTS(private val context: Context) {
    private var tts: OfflineTts? = null

    suspend fun initialize() = withContext(Dispatchers.IO) {
        val modelDir = File(context.cacheDir, "kokoro")
        modelDir.mkdirs()
        
        // Copy assets to cache directory for Sherpa-ONNX to read
        copyAsset("kokoro/model.onnx", File(modelDir, "model.onnx"))
        copyAsset("kokoro/tokens.txt", File(modelDir, "tokens.txt"))
        
        val voicesFile = File(modelDir, "voices.bin")
        if (context.assets.list("kokoro")?.contains("voices.bin") == true) {
            copyAsset("kokoro/voices.bin", voicesFile)
        }

        // OPTIMIZED FOR LOW-END PHONES:
        // 1. numThreads = 1 prevents CPU cache thrashing on budget chips
        // 2. provider = "cpu" is the most reliable and fastest for int8 models
        // 3. INT8 quantization reduces model size by 75% for faster RAM loading
        val kokoroConfig = OfflineTtsKokoroModelConfig(
            model = File(modelDir, "model.onnx").absolutePath,
            voices = if (voicesFile.exists()) voicesFile.absolutePath else "",
            tokens = File(modelDir, "tokens.txt").absolutePath,
            dataDir = "",
            lexicon = "",
            lengthScale = 1.0f
        )

        val modelConfig = OfflineTtsModelConfig(
            kokoro = kokoroConfig,
            numThreads = 1, // Critical for low-end phone speed
            debug = false,
            provider = "cpu"
        )

        val config = OfflineTtsConfig(
            model = modelConfig,
            ruleFsts = "",
            ruleFars = "",
            maxNumSentences = 2 // Process in smaller chunks for lower latency
        )

        tts = OfflineTts(config = config)
    }

    suspend fun synthesize(text: String, voice: String, speed: Float, langCode: String): Pair<FloatArray, Int> = withContext(Dispatchers.IO) {
        val speakerId = getSpeakerId(voice)
        
        val audio = tts?.generate(text = text, sid = speakerId, speed = speed)
        
        val samples = audio?.samples ?: FloatArray(0)
        val sampleRate = audio?.sampleRate ?: 24000
        Pair(samples, sampleRate)
    }

    private fun getSpeakerId(voice: String): Int {
        return when (voice) {
            "am_adam" -> 0      // Default English male voice
            "hm_omega" -> 55    // Hindi male voice
            else -> 0
        }
    }

    private fun copyAsset(fileName: String, outFile: File) {
        if (!outFile.exists()) {
            context.assets.open(fileName).use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    fun release() {
        tts?.release()
    }
    
    fun getModelInfo(): String = "Sherpa-ONNX INT8 Kokoro (Optimized for Lightning Speed on Low-End Phones)"
}
