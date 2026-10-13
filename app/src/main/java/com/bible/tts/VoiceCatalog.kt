package com.bible.tts

data class Voice(
    val displayName: String,
    val id: String
)

data class LanguageConfig(
    val displayName: String,
    val langCode: String,
    val voices: List<Voice>
)

object VoiceCatalog {
    val languages = listOf(
        LanguageConfig(
            displayName = "English (US)",
            langCode = "en-us",
            voices = listOf(
                Voice("am_adam (Deep, Warm, Authoritative)", "am_adam")
            )
        ),
        LanguageConfig(
            displayName = "Hindi",
            langCode = "hi",
            voices = listOf(
                Voice("hm_omega (Resonant, Calm, Respectful)", "hm_omega")
            )
        )
    )
    
    fun getDefaultText(langCode: String): String {
        return when (langCode) {
            "hi" -> "यहोवा मेरा चरवाहा है, मुझे कुछ भी घाटा नहीं होगा। वह मुझे हरी-हरी चराइयों में चराता है, और मुझे जल के शांत तटों के पास ले जाता है।"
            else -> "The Lord is my shepherd; I shall not want. He maketh me to lie down in green pastures: he leadeth me beside the still waters."
        }
    }
}
