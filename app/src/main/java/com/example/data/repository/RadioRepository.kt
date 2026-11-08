package com.example.data.repository

import com.example.data.api.RadioBrowserApi
import com.example.data.local.RadioStationDao
import com.example.data.model.RadioStation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class RadioRepository(
    private val dao: RadioStationDao,
    private val api: RadioBrowserApi
) {
    val allStations: Flow<List<RadioStation>> = dao.getAllStations()
    val allFavorites: Flow<List<RadioStation>> = dao.getAllFavorites()
    val presets: Flow<List<RadioStation>> = dao.getPresetStations()
    val history: Flow<List<RadioStation>> = dao.getListeningHistory()
    val customStations: Flow<List<RadioStation>> = dao.getCustomStations()

    fun getFavoritesForProfile(profileId: String): Flow<List<RadioStation>> {
        return dao.getFavoritesForProfile(profileId)
    }

    suspend fun toggleFavorite(station: RadioStation, targetProfile: String) = withContext(Dispatchers.IO) {
        val currentProfile = station.favoriteProfile
        val newProfile = when {
            // If already favorited for this profile, remove or demote
            currentProfile == targetProfile -> "none"
            currentProfile == "none" -> targetProfile
            // If already favorited for the other spouse, make it 'both'
            (currentProfile == "mine" && targetProfile == "hers") || 
            (currentProfile == "hers" && targetProfile == "mine") -> "both"
            currentProfile == "both" && targetProfile == "mine" -> "hers"
            currentProfile == "both" && targetProfile == "hers" -> "mine"
            else -> targetProfile
        }

        // Ensure station exists in local database before updating favorite status
        val existing = dao.getStationById(station.id)
        if (existing == null) {
            dao.insertStation(station.copy(favoriteProfile = newProfile))
        } else {
            dao.updateFavoriteStatus(station.id, newProfile)
        }
    }

    suspend fun setPreset(station: RadioStation, presetIndex: Int) = withContext(Dispatchers.IO) {
        // Clear any station currently holding this preset slot
        dao.clearPresetIndex(presetIndex)
        // Ensure station is in DB
        val existing = dao.getStationById(station.id)
        if (existing == null) {
            dao.insertStation(station.copy(presetIndex = presetIndex, isPreset = true))
        } else {
            dao.updatePreset(station.id, presetIndex, true)
        }
    }

    suspend fun removePreset(stationId: String) = withContext(Dispatchers.IO) {
        dao.updatePreset(stationId, -1, false)
    }

    suspend fun markAsPlayed(station: RadioStation) = withContext(Dispatchers.IO) {
        val existing = dao.getStationById(station.id)
        if (existing == null) {
            dao.insertStation(station.copy(lastPlayed = System.currentTimeMillis(), playCount = 1))
        } else {
            dao.markAsPlayed(station.id)
        }
    }

    suspend fun addCustomStation(
        name: String,
        streamUrl: String,
        tags: String,
        favoriteFor: String
    ): RadioStation = withContext(Dispatchers.IO) {
        val station = RadioStation(
            id = "custom_" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            streamUrl = streamUrl.trim(),
            tags = if (tags.isBlank()) "Eigenes Webradio" else tags.trim(),
            country = "Eigen",
            bitrate = 128,
            codec = "MP3",
            isCustom = true,
            favoriteProfile = favoriteFor,
            lastPlayed = System.currentTimeMillis()
        )
        dao.insertStation(station)
        station
    }

    suspend fun deleteStation(station: RadioStation) = withContext(Dispatchers.IO) {
        dao.deleteStation(station)
    }

    suspend fun searchOnline(
        query: String = "",
        tag: String? = null,
        countryCode: String? = null,
        country: String? = null
    ): List<RadioStation> = withContext(Dispatchers.IO) {
        try {
            val q = query.trim().ifBlank { null }
            val t = tag?.trim()?.ifBlank { null }
            val cc = countryCode?.trim()?.ifBlank { null }
            val c = country?.trim()?.ifBlank { null }
            val dtos = api.searchStations(
                name = q,
                tag = t,
                countrycode = cc,
                country = c,
                limit = 50
            )
            dtos.mapNotNull { it.toRadioStation() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTopOnlineStations(): List<RadioStation> = withContext(Dispatchers.IO) {
        try {
            val dtos = api.getTopStations(limit = 40)
            val mapped = dtos.mapNotNull { it.toRadioStation() }
            if (mapped.isNotEmpty()) {
                mapped
            } else {
                val deDtos = api.getGermanyStations(limit = 30)
                deDtos.mapNotNull { it.toRadioStation() }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
