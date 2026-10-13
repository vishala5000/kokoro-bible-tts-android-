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

        // Configure specifically for Kokoro (removed 'lang' for compatibility with v1.10.44)
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
            numThreads = 2,
            debug = false,
            provider = "cpu"
        )

        val config = OfflineTtsConfig(
            model = modelConfig,
            ruleFsts = "",
            ruleFars = "",
            maxNumSentences = 1
        )

        tts = OfflineTts(config = config)
    }

    suspend fun synthesize(text: String, voice: String, speed: Float, langCode: String): Pair<FloatArray, Int> = withContext(Dispatchers.IO) {
        val speakerId = getSpeakerId(voice)
        
        // Use 'sid' for speaker ID
        val audio = tts?.generate(text = text, sid = speakerId, speed = speed)
        
        val samples = audio?.samples ?: FloatArray(0)
        val sampleRate = audio?.sampleRate ?: 24000
        Pair(samples, sampleRate)
    }

    private fun getSpeakerId(voice: String): Int {
        return when (voice) {
            "am_adam" -> 0      // Default English male voice in multi-lang v1.0
            "hm_omega" -> 55    // Hindi male voice (approximate index for multi-lang)
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
    
    fun getModelInfo(): String = "Sherpa-ONNX Kokoro Multi-Language Engine Ready"
}
