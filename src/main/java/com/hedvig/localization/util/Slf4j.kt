package com.hedvig.localization.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T : Any> T.logger(): Lazy<Logger> = lazy {
    LoggerFactory.getLogger(this.javaClass)
}