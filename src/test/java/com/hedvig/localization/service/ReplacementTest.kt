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
import assertk.assertThat
import assertk.assertions.isEqualTo

class ReplacementTest {
    private val client: LocalizationClient = mockk()

    @Test
    fun `should handle replacements correctly`() {
        every { client.fetchLocalization(any()) } returns LocalizationResponse(
            LocalizationData(
                listOf(
                    Language(
                        listOf(
                            Translation(
                                Key(
                                    "SOME_KEY_NAME"
                                ),
                                "Som tack får både du och dina vänner [%1\$i:REFERRAL_VALUE] kr lägre månadskostnad. Fortsätt bjuda in vänner för att sänka ditt pris ännu mer!"
                            )
                        ), "sv_SE"
                    )
                )
            )
        )

        val resolver = TextKeysLocaleResolverImpl()
        val locale = resolver.resolveLocale("sv-se")
        val localizationService =
            LocalizationServiceImpl(client, "someProject")

        val result = localizationService.getText(locale, "SOME_KEY_NAME", mapOf("REFERRAL_VALUE" to "10"))

        assertThat(result).isEqualTo("Som tack får både du och dina vänner 10 kr lägre månadskostnad. Fortsätt bjuda in vänner för att sänka ditt pris ännu mer!")
    }
}