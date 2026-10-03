package com.elxvro.skytower.ui.assets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UiEmbeddedAssetPackTest {
    @Test
    fun parsesValidEntriesAndIgnoresMalformedLines() {
        val parsed = UiEmbeddedAssetPack.parse(
            """
            ui/home/background.webp|QUJD

            malformed
            ui/home/logo.webp|REVG
            """.trimIndent(),
        )

        assertEquals("QUJD", parsed["ui/home/background.webp"])
        assertEquals("REVG", parsed["ui/home/logo.webp"])
        assertNull(parsed["malformed"])
    }
}
