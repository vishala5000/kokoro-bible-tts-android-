package com.bible.tts

import android.content.Context
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class KokoroTTS(private val context: Context) {
    private var tts: OfflineTts? = null

    suspend fun initialize() = withContext(Dispatchers.IO) {
        val modelDir = File(context.cacheDir, "kokoro")
        modelDir.mkdirs()
        
        copyAsset("kokoro/model.onnx", File(modelDir, "model.onnx"))
        copyAsset("kokoro/tokens.txt", File(modelDir, "tokens.txt"))
        
        val voicesFile = File(modelDir, "voices.bin")
        if (context.assets.list("kokoro")?.contains("voices.bin") == true) {
            copyAsset("kokoro/voices.bin", voicesFile)
        }

        val modelConfig = OfflineTtsModelConfig(
            vits = OfflineTtsVitsModelConfig(
                model = File(modelDir, "model.onnx").absolutePath,
                tokens = File(modelDir, "tokens.txt").absolutePath,
                dataDir = "",
                dictDir = "",
                lexicon = "",
                noiseScale = 0.667f,
                noiseScaleW = 0.8f,
                lengthScale = 1.0f,
            ),
            numThreads = 2,
            debug = false,
            provider = "cpu"
        )

        val config = OfflineTtsConfig(
            model = modelConfig,
            ruleFsts = "",
            ruleFars = "",
            maxNumSentences = 1,
            voice = if (voicesFile.exists()) voicesFile.absolutePath else ""
        )

        tts = OfflineTts(config = config)
    }

    suspend fun synthesize(text: String, voice: String, speed: Float, langCode: String): Pair<FloatArray, Int> = withContext(Dispatchers.IO) {
        val speakerId = getSpeakerId(voice)
        
        val audio = tts?.generate(text, speakerId = speakerId, speed = speed)
        
        val samples = audio?.samples ?: FloatArray(0)
        val sampleRate = audio?.sampleRate ?: 24000
        Pair(samples, sampleRate)
    }

    private fun getSpeakerId(voice: String): Int {
        return when (voice) {
            "am_adam" -> 0
            "hm_omega" -> 55
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
