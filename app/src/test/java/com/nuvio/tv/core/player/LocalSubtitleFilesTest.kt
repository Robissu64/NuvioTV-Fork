package com.nuvio.tv.core.player

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.concurrent.CancellationException

class LocalSubtitleFilesTest {
    private val srt = "1\n00:00:01,000 --> 00:00:02,000\nAção, coração e você.\n"

    @Test fun supportedExtensionsAreCaseInsensitiveAndDoNotAcceptArchives() {
        assertEquals("ssa", LocalSubtitleFiles.extension("Filme.PT-BR.SSA"))
        assertEquals("ass", LocalSubtitleFiles.extension("../../legenda.ass"))
        assertNull(LocalSubtitleFiles.extension("legenda.srt.zip"))
        assertNull(LocalSubtitleFiles.extension("sem_extensao"))
    }

    @Test fun srtUtf8AndWindows1252KeepBrazilianAccentsWithoutLanguageGuess() {
        for (charset in listOf(Charsets.UTF_8, charset("windows-1252"))) {
            assertEquals(srt, LocalSubtitleFiles.decodeAndValidate(srt.toByteArray(charset), "srt"))
        }
    }

    @Test fun utf16BomAndVttAreAccepted() {
        val text = "WEBVTT\n\n00:01.000 --> 00:02.000\nOlá!\n"
        val bytes = byteArrayOf(0xff.toByte(), 0xfe.toByte()) + text.toByteArray(Charsets.UTF_16LE)
        assertEquals(text, LocalSubtitleFiles.decodeAndValidate(bytes, "vtt"))
    }

    @Test fun assAndSsaKeepStyleDirectives() {
        val text = "[Script Info]\nTitle: Ação\n[V4+ Styles]\nStyle: Default,Arial,24\n[Events]\nDialogue: 0,0:00:01.00,0:00:02.00,Default,,0,0,0,,{\\i1}Olá{\\i0}\n"
        for (extension in listOf("ass", "ssa")) {
            assertEquals(text, LocalSubtitleFiles.decodeAndValidate(text.toByteArray(), extension))
        }
    }

    @Test fun rejectsEmptyBinaryAndHtmlEvenWithSubtitleExtension() {
        for (bytes in listOf(byteArrayOf(), "<html>error</html>".toByteArray(), (srt + '\u0000').toByteArray())) {
            try { LocalSubtitleFiles.decodeAndValidate(bytes, "srt"); fail("Invalid file accepted") }
            catch (_: IllegalArgumentException) {}
        }
    }

    @Test fun rejectsOversizeBeforeReadingWholeStream() {
        var read = 0
        val input = object : InputStream() {
            override fun read(): Int { read++; return 65 }
            override fun read(b: ByteArray, off: Int, len: Int): Int { read += len; return len }
        }
        try { LocalSubtitleFiles.readBounded(input); fail("Unlimited stream accepted") }
        catch (_: IllegalArgumentException) {}
        assertTrue(read <= LocalSubtitleFiles.MAX_BYTES + 8192)
    }

    @Test fun exactLimitIsAllowed() {
        val bytes = ByteArray(LocalSubtitleFiles.MAX_BYTES) { 65 }
        assertArrayEquals(bytes, LocalSubtitleFiles.readBounded(ByteArrayInputStream(bytes)))
    }

    @Test(expected = CancellationException::class)
    fun cancellationIsPropagated() {
        LocalSubtitleFiles.readBounded(ByteArrayInputStream(srt.toByteArray())) { throw CancellationException() }
    }
}
