package com.hedvig.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class TextKeysLocaleResolver {

  fun resolveLocale(acceptLanguage: String?): Locale {
    if (acceptLanguage.isNullOrBlank()) {
      return DEFAULT_LOCALE
    }

    return try {
      val list = Locale.LanguageRange.parse(acceptLanguage)
      Locale.lookup(list, LOCALES) ?: DEFAULT_LOCALE
    } catch (e: IllegalArgumentException) {
      log.error("IllegalArgumentException when parsing acceptLanguage: '$acceptLanguage' message: ${e.message}")
      DEFAULT_LOCALE
    }
  }

  companion object {
    private val LOCALES = listOf(
      Locale("en"),
      Locale("sv")
    )

    val DEFAULT_LOCALE = Locale("sv")
    val log = LoggerFactory.getLogger(this::class.java)
  }
}
