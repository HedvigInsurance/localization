package com.hedvig.service

import com.hedvig.client.LocalizationClient
import com.hedvig.client.dto.Key
import com.hedvig.client.dto.Language
import com.hedvig.client.dto.LocalizationData
import com.hedvig.client.dto.LocalizationResponse
import com.hedvig.client.dto.Translation
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class LocalizationTest {

    var localizationClient: LocalizationClient = mockk()

    @Test
    fun `lookup key with se-sv locale`() {

        every { localizationClient.fetchLocalization(any()) } returns LocalizationResponse(LocalizationData(listOf(Language(
            listOf(Translation(Key("SOME_KEY_NAME"), "Försäkring")), "sv_SE"))))


        val resolver = TextKeysLocaleResolver()
        val locale = resolver.resolveLocale("sv-se")
        val localizationService = LocalizationService(localizationClient, "someProject")

        val result = localizationService.getText(locale, "SOME_KEY_NAME")

        assert(result == "Försäkring")
    }

    @Test
    fun `lookup no-no key`() {
        every { localizationClient.fetchLocalization(any()) } returns LocalizationResponse(LocalizationData(listOf(
            Language(listOf(Translation(Key("SOME_KEY_NAME"), "Försäkring")), "sv_SE"),
            Language(
                listOf(Translation(Key("SOME_KEY_NAME"), "Forsikring")), "nb_NO")
            )))


        val resolver = TextKeysLocaleResolver()
        val locale = resolver.resolveLocale("nb-NO")
        val localizationService = LocalizationService(localizationClient, "someProject")

        val result = localizationService.getText(locale, "SOME_KEY_NAME")

        assert(result == "Forsikring")
    }

    @Test
    fun fileKeys() {
        every { localizationClient.fetchLocalization(any()) } returns LocalizationResponse(LocalizationData(listOf()))

        val resolver = TextKeysLocaleResolver()
        val locale = resolver.resolveLocale("sv-se")

        val result = LocalizationService.getFileIdentifier(locale)

        assert(result == "")
    }

    @Test
    fun `getFileIdentifier en-se`() {
        every { localizationClient.fetchLocalization(any()) } returns LocalizationResponse(LocalizationData(listOf()))


        val resolver = TextKeysLocaleResolver()
        val locale = resolver.resolveLocale("en-se")

        val result = LocalizationService.getFileIdentifier(locale)

        assert(result == " (English)")
    }

    @Test
    fun `getFileIdentifier en-no`() {
        every { localizationClient.fetchLocalization(any()) } returns LocalizationResponse(LocalizationData(listOf()))


        val resolver = TextKeysLocaleResolver()
        val locale = resolver.resolveLocale("en-no")

        val result = LocalizationService.getFileIdentifier(locale)

        assert(result == " (English)")
    }

    @Test
    fun `getFileIdentifier nb-no`() {

        every { localizationClient.fetchLocalization(any()) } returns LocalizationResponse(LocalizationData(listOf()))

        val resolver = TextKeysLocaleResolver()
        val locale = resolver.resolveLocale("nb-no")

        val result = LocalizationService.getFileIdentifier(locale)

        assert(result == "")
    }
}