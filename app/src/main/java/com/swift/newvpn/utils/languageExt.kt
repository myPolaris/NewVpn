package com.swift.newvpn.utils


val languageList = mutableListOf(
    "en" to "English",
    "pt" to "Português",
    "zh" to "中文繁體",
    "ko" to "한국인",
    "ja" to "日本語",
    "es" to "Español",
    "de" to "Deutsch",
    "ru" to "Русский язык",
    "fr" to "Français",
)

fun String.toLocale() = when (this) {
    "pt" -> LocalLanguageSet.getPortugalLocale()
    "zh" -> LocalLanguageSet.getChineseLocale()
    "ko" -> LocalLanguageSet.getKoreanLocale()
    "ja" -> LocalLanguageSet.getJapaneseLocale()
    "es" -> LocalLanguageSet.getSpainLocale()
    "de" -> LocalLanguageSet.getGermanLocale()
    "ru" -> LocalLanguageSet.getRussiaLocale()
    "fr" -> LocalLanguageSet.getFrenchLocale()
    else -> LocalLanguageSet.getEnglishLocale()
}


