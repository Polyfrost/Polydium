package org.polyfrost.polydium.test

import com.ibm.icu.text.Bidi
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polydium.client.perf.LtrText
import org.polyfrost.polydium.client.perf.TranslationTemplate

class TxTextFastPathTest {
    private fun icuSaysSingleLtrRun(text: String): Boolean {
        val bidi = Bidi(text, Bidi.LEVEL_DEFAULT_LTR.toInt())
        bidi.setReorderingMode(Bidi.REORDER_DEFAULT.toInt())
        if (bidi.countRuns() != 1) return false
        val run = bidi.getVisualRun(0)
        return !run.isOddRun && run.start == 0 && run.length == text.length
    }

    @Test
    fun `every code point the fast path accepts is a single LTR run in ICU`() {
        for (codePoint in 0..0x10FFFF) {
            if (codePoint in 0xD800..0xDFFF) continue
            val ch = String(Character.toChars(codePoint))
            for (text in arrayOf(ch, "a $ch 12 (b)\n$ch", "1$ch")) {
                if (LtrText.isSingleLtrRun(text)) {
                    assertTrue(icuSaysSingleLtrRun(text)) { "U+%04X accepted but ICU reorders it".format(codePoint) }
                }
            }
        }
    }

    @Test
    fun `plain text takes the fast path and RTL text does not`() {
        assertTrue(LtrText.isSingleLtrRun(""))
        assertTrue(LtrText.isSingleLtrRun("Hello (world) 123 😀 é中"))
        assertFalse(LtrText.isSingleLtrRun("abc א"), "Hebrew letter")
        assertFalse(LtrText.isSingleLtrRun("abc ا"), "Arabic letter")
        assertFalse(LtrText.isSingleLtrRun("abc ١"), "Arabic-Indic digit")
        assertFalse(LtrText.isSingleLtrRun("abc ‏"), "RLM")
        assertFalse(LtrText.isSingleLtrRun("abc ‮"), "RLO")
        assertFalse(LtrText.isSingleLtrRun("abc ⁧"), "RLI")
        assertFalse(LtrText.isSingleLtrRun("abc 𞤀"), "Adlam, supplementary RTL")
    }

    @Test
    fun `templates parse into vanilla's parts`() {
        val percent = TranslationTemplate.PERCENT
        assertArrayEquals(arrayOf<Any>(0, " hit ", 1, " for 100", percent), TranslationTemplate.parse("%s hit %s for 100%%"))
        assertArrayEquals(arrayOf<Any>(1, " ", 0, " ", 0), TranslationTemplate.parse("%2\$s %1\$s %s"))
        assertArrayEquals(arrayOf<Any>(-1), TranslationTemplate.parse("%0\$s"), "index is range-checked per component, not here")
        assertArrayEquals(arrayOf<Any>("plain"), TranslationTemplate.parse("plain"))
        assertEquals(0, TranslationTemplate.parse("")!!.size)
    }

    @Test
    fun `templates vanilla rejects are left to vanilla`() {
        for (bad in arrayOf("100%", "%d items", "50% off", "%1\$%", "%99999999999\$s", "%s and %")) {
            assertNull(TranslationTemplate.parse(bad), bad)
            assertNull(TranslationTemplate.parts(bad, "%") { it }, bad)
        }
    }

    @Test
    fun `cached parts are converted once and reused`() {
        val first = TranslationTemplate.parts("%s: %%!", "PCT") { "lit:$it" }
        assertArrayEquals(arrayOf<Any>(0, "lit:: ", "PCT", "lit:!"), first)
        assertTrue(first === TranslationTemplate.parts("%s: %%!", "other") { "x" })
    }
}
