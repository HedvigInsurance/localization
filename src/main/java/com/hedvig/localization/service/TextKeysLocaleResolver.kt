package com.hedvig.localization.service

import java.util.Locale

interface TextKeysLocaleResolver {
    fun resolveLocale(acceptLanguage: String?): Locale
}