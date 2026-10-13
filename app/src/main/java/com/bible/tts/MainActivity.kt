package com.bible.tts

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bible.tts.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var tts: KokoroTTS
    private var selectedLangCode = "en-us"
    private var selectedVoice = "am_adam"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        tts = KokoroTTS(this)
        
        setupUI()
        initializeTTS()
    }
    
    private fun setupUI() {
        // Setup language spinner
        val languages = VoiceCatalog.languages.map { it.displayName }
        val langAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = langAdapter
        
        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val langConfig = VoiceCatalog.languages[position]
                selectedLangCode = langConfig.langCode
                
                // Update voice spinner
                val voices = langConfig.voices.map { it.displayName }
                val voiceAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, voices)
                voiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerVoice.adapter = voiceAdapter
                selectedVoice = langConfig.voices[0].id
                
                // Update default text
                binding.editTextVerse.setText(VoiceCatalog.getDefaultText(selectedLangCode))
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Setup voice spinner
        binding.spinnerVoice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val langConfig = VoiceCatalog.languages.first { it.langCode == selectedLangCode }
                selectedVoice = langConfig.voices[position].id
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Default text
        binding.editTextVerse.setText(VoiceCatalog.getDefaultText("en-us"))
        
        // Speed slider
        binding.seekBarSpeed.progress = 90 // 0.9 * 100
        binding.seekBarSpeed.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val speed = progress / 100.0f
                binding.textViewSpeed.text = String.format("%.2fx", speed)
            }
            
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
        
        // Generate button
        binding.btnGenerate.setOnClickListener {
            generateAudio()
        }
        
        // Show model info button
        binding.btnModelInfo.setOnClickListener {
            showModelInfo()
        }
    }
    
    private fun initializeTTS() {
        binding.progressBar.visibility = View.VISIBLE
        binding.textViewStatus.text = "⚡ Loading Kokoro 82M Engine..."
        
        lifecycleScope.launch {
            try {
                tts.initialize()
                binding.progressBar.visibility = View.GONE
                binding.textViewStatus.text = "✅ Engine Ready! Kokoro 82M loaded successfully."
                binding.btnGenerate.isEnabled = true
                binding.btnModelInfo.isEnabled = true
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.textViewStatus.text = "❌ Error: ${e.message}"
                Toast.makeText(this@MainActivity, "Failed to initialize TTS", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun generateAudio() {
        val text = binding.editTextVerse.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter a Bible verse", Toast.LENGTH_SHORT).show()
            return
        }
        
        val speed = binding.seekBarSpeed.progress / 100.0f
        
        binding.progressBar.visibility = View.VISIBLE
        binding.textViewStatus.text = "🎵 Generating audio..."
        binding.btnGenerate.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                val (samples, sampleRate) = tts.synthesize(text, selectedVoice, speed, selectedLangCode)
                val elapsed = System.currentTimeMillis() - startTime
                
                val duration = samples.size.toFloat() / sampleRate
                val rtf = elapsed / 1000.0 / duration
                
                // Save to WAV file
                val outputFile = File(getExternalFilesDir(null), "bible_verse.wav")
                saveWav(samples, sampleRate, outputFile)
                
                // Play audio
                playAudio(samples, sampleRate)
                
                binding.progressBar.visibility = View.GONE
                binding.textViewStatus.text = """
                    ✅ Generated ${String.format("%.2f", duration)}s audio in ${elapsed}ms
                    ⚡ Real-Time Factor: ${String.format("%.2f", rtf)}x
                    💾 Saved: ${outputFile.absolutePath}
                    🎙️ Voice: $selectedVoice | 🌐 Lang: $selectedLangCode
                """.trimIndent()
                
                binding.btnGenerate.isEnabled = true
                
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.textViewStatus.text = "❌ Error: ${e.message}"
                binding.btnGenerate.isEnabled = true
                Toast.makeText(this@MainActivity, "Generation failed", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showModelInfo() {
        val info = tts.getModelInfo()
        binding.textViewStatus.text = info
    }
    
    private fun playAudio(samples: FloatArray, sampleRate: Int) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 4)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        
        audioTrack.play()
        audioTrack.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
        audioTrack.stop()
        audioTrack.release()
    }
    
    private fun saveWav(samples: FloatArray, sampleRate: Int, file: File) {
        FileOutputStream(file).use { fos ->
            val dataSize = samples.size * 2 // 16-bit PCM
            val fileSize = 36 + dataSize
            
            // WAV header
            fos.write("RIFF".toByteArray())
            fos.write(intToByteArray(fileSize))
            fos.write("WAVE".toByteArray())
            fos.write("fmt ".toByteArray())
            fos.write(intToByteArray(16)) // Subchunk1Size
            fos.write(shortToByteArray(1.toShort())) // AudioFormat (PCM)
            fos.write(shortToByteArray(1.toShort())) // NumChannels
            fos.write(intToByteArray(sampleRate))
            fos.write(intToByteArray(sampleRate * 2)) // ByteRate
            fos.write(shortToByteArray(2.toShort())) // BlockAlign
            fos.write(shortToByteArray(16.toShort())) // BitsPerSample
            fos.write("data".toByteArray())
            fos.write(intToByteArray(dataSize))
            
            // Convert float samples to 16-bit PCM
            val buffer = ShortArray(samples.size)
            for (i in samples.indices) {
                buffer[i] = (samples[i] * 32767).toInt().toShort()
            }
            
            // Write samples
            val byteBuffer = ByteArray(buffer.size * 2)
            for (i in buffer.indices) {
                byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
                byteBuffer[i * 2 + 1] = (buffer[i].toInt() >> 8 and 0xFF).toByte()
            }
            fos.write(byteBuffer)
        }
    }
    
    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            (value >> 8 and 0xFF).toByte(),
            (value >> 16 and 0xFF).toByte(),
            (value >> 24 and 0xFF).toByte()
        )
    }
    
    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            (value.toInt() >> 8 and 0xFF).toByte()
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        tts.release()
    }
}
