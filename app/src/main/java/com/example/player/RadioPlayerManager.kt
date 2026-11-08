package com.example.player

import android.content.Context
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
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
import java.util.concurrent.TimeUnit
import kotlin.random.Random

enum class PlaybackState {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}

enum class SoundPreset(val title: String, val desc: String) {
    BALANCED("Hi-Fi Neutral", "Klarer, unverfälschter Radioklang"),
    BASS_BOOST("Bass Plus", "Warme Tiefen für Musik & Beats"),
    VOCAL_CLEAR("Stimme & Talk", "Kristallklare Sprache für Nachrichten & Podcasts"),
    NIGHT_SOFT("Nachtmodus", "Gedämpfte Spitzen für sanftes Einschlafen")
}

data class PlayerUiState(
    val currentStation: RadioStation? = null,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val errorMessage: String? = null,
    val volume: Float = 1.0f,
    val soundPreset: SoundPreset = SoundPreset.BALANCED,
    val sleepTimerMinutesRemaining: Int? = null,
    val sleepTimerActive: Boolean = false,
    val waveAmplitudes: List<Float> = List(16) { 0.15f }
)

class RadioPlayerManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var exoPlayer: ExoPlayer? = null
    private var sleepTimer: CountDownTimer? = null
    private var visualizerJob: Job? = null
    private var streamResolveJob: Job? = null
    private var reconnectJob: Job? = null
    private var stablePlaybackJob: Job? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 6
    private var isUserStopped = false

    // Specialized OkHttpClient for live continuous radio streaming
    private val streamingHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // 0 = continuous live audio streaming without socket read timeouts
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .addNetworkInterceptor { chain ->
            val original = chain.request()
            // Strip 'Range' header for live radio streams to prevent servers (like StreamTheWorld)
            // from falsely treating live streams as static 2GB file downloads with 206 Partial Content
            val urlStr = original.url.toString()
            val isLiveStream = urlStr.contains("stream", ignoreCase = true) ||
                               urlStr.contains("live", ignoreCase = true) ||
                               urlStr.contains("streamtheworld", ignoreCase = true) ||
                               urlStr.contains("broadcast", ignoreCase = true)
            val request = if (isLiveStream && original.header("Range") != null) {
                original.newBuilder().removeHeader("Range").build()
            } else {
                original
            }
            chain.proceed(request)
        }
        .build()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        initPlayer()
        startVisualizerLoop()
    }

    private fun initPlayer() {
        try {
            val userAgent = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 RadioPur/1.0"
            val okHttpDataSourceFactory = OkHttpDataSource.Factory(streamingHttpClient)
                .setUserAgent(userAgent)

            // Wrap OkHttpDataSource with DefaultDataSource so local file recordings also play smoothly
            val defaultDataSourceFactory = DefaultDataSource.Factory(context, okHttpDataSourceFactory)

            // Fast, resilient LoadControl matching professional radio players (like Audials & TuneIn):
            // 1. bufferForPlaybackMs = 1200ms ensures near-instant playback start
            // 2. bufferForPlaybackAfterRebufferMs = 2500ms provides quick, non-disruptive rebuffering recovery
            // 3. prioritizeTimeOverSizeThresholds = false ensures playback starts immediately when data is received
            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    5000,  // minBufferMs
                    30000, // maxBufferMs
                    1200,  // bufferForPlaybackMs
                    2500   // bufferForPlaybackAfterRebufferMs
                )
                .setPrioritizeTimeOverSizeThresholds(false)
                .setBackBuffer(0, false)
                .build()

            val errorHandlingPolicy = object : DefaultLoadErrorHandlingPolicy() {
                override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long {
                    return 1000L
                }
                override fun getMinimumLoadableRetryCount(dataType: Int): Int {
                    return 8
                }
            }

            val mediaSourceFactory = DefaultMediaSourceFactory(defaultDataSourceFactory)
                .setLoadErrorHandlingPolicy(errorHandlingPolicy)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()

            exoPlayer = ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .setLoadControl(loadControl)
                .setAudioAttributes(audioAttributes, true)
                .setWakeMode(C.WAKE_MODE_NETWORK)
                .setHandleAudioBecomingNoisy(true)
                .build().apply {
                    addListener(createPlayerListener())
                }
        } catch (e: Exception) {
            Log.e("RadioPlayerManager", "Failed to initialize ExoPlayer", e)
        }
    }

    private fun createPlayerListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_BUFFERING -> {
                    stablePlaybackJob?.cancel()
                    _uiState.value = _uiState.value.copy(
                        playbackState = PlaybackState.BUFFERING,
                        errorMessage = null
                    )
                }
                Player.STATE_READY -> {
                    val isPlaying = exoPlayer?.playWhenReady == true
                    _uiState.value = _uiState.value.copy(
                        playbackState = if (isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED,
                        errorMessage = null
                    )
                    if (isPlaying) {
                        scheduleStablePlaybackReset()
                    }
                }
                Player.STATE_ENDED -> {
                    stablePlaybackJob?.cancel()
                    // For live radio streams, STATE_ENDED is unexpected (server closed connection or stream dropped).
                    // Trigger auto-reconnect instead of stopping playback.
                    handleUnexpectedStreamEnd(error = null)
                }
                Player.STATE_IDLE -> {
                    stablePlaybackJob?.cancel()
                    if (_uiState.value.playbackState != PlaybackState.ERROR && reconnectJob?.isActive != true) {
                        _uiState.value = _uiState.value.copy(playbackState = PlaybackState.IDLE)
                    }
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                _uiState.value = _uiState.value.copy(
                    playbackState = PlaybackState.PLAYING,
                    errorMessage = null
                )
                scheduleStablePlaybackReset()
            } else {
                stablePlaybackJob?.cancel()
                if (exoPlayer?.playbackState == Player.STATE_READY && !isUserStopped) {
                    _uiState.value = _uiState.value.copy(playbackState = PlaybackState.PAUSED)
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            stablePlaybackJob?.cancel()
            Log.e("RadioPlayerManager", "Playback exception: ${error.errorCodeName}", error)
            handleUnexpectedStreamEnd(error = error)
        }
    }

    private fun scheduleStablePlaybackReset() {
        stablePlaybackJob?.cancel()
        stablePlaybackJob = scope.launch {
            delay(8000) // After 8 seconds of continuous playback, reset reconnect counter
            reconnectAttempts = 0
        }
    }

    private fun handleUnexpectedStreamEnd(error: PlaybackException?) {
        stablePlaybackJob?.cancel()
        val currentStation = _uiState.value.currentStation
        if (isUserStopped || currentStation == null) {
            _uiState.value = _uiState.value.copy(playbackState = PlaybackState.IDLE)
            return
        }

        // For local recordings, reaching the end of the file is normal
        if (currentStation.streamUrl.startsWith("/") && error == null) {
            _uiState.value = _uiState.value.copy(playbackState = PlaybackState.IDLE)
            return
        }

        if (reconnectAttempts < maxReconnectAttempts) {
            reconnectAttempts++
            Log.w("RadioPlayerManager", "Stream disconnected, auto-reconnecting (attempt $reconnectAttempts/$maxReconnectAttempts)...")
            _uiState.value = _uiState.value.copy(
                playbackState = PlaybackState.BUFFERING,
                errorMessage = "Verbindung wird wiederhergestellt ($reconnectAttempts/$maxReconnectAttempts)..."
            )
            reconnectJob?.cancel()
            reconnectJob = scope.launch {
                delay(1000L * reconnectAttempts)
                if (!isUserStopped && _uiState.value.currentStation?.id == currentStation.id) {
                    val resolvedUrl = resolvePlaylistUrlIfNeeded(currentStation.streamUrl)
                    startStreamPlayback(currentStation, resolvedUrl)
                }
            }
        } else {
            reconnectAttempts = 0
            val friendlyMsg = when (error?.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                    "Verbindungsaufbau fehlgeschlagen. Bitte Verbindung prüfen."
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                    "Sender-Server antwortet derzeit nicht."
                PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
                PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED ->
                    "Stream-Format nicht kompatibel."
                else -> "Senderverbindung unterbrochen. Tippe auf Play zum Wiederholen."
            }
            _uiState.value = _uiState.value.copy(
                playbackState = PlaybackState.ERROR,
                errorMessage = friendlyMsg
            )
        }
    }

    fun playStation(station: RadioStation) {
        val current = _uiState.value
        if (current.currentStation?.id == station.id && current.playbackState == PlaybackState.PLAYING) {
            return
        }

        isUserStopped = false
        reconnectAttempts = 0
        stablePlaybackJob?.cancel()
        reconnectJob?.cancel()
        streamResolveJob?.cancel()

        _uiState.value = _uiState.value.copy(
            currentStation = station,
            playbackState = PlaybackState.BUFFERING,
            errorMessage = null
        )

        streamResolveJob = scope.launch {
            val resolvedUrl = resolvePlaylistUrlIfNeeded(station.streamUrl)
            startStreamPlayback(station, resolvedUrl)
        }
    }

    private fun startStreamPlayback(station: RadioStation, url: String) {
        val player = exoPlayer ?: return
        try {
            player.stop()
            player.clearMediaItems()

            val mediaUri = if (url.startsWith("/")) {
                Uri.fromFile(java.io.File(url))
            } else {
                Uri.parse(url)
            }

            val isHls = url.lowercase().contains(".m3u8")
            val mediaItemBuilder = MediaItem.Builder()
                .setUri(mediaUri)
                .setMediaId(station.id)

            if (isHls) {
                mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
            }

            val mediaItem = mediaItemBuilder.build()
            player.setMediaItem(mediaItem)
            applyVolumeAndPreset()
            player.prepare()
            player.playWhenReady = true
        } catch (e: Exception) {
            Log.e("RadioPlayerManager", "Failed to start stream", e)
            _uiState.value = _uiState.value.copy(
                playbackState = PlaybackState.ERROR,
                errorMessage = "Stream konnte nicht gestartet werden."
            )
        }
    }

    private suspend fun resolvePlaylistUrlIfNeeded(rawUrl: String): String = withContext(Dispatchers.IO) {
        val normalized = RadioStation.normalizeStreamUrl(rawUrl)
        val trimmed = normalized.trim()
        val lower = trimmed.lowercase()

        // If it's already an HLS playlist or direct file/stream, we don't need to parse m3u/pls
        if (lower.contains(".m3u8")) {
            return@withContext trimmed
        }

        // Only parse if it looks like an M3U or PLS playlist (not HLS m3u8 which ExoPlayer handles)
        val isM3u = (lower.endsWith(".m3u") || lower.contains(".m3u?"))
        val isPls = lower.endsWith(".pls") || lower.contains(".pls?")

        if (!isM3u && !isPls) {
            return@withContext trimmed
        }

        try {
            val request = Request.Builder()
                .url(trimmed)
                .header("User-Agent", "Mozilla/5.0 RadioPur/1.0")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext trimmed
                val body = response.body?.string() ?: return@withContext trimmed

                if (isM3u) {
                    val streamLine = body.lines()
                        .map { it.trim() }
                        .firstOrNull { it.startsWith("http://") || it.startsWith("https://") }
                    if (!streamLine.isNullOrBlank()) {
                        return@withContext RadioStation.normalizeStreamUrl(streamLine)
                    }
                } else if (isPls) {
                    val streamLine = body.lines()
                        .map { it.trim() }
                        .firstOrNull { line ->
                            line.startsWith("File", ignoreCase = true) && line.contains("=")
                        }
                        ?.substringAfter("=")
                        ?.trim()
                    if (!streamLine.isNullOrBlank() && (streamLine.startsWith("http://") || streamLine.startsWith("https://"))) {
                        return@withContext RadioStation.normalizeStreamUrl(streamLine)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("RadioPlayerManager", "Could not parse playlist: $trimmed", e)
        }
        trimmed
    }

    fun togglePlayPause() {
        val current = _uiState.value
        val player = exoPlayer ?: return

        if (current.currentStation == null) return

        if (player.isPlaying) {
            isUserStopped = true
            stablePlaybackJob?.cancel()
            reconnectJob?.cancel()
            reconnectAttempts = 0
            player.pause()
            _uiState.value = current.copy(playbackState = PlaybackState.PAUSED)
        } else {
            isUserStopped = false
            reconnectAttempts = 0
            stablePlaybackJob?.cancel()
            reconnectJob?.cancel()
            if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED || current.playbackState == PlaybackState.ERROR) {
                playStation(current.currentStation)
            } else {
                player.play()
                _uiState.value = current.copy(playbackState = PlaybackState.PLAYING)
            }
        }
    }

    fun stopPlayback() {
        isUserStopped = true
        reconnectAttempts = 0
        stablePlaybackJob?.cancel()
        reconnectJob?.cancel()
        streamResolveJob?.cancel()
        try {
            exoPlayer?.stop()
            exoPlayer?.clearMediaItems()
        } catch (e: Exception) {
            Log.e("RadioPlayerManager", "Error stopping playback", e)
        } finally {
            _uiState.value = _uiState.value.copy(playbackState = PlaybackState.IDLE)
        }
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _uiState.value = _uiState.value.copy(volume = clamped)
        applyVolumeAndPreset()
    }

    fun setSoundPreset(preset: SoundPreset) {
        _uiState.value = _uiState.value.copy(soundPreset = preset)
        applyVolumeAndPreset()
    }

    private fun applyVolumeAndPreset() {
        val baseVol = _uiState.value.volume
        val factor = when (_uiState.value.soundPreset) {
            SoundPreset.NIGHT_SOFT -> 0.75f
            SoundPreset.BASS_BOOST -> 1.05f.coerceAtMost(1f)
            else -> 1.0f
        }
        val effectiveVol = (baseVol * factor).coerceIn(0f, 1f)
        try {
            exoPlayer?.volume = effectiveVol
        } catch (e: Exception) {
            Log.w("RadioPlayerManager", "Error applying volume", e)
        }
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimer?.cancel()
        if (minutes <= 0) {
            _uiState.value = _uiState.value.copy(
                sleepTimerActive = false,
                sleepTimerMinutesRemaining = null
            )
            return
        }

        val totalMillis = minutes * 60 * 1000L
        _uiState.value = _uiState.value.copy(
            sleepTimerActive = true,
            sleepTimerMinutesRemaining = minutes
        )

        sleepTimer = object : CountDownTimer(totalMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val minsLeft = (millisUntilFinished / 60000L).toInt() + 1
                _uiState.value = _uiState.value.copy(sleepTimerMinutesRemaining = minsLeft)

                // Smooth fade-out in the last 20 seconds
                if (millisUntilFinished < 20000L) {
                    val fadeFactor = (millisUntilFinished / 20000f).coerceIn(0.05f, 1f)
                    val baseVol = _uiState.value.volume
                    val faded = baseVol * fadeFactor
                    try {
                        exoPlayer?.volume = faded
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }

            override fun onFinish() {
                stopPlayback()
                _uiState.value = _uiState.value.copy(
                    sleepTimerActive = false,
                    sleepTimerMinutesRemaining = null
                )
            }
        }.start()
    }

    fun cancelSleepTimer() {
        sleepTimer?.cancel()
        sleepTimer = null
        applyVolumeAndPreset()
        _uiState.value = _uiState.value.copy(
            sleepTimerActive = false,
            sleepTimerMinutesRemaining = null
        )
    }

    private fun startVisualizerLoop() {
        visualizerJob?.cancel()
        visualizerJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val state = _uiState.value.playbackState
                if (state == PlaybackState.PLAYING) {
                    val amplitudes = List(16) { index ->
                        val base = 0.35f + Random.nextFloat() * 0.60f
                        val shaped = if (index in 4..11) base else base * 0.7f
                        shaped.coerceIn(0.12f, 1f)
                    }
                    _uiState.value = _uiState.value.copy(waveAmplitudes = amplitudes)
                } else if (state == PlaybackState.BUFFERING) {
                    val time = System.currentTimeMillis() / 150.0
                    val amplitudes = List(16) { index ->
                        val pulse = (Math.sin(time + index * 0.4) + 1.0) / 2.0
                        pulse.toFloat().coerceIn(0.2f, 0.85f)
                    }
                    _uiState.value = _uiState.value.copy(waveAmplitudes = amplitudes)
                } else {
                    _uiState.value = _uiState.value.copy(waveAmplitudes = List(16) { 0.10f })
                }
                delay(90)
            }
        }
    }

    fun release() {
        isUserStopped = true
        stablePlaybackJob?.cancel()
        reconnectJob?.cancel()
        sleepTimer?.cancel()
        visualizerJob?.cancel()
        streamResolveJob?.cancel()
        try {
            exoPlayer?.release()
        } catch (e: Exception) {
            Log.e("RadioPlayerManager", "Error releasing player", e)
        }
        exoPlayer = null
    }
}
