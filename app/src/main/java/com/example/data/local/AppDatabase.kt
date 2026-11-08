package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.RadioStation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [RadioStation::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun radioStationDao(): RadioStationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pure_radio_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialStations(database.radioStationDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        syncCuratedStations(database.radioStationDao())
                    }
                }
            }

            suspend fun populateInitialStations(dao: RadioStationDao) {
                dao.insertStations(CuratedStations.INITIAL_STATIONS)
            }

            suspend fun syncCuratedStations(dao: RadioStationDao) {
                val count = dao.getCount()
                if (count == 0) {
                    populateInitialStations(dao)
                    return
                }

                // Update default seed stations with verified stream URLs
                for (fresh in CuratedStations.INITIAL_STATIONS) {
                    val existing = dao.getStationById(fresh.id)
                    if (existing != null) {
                        // Keep user's favorite profile and presetIndex if set, but update streamUrl
                        val updated = existing.copy(
                            streamUrl = fresh.streamUrl,
                            bitrate = fresh.bitrate,
                            codec = fresh.codec,
                            favicon = if (fresh.favicon.isNotBlank()) fresh.favicon else existing.favicon,
                            tags = fresh.tags
                        )
                        dao.updateStation(updated)
                    } else {
                        dao.insertStation(fresh)
                    }
                }

                // Upgrade any existing saved stations (e.g. from favorites, presets, history) that used old progressive URLs
                val allStations = dao.getAllStationsList()
                for (station in allStations) {
                    val normalized = com.example.data.model.RadioStation.normalizeStreamUrl(station.streamUrl)
                    if (normalized != station.streamUrl) {
                        dao.updateStation(station.copy(streamUrl = normalized))
                    }
                }
            }
        }
    }
}
