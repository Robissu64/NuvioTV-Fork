package com.nuvio.tv.core.player

import org.junit.Assert.*
import org.junit.Test

class SubtitleVisibilityTest {
    private data class Option(val key: String, val lang: String, val local: Boolean = false)
    private val local = Option("file", "und", true)
    private val brazil = Option("br", "pt-br")
    private val portugal = Option("pt", "pt-pt")

    @Test fun asynchronousProviderSnapshotKeepsLocalChoiceAndDoesNotGuessItsLanguage() {
        val options = SubtitleVisibility.merge(listOf(brazil, portugal), listOf(local)) { it.key }
        val visible = SubtitleVisibility.filter(options, { it.local }, { it.lang == "pt-br" })
        assertEquals(listOf(brazil, local), visible)
        assertEquals("und", visible.last().lang)
    }

    @Test fun emptyPreferredLanguagesStillExposeUserImport() {
        val visible = SubtitleVisibility.filter(listOf(brazil, local), { it.local }, { false })
        assertEquals(listOf(local), visible)
    }

    @Test fun snapshotContainingLocalChoiceDoesNotDuplicateIt() {
        val options = SubtitleVisibility.merge(listOf(brazil, local), listOf(local)) { it.key }
        assertEquals(listOf(brazil, local), options)
    }

    @Test fun emptyProviderAndUnrestrictedLanguagesPreserveOrdering() {
        assertEquals(listOf(local), SubtitleVisibility.merge(emptyList(), listOf(local)) { it.key })
        val visible = SubtitleVisibility.filter(listOf(portugal, brazil, local), { it.local }, { true })
        assertEquals(listOf(portugal, brazil, local), visible)
    }
}
