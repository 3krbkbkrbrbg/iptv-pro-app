package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

data class Channel(
    val name: String,
    val streamUrl: String,
    val logoUrl: String = "",
    val groupName: String = "",
    val isRadio: Boolean = false
)

class IptvRepository(
    private val dao: IptvDao,
    private val client: OkHttpClient = OkHttpClient()
) {
    val allPlaylists: Flow<List<PlaylistEntity>> = dao.getAllPlaylists()
    val allFavorites: Flow<List<FavoriteEntity>> = dao.getAllFavorites()
    val recentPlays: Flow<List<RecentPlayEntity>> = dao.getRecentPlaysFlow()

    fun isFavoriteFlow(url: String): Flow<Boolean> = dao.isFavoriteFlow(url)

    suspend fun addFavorite(name: String, streamUrl: String, logoUrl: String, groupName: String, isRadio: Boolean) {
        dao.insertFavorite(
            FavoriteEntity(
                name = name,
                streamUrl = streamUrl,
                logoUrl = logoUrl,
                groupName = groupName,
                isRadio = isRadio
            )
        )
    }

    suspend fun removeFavorite(url: String) {
        dao.deleteFavoriteByUrl(url)
    }

    suspend fun isFavorite(url: String): Boolean = dao.isFavorite(url)

    suspend fun addRecentPlay(name: String, streamUrl: String, logoUrl: String, groupName: String, isRadio: Boolean) {
        // Remove existing to bring to top of list
        dao.deleteRecentPlayByUrl(streamUrl)
        dao.insertRecentPlay(
            RecentPlayEntity(
                name = name,
                streamUrl = streamUrl,
                logoUrl = logoUrl,
                groupName = groupName,
                isRadio = isRadio,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }

    suspend fun deletePlaylist(id: Long) {
        dao.deletePlaylistById(id)
    }

    suspend fun importPlaylist(name: String, url: String, content: String = "", isCustom: Boolean = false): Long {
        return dao.insertPlaylist(
            PlaylistEntity(
                name = name,
                url = url,
                content = content,
                isCustom = isCustom,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    /**
     * Fetches M3U text content from a remote URL
     */
    suspend fun fetchRemotePlaylist(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed to download playlist: HTTP ${response.code}")
            response.body?.string() ?: throw IOException("Empty response body")
        }
  }

    /**
     * Parses M3U format text into a list of Channels
     */
    fun parseM3uContent(m3uText: String): List<Channel> {
        val lines = m3uText.lines()
        val list = mutableListOf<Channel>()
        var currentName = ""
        var currentLogo = ""
        var currentGroup = "General"

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#EXTINF:")) {
                // Parse TVG Logo
                val logoRegex = """tvg-logo="([^"]+)"""".toRegex()
                val logoMatch = logoRegex.find(trimmed)
                currentLogo = logoMatch?.groupValues?.get(1) ?: ""

                // Try to resolve empty logo using wsrv.nl proxy as in worker to reduce size and handle security if needed,
                // but direct loading via Coil is fine. Let's keep original and fallback in Coil.

                // Parse Group Title
                val groupRegex = """group-title="([^"]+)"""".toRegex()
                val groupMatch = groupRegex.find(trimmed)
                currentGroup = groupMatch?.groupValues?.get(1) ?: "General"

                // Parse Name (after the last comma)
                val lastCommaIndex = trimmed.lastIndexOf(',')
                currentName = if (lastCommaIndex != -1) {
                    trimmed.substring(lastCommaIndex + 1).trim()
                } else {
                    ""
                }
            } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                val name = currentName.ifEmpty { "Channel ${list.size + 1}" }

                val lowerName = name.lowercase()
                val isRadio = lowerName.contains("radio") || 
                              lowerName.contains(" fm") || 
                              currentGroup.lowercase().contains("radio")

                list.add(
                    Channel(
                        name = name,
                        streamUrl = trimmed,
                        logoUrl = currentLogo,
                        groupName = currentGroup,
                        isRadio = isRadio
                    )
                )
                // Reset temporary parsed fields
                currentName = ""
                currentLogo = ""
                currentGroup = "General"
            }
        }
        return list
    }
}
