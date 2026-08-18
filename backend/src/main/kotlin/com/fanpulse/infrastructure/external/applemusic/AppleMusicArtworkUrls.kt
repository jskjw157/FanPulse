package com.fanpulse.infrastructure.external.applemusic

import java.net.URI

object AppleMusicArtworkUrls {
    private val HOST = Regex("^is[1-9]-ssl\\.mzstatic\\.com$")
    private const val MAX_LENGTH = 512

    fun parseOptional(raw: String?): String? {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return null
        return canonicalize(value)
    }

    internal fun canonicalize(raw: String): String {
        if (raw.length > MAX_LENGTH || raw.any { it.isISOControl() || it.isWhitespace() }) {
            throw AppleMusicChartSourceException("Apple Music artwork url was invalid")
        }
        val uri = try {
            URI(raw)
        } catch (exc: Exception) {
            throw AppleMusicChartSourceException("Apple Music artwork url was invalid", exc)
        }
        val host = uri.host?.lowercase()
        val rawPath = uri.rawPath
        val path = uri.path
        if (
            uri.scheme?.lowercase() != "https" ||
            !uri.userInfo.isNullOrEmpty() ||
            host == null ||
            !HOST.matches(host) ||
            (uri.port != -1 && uri.port != 443) ||
            !uri.rawQuery.isNullOrEmpty() ||
            !uri.rawFragment.isNullOrEmpty() ||
            rawPath.isNullOrEmpty() ||
            path.isNullOrEmpty() ||
            rawPath != path ||
            rawPath.contains(Regex("(?i)%2e|%2f|%5c")) ||
            path.contains("/./") ||
            path.contains("/../") ||
            path.endsWith("/.") ||
            path.endsWith("/..") ||
            !path.startsWith("/image/thumb/") ||
            !(path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png"))
        ) {
            throw AppleMusicChartSourceException("Apple Music artwork url was invalid")
        }
        return "https://$host$path"
    }
}
