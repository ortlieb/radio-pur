package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiAssistantResponse
import com.example.data.ai.GeminiRadioAssistant
import com.example.data.api.RadioBrowserApi
import com.example.data.local.AppDatabase
import com.example.data.local.CuratedStations
import com.example.data.model.ActiveProfile
import com.example.data.model.CountryGenreCombo
import com.example.data.model.CountryItem
import com.example.data.model.GenreItem
import com.example.data.model.ProfileConfig
import com.example.data.model.RadioStation
import com.example.data.repository.RadioRepository
import com.example.player.PlaybackState
import com.example.player.PlayerUiState
import com.example.player.RadioPlayerManager
import com.example.player.RadioRecorderManager
import com.example.player.RadioRecording
import com.example.player.RecorderUiState
import com.example.player.SoundPreset
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainNavTab(val title: String) {
    FAVORITES("Favoriten"),
    DISCOVER("Sender"),
    ONLINE_SEARCH("Suche"),
    HISTORY("Aufnahmen & Verlauf")
}

data class SearchUiState(
    val query: String = "",
    val selectedCountry: CountryItem? = null,
    val selectedGenre: GenreItem? = null,
    val isSearching: Boolean = false,
    val searchResults: List<RadioStation> = emptyList(),
    val searchError: String? = null,
    val totalFoundCount: Int = 0
)

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val api = RadioBrowserApi.create()
    private val repository = RadioRepository(database.radioStationDao(), api)
    private val playerManager = RadioPlayerManager(application, viewModelScope)
    private val aiAssistant = GeminiRadioAssistant(repository)
    private val recorderManager = RadioRecorderManager(application, viewModelScope)

    val playerState: StateFlow<PlayerUiState> = playerManager.uiState
    val recorderState: StateFlow<RecorderUiState> = recorderManager.uiState

    private val _aiResponse = MutableStateFlow<AiAssistantResponse?>(null)
    val aiResponse: StateFlow<AiAssistantResponse?> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _showAiDialog = MutableStateFlow(false)
    val showAiDialog: StateFlow<Boolean> = _showAiDialog.asStateFlow()

    private val _profileConfig = MutableStateFlow(
        ProfileConfig(
            myName = "Für Mich",
            herName = "Für Meine Frau",
            sharedName = "Gemeinsam",
            activeProfile = ActiveProfile.MINE
        )
    )
    val profileConfig: StateFlow<ProfileConfig> = _profileConfig.asStateFlow()

    private val _currentTab = MutableStateFlow(MainNavTab.FAVORITES)
    val currentTab: StateFlow<MainNavTab> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Alle")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _showFullPlayer = MutableStateFlow(false)
    val showFullPlayer: StateFlow<Boolean> = _showFullPlayer.asStateFlow()

    private val _showSleepTimerDialog = MutableStateFlow(false)
    val showSleepTimerDialog: StateFlow<Boolean> = _showSleepTimerDialog.asStateFlow()

    private val _showAddStationDialog = MutableStateFlow(false)
    val showAddStationDialog: StateFlow<Boolean> = _showAddStationDialog.asStateFlow()

    private val _showEditProfileDialog = MutableStateFlow(false)
    val showEditProfileDialog: StateFlow<Boolean> = _showEditProfileDialog.asStateFlow()

    private val _showCountryDialog = MutableStateFlow(false)
    val showCountryDialog: StateFlow<Boolean> = _showCountryDialog.asStateFlow()

    private val _showGenreDialog = MutableStateFlow(false)
    val showGenreDialog: StateFlow<Boolean> = _showGenreDialog.asStateFlow()

    private val _isAiInlineVisible = MutableStateFlow(true)
    val isAiInlineVisible: StateFlow<Boolean> = _isAiInlineVisible.asStateFlow()

    private var searchJob: Job? = null

    val allFavorites = repository.allFavorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allStations = repository.allStations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CuratedStations.INITIAL_STATIONS
    )

    val presetStations = repository.presets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val historyStations = repository.history.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customStations = repository.customStations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered favorites by active profile
    val profileFavorites: StateFlow<List<RadioStation>> = combine(
        allFavorites,
        _profileConfig
    ) { favs, config ->
        when (config.activeProfile) {
            ActiveProfile.MINE -> favs.filter { it.favoriteProfile == "mine" || it.favoriteProfile == "both" }
            ActiveProfile.HERS -> favs.filter { it.favoriteProfile == "hers" || it.favoriteProfile == "both" }
            ActiveProfile.BOTH -> favs.filter { it.favoriteProfile != "none" }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Load initial top online stations in search tab
        loadTopOnlineStations()
        // Provide initial thoughtful AI suggestions so recommendations are immediately available
        _aiResponse.value = aiAssistant.getCuratedFallbackResponse("Empfehlungen für den Tag", _profileConfig.value, isKeyMissing = false)
    }

    fun selectTab(tab: MainNavTab) {
        _currentTab.value = tab
    }

    fun selectProfile(profile: ActiveProfile) {
        _profileConfig.value = _profileConfig.value.copy(activeProfile = profile)
    }

    fun updateProfileNames(myName: String, herName: String, sharedName: String) {
        _profileConfig.value = _profileConfig.value.copy(
            myName = myName.ifBlank { "Für Mich" },
            herName = herName.ifBlank { "Für Meine Frau" },
            sharedName = sharedName.ifBlank { "Gemeinsam" }
        )
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun playStation(station: RadioStation) {
        playerManager.playStation(station)
        viewModelScope.launch {
            repository.markAsPlayed(station)
        }
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun stopPlayback() {
        playerManager.stopPlayback()
    }

    fun toggleFavorite(station: RadioStation, targetProfile: String? = null) {
        viewModelScope.launch {
            val profileToSet = targetProfile ?: _profileConfig.value.activeProfile.id
            repository.toggleFavorite(station, profileToSet)
        }
    }

    fun setPreset(station: RadioStation, presetIndex: Int) {
        viewModelScope.launch {
            repository.setPreset(station, presetIndex)
        }
    }

    fun removePreset(stationId: String) {
        viewModelScope.launch {
            repository.removePreset(stationId)
        }
    }

    fun addCustomStation(name: String, streamUrl: String, tags: String, favoriteFor: String) {
        viewModelScope.launch {
            val station = repository.addCustomStation(name, streamUrl, tags, favoriteFor)
            playStation(station)
        }
    }

    fun deleteStation(station: RadioStation) {
        viewModelScope.launch {
            repository.deleteStation(station)
        }
    }

    fun setSearchQuery(query: String) {
        _searchState.value = _searchState.value.copy(query = query)
        executeSearch(debounceMs = 350L)
    }

    fun setCountryFilter(country: CountryItem?) {
        _searchState.value = _searchState.value.copy(selectedCountry = country)
        executeSearch(debounceMs = 0L)
    }

    fun setGenreFilter(genre: GenreItem?) {
        _searchState.value = _searchState.value.copy(selectedGenre = genre)
        executeSearch(debounceMs = 0L)
    }

    fun setCountryAndGenreFilter(country: CountryItem?, genre: GenreItem?) {
        _searchState.value = _searchState.value.copy(
            selectedCountry = country,
            selectedGenre = genre
        )
        executeSearch(debounceMs = 0L)
    }

    fun applyComboFilter(combo: CountryGenreCombo) {
        _searchState.value = _searchState.value.copy(
            query = "",
            selectedCountry = combo.country,
            selectedGenre = combo.genre
        )
        executeSearch(debounceMs = 0L)
    }

    fun toggleAiInline(visible: Boolean? = null) {
        _isAiInlineVisible.value = visible ?: !_isAiInlineVisible.value
    }

    fun clearSearchFilters() {
        _searchState.value = _searchState.value.copy(
            query = "",
            selectedCountry = null,
            selectedGenre = null
        )
        loadTopOnlineStations()
    }

    private fun executeSearch(debounceMs: Long = 0L) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (debounceMs > 0) delay(debounceMs)

            val currentState = _searchState.value
            val q = currentState.query.trim()
            val country = currentState.selectedCountry
            val genre = currentState.selectedGenre

            if (q.isBlank() && country == null && genre == null) {
                loadTopOnlineStations()
                return@launch
            }

            _searchState.value = _searchState.value.copy(isSearching = true, searchError = null)
            try {
                val results = repository.searchOnline(
                    query = q,
                    tag = genre?.tag,
                    countryCode = country?.code,
                    country = country?.englishName
                )
                _searchState.value = _searchState.value.copy(
                    isSearching = false,
                    searchResults = results,
                    totalFoundCount = results.size,
                    searchError = if (results.isEmpty()) "Keine Sender für diese Suche gefunden." else null
                )
            } catch (e: Exception) {
                _searchState.value = _searchState.value.copy(
                    isSearching = false,
                    searchError = "Sendersuche derzeit nicht erreichbar."
                )
            }
        }
    }

    fun loadTopOnlineStations() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchState.value = _searchState.value.copy(isSearching = true, searchError = null)
            try {
                val topStations = repository.getTopOnlineStations()
                _searchState.value = _searchState.value.copy(
                    isSearching = false,
                    searchResults = topStations,
                    totalFoundCount = topStations.size
                )
            } catch (e: Exception) {
                _searchState.value = _searchState.value.copy(
                    isSearching = false,
                    searchResults = CuratedStations.INITIAL_STATIONS,
                    totalFoundCount = CuratedStations.INITIAL_STATIONS.size
                )
            }
        }
    }

    // AI Concierge
    fun openAiDialog() {
        _showAiDialog.value = true
        if (_aiResponse.value == null) {
            askAi("Empfiehl uns bitte schöne Sender für heute!")
        }
    }

    fun setShowAiDialog(show: Boolean) {
        _showAiDialog.value = show
    }

    fun askAi(prompt: String) {
        _isAiLoading.value = true
        viewModelScope.launch {
            try {
                val resp = aiAssistant.getRecommendations(prompt, _profileConfig.value)
                _aiResponse.value = resp
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // Radio Recorder (Audials Signature Feature)
    fun startRecording(station: RadioStation) {
        recorderManager.startRecording(station)
    }

    fun stopRecording() {
        recorderManager.stopRecording()
    }

    fun deleteRecording(recording: RadioRecording) {
        recorderManager.deleteRecording(recording)
    }

    fun playRecording(recording: RadioRecording) {
        val virtualStation = RadioStation(
            id = "rec_${recording.file.name}",
            name = "Mitschnitt: ${recording.stationName}",
            streamUrl = recording.file.absolutePath,
            tags = "Lokale Aufnahme • ${recording.dateFormatted}",
            country = "Aufnahme",
            bitrate = 128,
            codec = "MP3",
            isCustom = true
        )
        playStation(virtualStation)
    }

    fun setVolume(volume: Float) {
        playerManager.setVolume(volume)
    }

    fun setSoundPreset(preset: SoundPreset) {
        playerManager.setSoundPreset(preset)
    }

    fun startSleepTimer(minutes: Int) {
        playerManager.startSleepTimer(minutes)
        _showSleepTimerDialog.value = false
    }

    fun cancelSleepTimer() {
        playerManager.cancelSleepTimer()
        _showSleepTimerDialog.value = false
    }

    fun setShowFullPlayer(show: Boolean) {
        _showFullPlayer.value = show
    }

    fun setShowSleepTimerDialog(show: Boolean) {
        _showSleepTimerDialog.value = show
    }

    fun setShowAddStationDialog(show: Boolean) {
        _showAddStationDialog.value = show
    }

    fun setShowEditProfileDialog(show: Boolean) {
        _showEditProfileDialog.value = show
    }

    fun setShowCountryDialog(show: Boolean) {
        _showCountryDialog.value = show
    }

    fun setShowGenreDialog(show: Boolean) {
        _showGenreDialog.value = show
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        recorderManager.stopRecording()
        playerManager.release()
    }
}
