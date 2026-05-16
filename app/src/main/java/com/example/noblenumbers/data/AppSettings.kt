package com.example.noblenumbers.data

data class AppSettings(
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = false,
    val languageTag: String = DEFAULT_LANGUAGE,
) {
    companion object {
        const val DEFAULT_LANGUAGE = "en"
        val supportedLanguages = setOf("en", "ru")
    }
}
