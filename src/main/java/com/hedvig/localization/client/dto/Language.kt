package com.hedvig.localization.client.dto

data class Language(
    val translations: List<Translation>,
    val code: String
)
