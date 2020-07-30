package com.hedvig.localization.service

import com.hedvig.localization.client.GraphQLQueryWrapper
import com.hedvig.localization.client.LocalizationClient
import com.hedvig.localization.client.dto.LocalizationData
import com.hedvig.localization.util.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import kotlin.concurrent.timer

@Component
class LocalizationServiceImpl @Autowired constructor(
        private val localizationClient: LocalizationClient,
        @Value("\${graphcms.project}") private val graphcmsProject: String
) : LocalizationService {

  private val logger by logger()

  private var localizationData: LocalizationData? = null

  private val graphcmsTextKeysQuery = GraphQLQueryWrapper(
    "{languages {translations(where: { project: $graphcmsProject }) {text key {value}} code}}"
  )

  init {
    timer("refreshLocalizations", false, 0,
      TEN_MINUTES_MS
    ) {
      this@LocalizationServiceImpl.refreshLocalizations()
    }
  }

  private fun refreshLocalizations() {
    val data = fetchLocalizations()
    if (data != localizationData) {
      synchronized(this@LocalizationServiceImpl) {
        localizationData = data
      }
    }
  }

  override fun getText(locale: Locale?, key: String, replacements: Map<String, String>): String? {
    val language =
      parseLanguage(locale)
    localizationData?.let { data ->
      return data.getText(language, key)
    } ?: run {
      // Let's retry
      refreshLocalizations()
      val translation = localizationData?.getText(language, key) ?: return null

      return lokalisePlaceholderSyntaxMatcher
        .findAll(translation)
        .fold(translation) { acc, curr ->
          val replacementName = curr.groups[1]?.value
          if (replacementName == null) {
            logger.warn("Unable to find replacement name in key: $key with translation: $translation")
            return acc
          }
          val replacement = replacements[replacementName]
          if (replacement == null) {
            logger.warn("Requested replacement: $replacementName not found in key: $key with translation: $translation")
            return acc
          }
          return lokalisePlaceholderSyntaxMatcher.replaceFirst(acc, replacement)
        }
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
    private fun parseLanguage(locale: Locale?): String = locale?.toString() ?: "sv_SE"

    private const val TEN_MINUTES_MS = 600000L
    private val lokalisePlaceholderSyntaxMatcher = Regex("\\[\\%\\d+\\\$[sif]\\:(.+?)\\]")
  }
}
