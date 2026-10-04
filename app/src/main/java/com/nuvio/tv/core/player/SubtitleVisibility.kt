package com.nuvio.tv.core.player

/** Keep user imports across asynchronous provider snapshots, independently of language filters. */
object SubtitleVisibility {
    fun <T> merge(provider: List<T>, local: List<T>, key: (T) -> String): List<T> =
        (provider + local).distinctBy(key)

    fun <T> filter(options: List<T>, isLocal: (T) -> Boolean, matchesLanguage: (T) -> Boolean): List<T> =
        options.filter { isLocal(it) || matchesLanguage(it) }
}
