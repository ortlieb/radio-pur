package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RadioStation
import kotlinx.coroutines.flow.Flow

@Dao
interface RadioStationDao {

    @Query("SELECT * FROM radio_stations ORDER BY name ASC")
    fun getAllStations(): Flow<List<RadioStation>>

    @Query("SELECT * FROM radio_stations WHERE favoriteProfile != 'none' ORDER BY lastPlayed DESC, name ASC")
    fun getAllFavorites(): Flow<List<RadioStation>>

    @Query("SELECT * FROM radio_stations WHERE favoriteProfile = :profile OR favoriteProfile = 'both' ORDER BY name ASC")
    fun getFavoritesForProfile(profile: String): Flow<List<RadioStation>>

    @Query("SELECT * FROM radio_stations WHERE presetIndex >= 1 ORDER BY presetIndex ASC")
    fun getPresetStations(): Flow<List<RadioStation>>

    @Query("SELECT * FROM radio_stations WHERE lastPlayed > 0 ORDER BY lastPlayed DESC LIMIT 30")
    fun getListeningHistory(): Flow<List<RadioStation>>

    @Query("SELECT * FROM radio_stations WHERE isCustom = 1 ORDER BY name ASC")
    fun getCustomStations(): Flow<List<RadioStation>>

    @Query("SELECT * FROM radio_stations WHERE id = :id LIMIT 1")
    suspend fun getStationById(id: String): RadioStation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<RadioStation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: RadioStation)

    @Update
    suspend fun updateStation(station: RadioStation)

    @Query("UPDATE radio_stations SET favoriteProfile = :favoriteProfile WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, favoriteProfile: String)

    @Query("UPDATE radio_stations SET presetIndex = :presetIndex, isPreset = :isPreset WHERE id = :id")
    suspend fun updatePreset(id: String, presetIndex: Int, isPreset: Boolean)

    @Query("UPDATE radio_stations SET presetIndex = -1, isPreset = 0 WHERE presetIndex = :presetIndex")
    suspend fun clearPresetIndex(presetIndex: Int)

    @Query("UPDATE radio_stations SET lastPlayed = :timestamp, playCount = playCount + 1 WHERE id = :id")
    suspend fun markAsPlayed(id: String, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteStation(station: RadioStation)

    @Query("DELETE FROM radio_stations WHERE id = :id")
    suspend fun deleteStationById(id: String)

    @Query("SELECT COUNT(*) FROM radio_stations")
    suspend fun getCount(): Int

    @Query("SELECT * FROM radio_stations")
    suspend fun getAllStationsList(): List<RadioStation>
}
