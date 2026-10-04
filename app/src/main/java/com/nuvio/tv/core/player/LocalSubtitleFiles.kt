package com.nuvio.tv.core.player

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Locale

/** Shared limits/validation for document imports and phone uploads. No video IO. */
object LocalSubtitleFiles {
    const val MAX_BYTES = 8 * 1024 * 1024

    fun extension(name: String): String? = name.substringAfterLast('.', "")
        .lowercase(Locale.ROOT).takeIf { it in setOf("srt", "vtt", "ass", "ssa") }

    fun readBounded(input: InputStream, checkCancelled: () -> Unit = {}): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            checkCancelled()
            val count = input.read(buffer)
            if (count < 0) break
            require(output.size().toLong() + count <= MAX_BYTES) { "Subtitle exceeds 8 MiB" }
            output.write(buffer, 0, count)
        }
        require(output.size() > 0) { "Empty subtitle" }
        return output.toByteArray()
    }

    fun decodeAndValidate(bytes: ByteArray, extension: String): String {
        require(bytes.size in 1..MAX_BYTES)
        val text = SubtitleCharsetDetector.decode(bytes)
        require(!text.contains('\u0000')) { "Invalid subtitle text" }
        val valid = when (extension) {
            "srt", "vtt" -> Regex("(?m)\\d{1,2}:\\d{2}(?::\\d{2})?[,.]\\d{3}\\s+-->\\s+\\d{1,2}:\\d{2}").containsMatchIn(text)
            "ass", "ssa" -> text.contains("[Events]", true) && Regex("(?im)^\\s*Dialogue:").containsMatchIn(text)
            else -> false
        }
        require(valid) { "Invalid subtitle format" }
        return text
    }
}
