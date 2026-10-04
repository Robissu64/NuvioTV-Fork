package com.nuvio.tv.core.server

import com.nuvio.tv.core.player.LocalSubtitleFiles
import fi.iki.elonen.NanoHTTPD
import java.io.EOFException
import java.io.InputStream
import java.net.URLDecoder
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/** Ephemeral LAN upload. Available only while its QR is visible; no filesystem endpoint. */
class LocalSubtitleTransferServer(
    private val onSubtitle: (String, ByteArray) -> Unit,
    private val pageTitle: String,
    private val chooseLabel: String,
    private val successLabel: String,
    private val errorLabel: String
) : NanoHTTPD(0) {
    val token: String = UUID.randomUUID().toString()
    private val startedAt = System.currentTimeMillis()
    private val uploading = AtomicBoolean(false)

    override fun serve(session: IHTTPSession): Response {
        if (session.uri != "/$token" || System.currentTimeMillis() - startedAt > 10 * 60_000L) {
            return reply(Response.Status.NOT_FOUND, errorLabel)
        }
        if (session.method == Method.GET) {
            return newFixedLengthResponse(Response.Status.OK, "text/html; charset=utf-8", html())
                .apply { addHeader("Cache-Control", "no-store"); addHeader("Referrer-Policy", "no-referrer") }
        }
        if (session.method != Method.POST) return reply(Response.Status.METHOD_NOT_ALLOWED, errorLabel)
        val size = session.headers["content-length"]?.toLongOrNull()
            ?: return reply(Response.Status.BAD_REQUEST, errorLabel)
        if (size !in 1..LocalSubtitleFiles.MAX_BYTES.toLong()) return reply(Response.Status.BAD_REQUEST, errorLabel)
        if (!uploading.compareAndSet(false, true)) return reply(Response.Status.BAD_REQUEST, errorLabel)
        return try {
            val name = URLDecoder.decode(session.headers["x-subtitle-name"].orEmpty(), "UTF-8").take(200)
            val extension = LocalSubtitleFiles.extension(name) ?: error("Unsupported format")
            // Never use parseBody(): it can spool an unlimited multipart body to disk.
            val input = object : InputStream() {
                var remaining = size
                override fun read(): Int {
                    if (remaining <= 0) return -1
                    val value = session.inputStream.read()
                    if (value < 0) throw EOFException()
                    remaining--
                    return value
                }
                override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                    if (remaining <= 0) return -1
                    val count = session.inputStream.read(buffer, offset, minOf(length.toLong(), remaining).toInt())
                    if (count < 0) throw EOFException()
                    remaining -= count
                    return count
                }
            }
            val bytes = LocalSubtitleFiles.readBounded(input)
            LocalSubtitleFiles.decodeAndValidate(bytes, extension)
            onSubtitle(name, bytes)
            reply(Response.Status.OK, successLabel)
        } catch (_: Exception) {
            reply(Response.Status.BAD_REQUEST, errorLabel)
        } finally {
            uploading.set(false)
        }
    }

    private fun reply(status: Response.Status, message: String) =
        newFixedLengthResponse(status, "text/plain; charset=utf-8", message).apply {
            addHeader("Connection", "close")
            addHeader("Cache-Control", "no-store")
        }

    private fun html(): String = """
        <!doctype html><html lang="pt-BR"><meta charset="utf-8">
        <meta name="viewport" content="width=device-width,initial-scale=1">
        <title>$pageTitle</title>
        <style>body{font:18px system-ui;max-width:36em;margin:3em auto;padding:1em;background:#151515;color:#fff}input,button{font:inherit;margin:1em 0;max-width:100%}</style>
        <h1>$pageTitle</h1><p>SRT, VTT, ASS, SSA · 8 MiB</p>
        <input id="file" type="file" accept=".srt,.vtt,.ass,.ssa"><br>
        <button id="send">$chooseLabel</button><p id="status"></p>
        <script>
        document.getElementById('send').onclick=async()=>{
          const f=document.getElementById('file').files[0],s=document.getElementById('status'),b=document.getElementById('send');
          if(!f||f.size>8388608){s.textContent='$errorLabel';return;}
          b.disabled=true;
          try{const r=await fetch(location.pathname,{method:'POST',headers:{'Content-Type':'application/octet-stream','X-Subtitle-Name':encodeURIComponent(f.name)},body:f});s.textContent=await r.text();}
          catch(e){s.textContent='$errorLabel';}finally{b.disabled=false;}
        };
        </script></html>
    """.trimIndent()
}
