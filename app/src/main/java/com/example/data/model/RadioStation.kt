package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "radio_stations")
data class RadioStation(
    @PrimaryKey
    val id: String,
    val name: String,
    val streamUrl: String,
    val homepage: String = "",
    val favicon: String = "",
    val tags: String = "",
    val country: String = "Deutschland",
    val language: String = "german",
    val bitrate: Int = 128,
    val codec: String = "MP3",
    val isCustom: Boolean = false,
    
    // Favorite profile assignment: "none", "mine" (Er), "hers" (Sie), "both" (Gemeinsam)
    val favoriteProfile: String = "none",

    val isPreset: Boolean = false,
    val presetIndex: Int = -1, // 1..6

    val lastPlayed: Long = 0L,
    val playCount: Int = 0,
    val isDefaultSeed: Boolean = false
) {
    fun isFavoriteFor(profileId: String): Boolean {
        return when (profileId) {
            "mine" -> favoriteProfile == "mine" || favoriteProfile == "both"
            "hers" -> favoriteProfile == "hers" || favoriteProfile == "both"
            "both" -> favoriteProfile == "both" || favoriteProfile == "mine" || favoriteProfile == "hers"
            else -> favoriteProfile != "none"
        }
    }

    companion object {
        /**
         * Normalizes and upgrades known radio stream URLs (like StreamTheWorld / Triton Digital)
         * to their official, continuous Shoutcast MP3 stream (_SC) - exactly what Audials and
         * major radio apps use. Progressive .aac feeds on adaptive (_ADP) mounts falsely report
         * artificial 2GB content lengths and disconnect under progressive HTTP/1.0, whereas
         * the Shoutcast MP3 (_SC) mounts stream continuously without buffering loops or socket disconnects.
         */
        fun normalizeStreamUrl(url: String): String {
            val trimmed = url.trim()
            if (!trimmed.contains("streamtheworld.com", ignoreCase = true)) {
                return trimmed
            }

            val query = trimmed.substringAfter("?", "").let { if (it.isNotBlank()) "?$it" else "" }

            // Extract the callsign / mount
            val mount = when {
                trimmed.contains("livestream-redirect/", ignoreCase = true) -> {
                    trimmed.substringAfter("livestream-redirect/").substringBefore(".").substringBefore("?")
                }
                else -> {
                    val directRegex = Regex("https?://[^/]+/([^/?]+)", RegexOption.IGNORE_CASE)
                    directRegex.find(trimmed)?.groupValues?.get(1)?.substringBefore(".") ?: ""
                }
            }

            if (mount.isNotBlank()) {
                val scMount = when {
                    mount.endsWith("_SC", ignoreCase = true) -> mount
                    mount.endsWith("_ADP", ignoreCase = true) -> mount.removeSuffix("_ADP") + "_SC"
                    mount.endsWith("AAC", ignoreCase = true) -> mount.removeSuffix("AAC") + "_SC"
                    else -> "${mount}_SC"
                }
                return "https://playerservices.streamtheworld.com/api/livestream-redirect/$scMount$query"
            }

            return trimmed
        }
    }
}

enum class ActiveProfile(val id: String, val defaultTitle: String, val icon: String) {
    MINE("mine", "Für Mich", "👨"),
    HERS("hers", "Für Meine Frau", "👩"),
    BOTH("both", "Gemeinsam", "👫")
}

data class ProfileConfig(
    val myName: String = "Für Mich",
    val herName: String = "Für Meine Frau",
    val sharedName: String = "Gemeinsam",
    val activeProfile: ActiveProfile = ActiveProfile.MINE
)
