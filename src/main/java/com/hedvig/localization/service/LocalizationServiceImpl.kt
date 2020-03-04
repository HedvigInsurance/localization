package com.hedvig.localization.service

import com.hedvig.localization.client.GraphQLQueryWrapper
import com.hedvig.localization.client.LocalizationClient
import com.hedvig.localization.client.dto.LocalizationData
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

  override fun getText(locale: Locale?, key: String): String? {
    val language =
      parseLanguage(locale)
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
    private fun parseLanguage(locale: Locale?): String = locale?.toString() ?: "sv_SE"

    private const val TEN_MINUTES_MS = 600000L
  }
}
