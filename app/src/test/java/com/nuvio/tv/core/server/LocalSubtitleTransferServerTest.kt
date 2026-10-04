package com.nuvio.tv.core.server

import org.junit.Assert.*
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.atomic.AtomicInteger

class LocalSubtitleTransferServerTest {
    @Test fun uploadRequiresTokenAndValidSubtitleAndKeepsFilenameAsData() {
        val received = AtomicInteger()
        val server = LocalSubtitleTransferServer({ name, bytes ->
            assertEquals("../../Ação.srt", name)
            assertTrue(String(bytes).contains("Olá"))
            received.incrementAndGet()
        }, "Nuvio", "Send", "Received", "Invalid")
        server.start(3000, true)
        try {
            val valid = "1\n00:00:01,000 --> 00:00:02,000\nOlá\n".toByteArray()
            assertEquals(404, request(server, "wrong-token", "../../Ação.srt", valid))
            assertEquals(400, request(server, server.token, "movie.mkv", valid))
            assertEquals(400, request(server, server.token, "bad.srt", "<html>error</html>".toByteArray()))
            assertEquals(0, received.get())
            assertEquals(200, request(server, server.token, "../../Ação.srt", valid))
            assertEquals(1, received.get())
        } finally { server.stop() }
    }

    @Test fun rejectsOversizeDeclaredLengthWithoutReceivingBody() {
        val server = LocalSubtitleTransferServer({ _, _ -> fail("Oversize accepted") }, "Nuvio", "Send", "Received", "Invalid")
        server.start(3000, true)
        try {
            java.net.Socket("127.0.0.1", server.listeningPort).use { socket ->
                socket.soTimeout = 3000
                val header = "POST /${server.token} HTTP/1.1\r\nHost: localhost\r\nContent-Length: 8388609\r\nX-Subtitle-Name: test.srt\r\n\r\n"
                socket.getOutputStream().write(header.toByteArray())
                val status = socket.getInputStream().bufferedReader().readLine()
                assertTrue(status.contains("400"))
            }
        } finally { server.stop() }
    }

    private fun request(server: LocalSubtitleTransferServer, token: String, name: String, bytes: ByteArray): Int {
        val connection = URL("http://127.0.0.1:${server.listeningPort}/$token").openConnection() as HttpURLConnection
        connection.connectTimeout = 3000
        connection.readTimeout = 3000
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setFixedLengthStreamingMode(bytes.size)
        connection.setRequestProperty("X-Subtitle-Name", URLEncoder.encode(name, "UTF-8"))
        return try {
            connection.outputStream.use { it.write(bytes) }
            connection.responseCode
        } finally { connection.disconnect() }
    }
}
