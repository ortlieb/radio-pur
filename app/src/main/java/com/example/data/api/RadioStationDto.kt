package com.example.data.api

import com.example.data.model.RadioStation
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RadioStationDto(
    @field:Json(name = "stationuuid") val stationuuid: String? = null,
    @field:Json(name = "name") val name: String? = null,
    @field:Json(name = "url_resolved") val urlResolved: String? = null,
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "homepage") val homepage: String? = null,
    @field:Json(name = "favicon") val favicon: String? = null,
    @field:Json(name = "tags") val tags: String? = null,
    @field:Json(name = "country") val country: String? = null,
    @field:Json(name = "language") val language: String? = null,
    @field:Json(name = "bitrate") val bitrate: Int? = null,
    @field:Json(name = "codec") val codec: String? = null
) {
    fun toRadioStation(): RadioStation? {
        val stream = urlResolved?.takeIf { it.isNotBlank() }
            ?: url?.takeIf { it.isNotBlank() }
            ?: return null
        val stationId = stationuuid?.takeIf { it.isNotBlank() } ?: return null
        val stationName = name?.trim()?.takeIf { it.isNotBlank() } ?: "Radio"
        val normalizedUrl = RadioStation.normalizeStreamUrl(stream.trim())

        return RadioStation(
            id = stationId,
            name = stationName,
            streamUrl = normalizedUrl,
            homepage = homepage?.trim() ?: "",
            favicon = favicon?.trim() ?: "",
            tags = tags?.trim() ?: "",
            country = country?.trim() ?: "International",
            language = language?.trim() ?: "german",
            bitrate = bitrate ?: 128,
            codec = codec?.trim()?.uppercase() ?: "MP3",
            isCustom = false,
            favoriteProfile = "none",
            isPreset = false,
            presetIndex = -1,
            isDefaultSeed = false
        )
    }
}
