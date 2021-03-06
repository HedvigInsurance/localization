package com.hedvig.localization.service

import java.util.Locale

interface LocalizationService {
  fun getText(locale: Locale?, key: String): String?
}