package com.hedvig.service

import com.hedvig.client.GraphQLQueryWrapper
import com.hedvig.client.LocalizationClient
import com.hedvig.client.dto.LocalizationData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import kotlin.concurrent.timer

@Component
class LocalizationService @Autowired constructor(
        private val localizationClient: LocalizationClient,
        @Value("\${graphcms.project}") private val graphcmsProject: String
) {

  private var localizationData: LocalizationData? = null

  private val graphcmsTextKeysQuery = GraphQLQueryWrapper(
          "{languages {translations(where: { project: $graphcmsProject }) {text key {value}} code}}"
  )

  init {
    timer("refreshLocalizations", false, 0, TEN_MINUTES_MS) {
      this@LocalizationService.refreshLocalizations()
    }
  }

  private fun refreshLocalizations() {
    val data = fetchLocalizations()
    if (data != localizationData) {
      synchronized(this@LocalizationService) {
        localizationData = data
      }
    }
  }

  fun getText(locale: Locale?, key: String): String? {
    val language = parseLanguage(locale)
    localizationData?.let { data ->
      return data.getText(language, key)
    } ?: run {
      // Let's retry
      refreshLocalizations()
      return localizationData?.getText(language, key)
    }
  }


  private fun fetchLocalizations(): LocalizationData =
    localizationClient.fetchLocalization(graphcmsTextKeysQuery).data

  private fun LocalizationData.getText(language: String, key: String) =
    languages
      .firstOrNull { it.code == language }
      ?.translations
      ?.firstOrNull { it.key?.value == key }
      ?.text

  companion object {
    private fun parseLanguage(locale: Locale?): String {
      return when {
        locale.isLanguage(Language.ENGLISH.localeLanguage) -> Language.ENGLISH.graphCMSLanguage
        locale.isLanguage(Language.SWEDISH.localeLanguage) -> Language.SWEDISH.graphCMSLanguage
        else -> Language.SWEDISH.graphCMSLanguage
      }
    }

    fun getFileIdentifier(locale: Locale?): String =
      when {
        locale.isLanguage(Language.ENGLISH.localeLanguage) -> Language.ENGLISH.fileIdentifier
        locale.isLanguage(Language.SWEDISH.localeLanguage) -> Language.SWEDISH.fileIdentifier
        else -> Language.SWEDISH.fileIdentifier
      }

    private fun Locale?.isLanguage(language: String) =
      this?.language.equals(Locale(language).language)

    private const val TEN_MINUTES_MS = 600000L
  }

  private enum class Language(val localeLanguage: String, val graphCMSLanguage: String, val fileIdentifier: String) {
    ENGLISH("en", "en_SE", " (English)"),
    SWEDISH("sv", "sv_SE", "")
  }
}
