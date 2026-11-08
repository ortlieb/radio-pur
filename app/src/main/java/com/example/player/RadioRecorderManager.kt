package com.example.player

import android.content.Context
import android.util.Log
import com.example.data.model.RadioStation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class RadioRecording(
    val file: File,
    val stationName: String,
    val dateFormatted: String,
    val durationSeconds: Int,
    val sizeBytes: Long
)

data class RecorderUiState(
    val isRecording: Boolean = false,
    val recordingStation: RadioStation? = null,
    val recordingDurationSeconds: Int = 0,
    val recordedBytes: Long = 0L,
    val recordings: List<RadioRecording> = emptyList(),
    val statusMessage: String? = null
)

class RadioRecorderManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // No read timeout for live streams
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private var recordingJob: Job? = null
    private var timerJob: Job? = null

    private val _uiState = MutableStateFlow(RecorderUiState())
    val uiState: StateFlow<RecorderUiState> = _uiState.asStateFlow()

    init {
        loadRecordings()
    }

    private fun getRecordingsDir(): File {
        val dir = File(context.getExternalFilesDir(null), "RadioRecordings")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun loadRecordings() {
        val dir = getRecordingsDir()
        val files = dir.listFiles { f -> f.extension.equals("mp3", ignoreCase = true) } ?: emptyArray()
        val list = files.sortedByDescending { it.lastModified() }.map { file ->
            val parts = file.nameWithoutExtension.split("__")
            val stationName = parts.firstOrNull()?.replace("_", " ") ?: "Radio Mitschnitt"
            val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(Date(file.lastModified()))
            val approxDurationSec = (file.length() / (128 * 1024 / 8)).toInt() // Approx based on 128kbps

            RadioRecording(
                file = file,
                stationName = stationName,
                dateFormatted = dateStr,
                durationSeconds = approxDurationSec,
                sizeBytes = file.length()
            )
        }
        _uiState.value = _uiState.value.copy(recordings = list)
    }

    fun startRecording(station: RadioStation) {
        if (_uiState.value.isRecording) return

        val cleanName = station.name.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY).format(Date())
        val targetFile = File(getRecordingsDir(), "${cleanName}__${timestamp}.mp3")

        _uiState.value = _uiState.value.copy(
            isRecording = true,
            recordingStation = station,
            recordingDurationSeconds = 0,
            recordedBytes = 0L,
            statusMessage = "Aufnahme von ${station.name} gestartet..."
        )

        timerJob?.cancel()
        timerJob = scope.launch(Dispatchers.Default) {
            while (isActive && _uiState.value.isRecording) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    recordingDurationSeconds = _uiState.value.recordingDurationSeconds + 1
                )
            }
        }

        recordingJob?.cancel()
        recordingJob = scope.launch(Dispatchers.IO) {
            var outputStream: FileOutputStream? = null
            try {
                outputStream = FileOutputStream(targetFile)
                val request = Request.Builder()
                    .url(station.streamUrl)
                    .header("User-Agent", "Mozilla/5.0 RadioPur/1.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("RadioRecorder", "Stream response not successful: ${response.code}")
                        return@use
                    }

                    val source = response.body?.byteStream() ?: return@use
                    val buffer = ByteArray(8192)
                    var bytesRead = 0
                    var totalBytes = 0L

                    while (isActive && _uiState.value.isRecording && source.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytes += bytesRead
                        if (totalBytes % 65536 == 0L) {
                            _uiState.value = _uiState.value.copy(recordedBytes = totalBytes)
                        }
                    }
                    outputStream.flush()
                }
            } catch (e: Exception) {
                Log.w("RadioRecorder", "Recording stream stopped", e)
            } finally {
                try {
                    outputStream?.close()
                } catch (e: Exception) {
                    // ignore
                }
                loadRecordings()
            }
        }
    }

    fun stopRecording() {
        if (!_uiState.value.isRecording) return

        timerJob?.cancel()
        recordingJob?.cancel()

        val stationName = _uiState.value.recordingStation?.name ?: "Sender"
        val duration = _uiState.value.recordingDurationSeconds

        _uiState.value = _uiState.value.copy(
            isRecording = false,
            recordingStation = null,
            statusMessage = "Mitschnitt von $stationName gespeichert ($duration Sek.)"
        )

        loadRecordings()
    }

    fun deleteRecording(recording: RadioRecording) {
        try {
            if (recording.file.exists()) {
                recording.file.delete()
            }
            loadRecordings()
        } catch (e: Exception) {
            Log.e("RadioRecorder", "Could not delete recording", e)
        }
    }
}
