package com.hedvig.localization.service

import com.hedvig.localization.client.LocalizationClient
import com.hedvig.localization.client.dto.Key
import com.hedvig.localization.client.dto.Language
import com.hedvig.localization.client.dto.LocalizationData
import com.hedvig.localization.client.dto.LocalizationResponse
import com.hedvig.localization.client.dto.Translation
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class LocalizationTest {

    var localizationClient: LocalizationClient = mockk()

    @Test
    fun `lookup key with se-sv locale`() {

        every { localizationClient.fetchLocalization(any()) } returns LocalizationResponse(
            LocalizationData(
                listOf(
                    Language(
                        listOf(
                            Translation(
                                Key(
                                    "SOME_KEY_NAME"
                                ), "Försäkring"
                            )
                        ), "sv_SE"
                    )
                )
            )
        )


        val resolver = TextKeysLocaleResolverImpl()
        val locale = resolver.resolveLocale("sv-se")
        val localizationService =
            LocalizationServiceImp(localizationClient, "someProject")

        val result = localizationService.getText(locale, "SOME_KEY_NAME")

        assert(result == "Försäkring")
    }

    @Test
    fun `lookup no-no key`() {
        every { localizationClient.fetchLocalization(any()) } returns LocalizationResponse(
            LocalizationData(
                listOf(
                    Language(
                        listOf(
                            Translation(
                                Key("SOME_KEY_NAME"),
                                "Försäkring"
                            )
                        ), "sv_SE"
                    ),
                    Language(
                        listOf(
                            Translation(
                                Key(
                                    "SOME_KEY_NAME"
                                ), "Forsikring"
                            )
                        ), "nb_NO"
                    )
                )
            )
        )


        val resolver = TextKeysLocaleResolverImpl()
        val locale = resolver.resolveLocale("nb-NO")
        val localizationService =
            LocalizationServiceImp(localizationClient, "someProject")

        val result = localizationService.getText(locale, "SOME_KEY_NAME")

        assert(result == "Forsikring")
    }
}