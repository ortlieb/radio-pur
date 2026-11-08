package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CuratedStations
import com.example.data.model.ActiveProfile
import com.example.data.model.RadioStation
import com.example.player.PlaybackState
import com.example.ui.MainNavTab
import com.example.ui.RadioViewModel
import com.example.ui.components.AddCustomStationDialog
import com.example.player.RadioRecording
import com.example.player.RecorderUiState
import com.example.ui.components.AiConciergeDialog
import com.example.ui.components.CountryPickerDialog
import com.example.ui.components.FullPlayerBottomSheet
import com.example.ui.components.GenrePickerDialog
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.PresetDialRow
import com.example.ui.components.ProfileEditDialog
import com.example.ui.components.RadioHeader
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.StationCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDark
import com.example.ui.theme.AmberGoldLight
import com.example.ui.theme.ProfileBothAccent
import com.example.ui.theme.ProfileHerAccent
import com.example.ui.theme.ProfileHisAccent
import com.example.ui.theme.RadioCardBorder
import com.example.ui.theme.RadioCardDark
import com.example.ui.theme.RadioDarkBg
import com.example.ui.theme.RadioSurfaceDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark

@Composable
fun RadioMainScreen(
    viewModel: RadioViewModel,
    modifier: Modifier = Modifier
) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val profileConfig by viewModel.profileConfig.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()

    val profileFavorites by viewModel.profileFavorites.collectAsStateWithLifecycle()
    val allStations by viewModel.allStations.collectAsStateWithLifecycle()
    val presetStations by viewModel.presetStations.collectAsStateWithLifecycle()
    val historyStations by viewModel.historyStations.collectAsStateWithLifecycle()
    val customStations by viewModel.customStations.collectAsStateWithLifecycle()

    val showFullPlayer by viewModel.showFullPlayer.collectAsStateWithLifecycle()
    val showSleepTimerDialog by viewModel.showSleepTimerDialog.collectAsStateWithLifecycle()
    val showAddStationDialog by viewModel.showAddStationDialog.collectAsStateWithLifecycle()
    val showEditProfileDialog by viewModel.showEditProfileDialog.collectAsStateWithLifecycle()
    val showCountryDialog by viewModel.showCountryDialog.collectAsStateWithLifecycle()
    val showGenreDialog by viewModel.showGenreDialog.collectAsStateWithLifecycle()
    val showAiDialog by viewModel.showAiDialog.collectAsStateWithLifecycle()
    val aiResponse by viewModel.aiResponse.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val isAiInlineVisible by viewModel.isAiInlineVisible.collectAsStateWithLifecycle()
    val recorderState by viewModel.recorderState.collectAsStateWithLifecycle()

    val isPlaying = playerState.playbackState == PlaybackState.PLAYING
    val currentStationId = playerState.currentStation?.id

    Scaffold(
        modifier = modifier.fillMaxSize().background(RadioDarkBg),
        containerColor = RadioDarkBg,
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                RadioHeader(
                    profileConfig = profileConfig,
                    onSelectProfile = { viewModel.selectProfile(it) },
                    onEditProfiles = { viewModel.setShowEditProfileDialog(true) },
                    onAddCustomStation = { viewModel.setShowAddStationDialog(true) },
                    onOpenAiConcierge = { viewModel.openAiDialog() }
                )
                PresetDialRow(
                    presetStations = presetStations,
                    currentStationId = currentStationId,
                    isPlaying = isPlaying,
                    onPresetClick = { slot, station ->
                        if (station != null) {
                            viewModel.playStation(station)
                        } else if (playerState.currentStation != null) {
                            viewModel.setPreset(playerState.currentStation!!, slot)
                        }
                    }
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RadioSurfaceDark)
            ) {
                // Mini Player if station selected
                AnimatedVisibility(
                    visible = playerState.currentStation != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    MiniPlayerBar(
                        playerState = playerState,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onExpandFullPlayer = { viewModel.setShowFullPlayer(true) }
                    )
                }

                // Standard M3 Navigation Bar
                NavigationBar(
                    containerColor = RadioSurfaceDark,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = currentTab == MainNavTab.FAVORITES,
                        onClick = { viewModel.selectTab(MainNavTab.FAVORITES) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainNavTab.FAVORITES) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "Favoriten"
                            )
                        },
                        label = { Text("Favoriten") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1E1500),
                            selectedTextColor = AmberGoldLight,
                            indicatorColor = AmberGold,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_tab_favorites")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainNavTab.DISCOVER,
                        onClick = { viewModel.selectTab(MainNavTab.DISCOVER) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainNavTab.DISCOVER) Icons.Filled.Radio else Icons.Filled.Explore,
                                contentDescription = "Sender"
                            )
                        },
                        label = { Text("Sender") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1E1500),
                            selectedTextColor = AmberGoldLight,
                            indicatorColor = AmberGold,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_tab_discover")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainNavTab.ONLINE_SEARCH,
                        onClick = { viewModel.selectTab(MainNavTab.ONLINE_SEARCH) },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Suche"
                            )
                        },
                        label = { Text("Suche") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1E1500),
                            selectedTextColor = AmberGoldLight,
                            indicatorColor = AmberGold,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_tab_search")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainNavTab.HISTORY,
                        onClick = { viewModel.selectTab(MainNavTab.HISTORY) },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "Verlauf"
                            )
                        },
                        label = { Text("Verlauf") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1E1500),
                            selectedTextColor = AmberGoldLight,
                            indicatorColor = AmberGold,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_tab_history")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainNavTab.FAVORITES -> {
                    FavoritesTabContent(
                        favorites = profileFavorites,
                        activeProfile = profileConfig.activeProfile,
                        activeProfileTitle = when (profileConfig.activeProfile) {
                            ActiveProfile.MINE -> profileConfig.myName
                            ActiveProfile.HERS -> profileConfig.herName
                            ActiveProfile.BOTH -> profileConfig.sharedName
                        },
                        currentStationId = currentStationId,
                        playbackState = playerState.playbackState,
                        waveAmplitudes = playerState.waveAmplitudes,
                        onPlayStation = { viewModel.playStation(it) },
                        onFavoriteToggle = { station, prof -> viewModel.toggleFavorite(station, prof) },
                        onAssignPreset = { station, slot -> viewModel.setPreset(station, slot) },
                        onExploreClick = { viewModel.selectTab(MainNavTab.DISCOVER) }
                    )
                }

                MainNavTab.DISCOVER -> {
                    DiscoverTabContent(
                        allStations = allStations,
                        selectedCategory = selectedCategory,
                        activeProfileId = profileConfig.activeProfile.id,
                        currentStationId = currentStationId,
                        playbackState = playerState.playbackState,
                        waveAmplitudes = playerState.waveAmplitudes,
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onNavigateToSearch = { viewModel.selectTab(MainNavTab.ONLINE_SEARCH) },
                        onPlayStation = { viewModel.playStation(it) },
                        onFavoriteToggle = { station, prof -> viewModel.toggleFavorite(station, prof) },
                        onAssignPreset = { station, slot -> viewModel.setPreset(station, slot) }
                    )
                }

                MainNavTab.ONLINE_SEARCH -> {
                    OnlineSearchTabContent(
                        searchState = searchState,
                        activeProfileId = profileConfig.activeProfile.id,
                        currentStationId = currentStationId,
                        playbackState = playerState.playbackState,
                        waveAmplitudes = playerState.waveAmplitudes,
                        aiResponse = aiResponse,
                        isAiLoading = isAiLoading,
                        isAiInlineVisible = isAiInlineVisible,
                        onToggleAiInline = { viewModel.toggleAiInline() },
                        onAskAi = { viewModel.askAi(it) },
                        onSelectCombo = { viewModel.applyComboFilter(it) },
                        onSearchTextChange = { viewModel.setSearchQuery(it) },
                        onSelectCountry = { viewModel.setCountryFilter(it) },
                        onSelectGenre = { viewModel.setGenreFilter(it) },
                        onOpenCountryPicker = { viewModel.setShowCountryDialog(true) },
                        onOpenGenrePicker = { viewModel.setShowGenreDialog(true) },
                        onOpenAiDialog = { viewModel.openAiDialog() },
                        onClearFilters = { viewModel.clearSearchFilters() },
                        onPlayStation = { viewModel.playStation(it) },
                        onFavoriteToggle = { station, prof -> viewModel.toggleFavorite(station, prof) },
                        onAssignPreset = { station, slot -> viewModel.setPreset(station, slot) }
                    )
                }

                MainNavTab.HISTORY -> {
                    HistoryAndCustomTabContent(
                        historyStations = historyStations,
                        customStations = customStations,
                        recorderState = recorderState,
                        activeProfileId = profileConfig.activeProfile.id,
                        currentStationId = currentStationId,
                        playbackState = playerState.playbackState,
                        waveAmplitudes = playerState.waveAmplitudes,
                        onPlayStation = { viewModel.playStation(it) },
                        onFavoriteToggle = { station, prof -> viewModel.toggleFavorite(station, prof) },
                        onAssignPreset = { station, slot -> viewModel.setPreset(station, slot) },
                        onAddCustomStation = { viewModel.setShowAddStationDialog(true) },
                        onDeleteStation = { viewModel.deleteStation(it) },
                        onPlayRecording = { viewModel.playRecording(it) },
                        onDeleteRecording = { viewModel.deleteRecording(it) },
                        onStopRecording = { viewModel.stopRecording() }
                    )
                }
            }
        }
    }

    // Modals
    if (showFullPlayer && playerState.currentStation != null) {
        FullPlayerBottomSheet(
            playerState = playerState,
            myName = profileConfig.myName,
            herName = profileConfig.herName,
            sharedName = profileConfig.sharedName,
            onDismiss = { viewModel.setShowFullPlayer(false) },
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onToggleFavorite = { station, prof -> viewModel.toggleFavorite(station, prof) },
            onVolumeChange = { viewModel.setVolume(it) },
            onSoundPresetChange = { viewModel.setSoundPreset(it) },
            onOpenSleepTimer = { viewModel.setShowSleepTimerDialog(true) },
            onAssignPreset = { station, slot -> viewModel.setPreset(station, slot) },
            isRecording = recorderState.isRecording,
            recordingDurationSeconds = recorderState.recordingDurationSeconds,
            onToggleRecording = {
                val st = playerState.currentStation
                if (st != null) {
                    if (recorderState.isRecording) {
                        viewModel.stopRecording()
                    } else {
                        viewModel.startRecording(st)
                    }
                }
            },
            onOpenAiConcierge = { viewModel.openAiDialog() }
        )
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            isActive = playerState.sleepTimerActive,
            minutesRemaining = playerState.sleepTimerMinutesRemaining,
            onSetTimer = { viewModel.startSleepTimer(it) },
            onCancelTimer = { viewModel.cancelSleepTimer() },
            onDismiss = { viewModel.setShowSleepTimerDialog(false) }
        )
    }

    if (showAddStationDialog) {
        AddCustomStationDialog(
            myName = profileConfig.myName,
            herName = profileConfig.herName,
            sharedName = profileConfig.sharedName,
            onAddStation = { name, url, tags, prof ->
                viewModel.addCustomStation(name, url, tags, prof)
            },
            onDismiss = { viewModel.setShowAddStationDialog(false) }
        )
    }

    if (showEditProfileDialog) {
        ProfileEditDialog(
            profileConfig = profileConfig,
            onSave = { my, her, shared ->
                viewModel.updateProfileNames(my, her, shared)
            },
            onDismiss = { viewModel.setShowEditProfileDialog(false) }
        )
    }

    if (showCountryDialog) {
        CountryPickerDialog(
            selectedCountry = searchState.selectedCountry,
            onSelectCountry = { viewModel.setCountryFilter(it) },
            onDismiss = { viewModel.setShowCountryDialog(false) }
        )
    }

    if (showGenreDialog) {
        GenrePickerDialog(
            selectedGenre = searchState.selectedGenre,
            onSelectGenre = { viewModel.setGenreFilter(it) },
            onDismiss = { viewModel.setShowGenreDialog(false) }
        )
    }

    if (showAiDialog) {
        AiConciergeDialog(
            profileConfig = profileConfig,
            aiResponse = aiResponse,
            isLoading = isAiLoading,
            onAskAi = { viewModel.askAi(it) },
            onPlayStation = { viewModel.playStation(it) },
            onFavoriteToggle = { station, prof -> viewModel.toggleFavorite(station, prof) },
            onDismiss = { viewModel.setShowAiDialog(false) }
        )
    }
}

@Composable
private fun FavoritesTabContent(
    favorites: List<RadioStation>,
    activeProfile: ActiveProfile,
    activeProfileTitle: String,
    currentStationId: String?,
    playbackState: PlaybackState,
    waveAmplitudes: List<Float>,
    onPlayStation: (RadioStation) -> Unit,
    onFavoriteToggle: (RadioStation, String) -> Unit,
    onAssignPreset: (RadioStation, Int) -> Unit,
    onExploreClick: () -> Unit
) {
    if (favorites.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141D2D)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Noch keine Favoriten für $activeProfileTitle",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimaryDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tippe im Sender-Katalog auf das Herz-Symbol, um Lieblingssender für dieses Profil zu speichern.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AmberGold,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onExploreClick)
            ) {
                Text(
                    text = "📻 Sender entdecken",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E1500),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIEBLINGSSENDER ($activeProfileTitle)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = when (activeProfile) {
                            ActiveProfile.MINE -> ProfileHisAccent
                            ActiveProfile.HERS -> ProfileHerAccent
                            ActiveProfile.BOTH -> ProfileBothAccent
                        }
                    )
                    Text(
                        text = "${favorites.size} Sender",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiaryDark
                    )
                }
            }

            items(favorites, key = { it.id }) { station ->
                StationCard(
                    station = station,
                    isCurrentStation = station.id == currentStationId,
                    playbackState = playbackState,
                    waveAmplitudes = waveAmplitudes,
                    activeProfileId = activeProfile.id,
                    onPlayClick = { onPlayStation(station) },
                    onFavoriteToggle = { prof -> onFavoriteToggle(station, prof) },
                    onAssignPreset = { slot -> onAssignPreset(station, slot) }
                )
            }
        }
    }
}

@Composable
private fun DiscoverTabContent(
    allStations: List<RadioStation>,
    selectedCategory: String,
    activeProfileId: String,
    currentStationId: String?,
    playbackState: PlaybackState,
    waveAmplitudes: List<Float>,
    onSelectCategory: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onPlayStation: (RadioStation) -> Unit,
    onFavoriteToggle: (RadioStation, String) -> Unit,
    onAssignPreset: (RadioStation, Int) -> Unit
) {
    var selectedCountryFilter by remember { mutableStateOf("Alle") }

    val filteredStations = remember(allStations, selectedCategory, selectedCountryFilter) {
        val byCountry = if (selectedCountryFilter == "Alle") {
            allStations
        } else {
            allStations.filter { it.country.contains(selectedCountryFilter, ignoreCase = true) }
        }

        if (selectedCategory == "Alle") {
            byCountry
        } else {
            val keywords = when (selectedCategory) {
                "Pop & Charts" -> listOf("pop", "charts", "top", "hits")
                "Rock & Alternative" -> listOf("rock", "metal", "alternative", "indie")
                "80er & 90er" -> listOf("80", "90", "retro", "synthpop")
                "Relax & Chillout" -> listOf("relax", "chill", "ambient", "lounge")
                "Klassik & Jazz" -> listOf("klassik", "jazz", "soul", "blues")
                "Elektronisch" -> listOf("elektro", "techno", "dance", "edm", "house")
                "Schlager & Kult" -> listOf("schlager", "kult", "deutsch", "paloma")
                "Nachrichten & Info" -> listOf("nachrichten", "info", "kultur", "dlf")
                else -> listOf(selectedCategory.lowercase())
            }
            byCountry.filter { st ->
                val combined = (st.tags + " " + st.name).lowercase()
                keywords.any { kw -> combined.contains(kw) }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // AI Inspiration Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onNavigateToSearch),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF131B2E),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "✨", fontSize = 18.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lust auf persönliche Musik-Tipps?",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Kombiniere Länder & Genres mit maßgeschneiderten KI-Vorschlägen",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberGoldLight
                        )
                    }
                    Text(
                        text = "Suche & KI ➔",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AmberGold
                    )
                }
            }
        }

        // Country Selector Row
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "LAND",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextTertiaryDark,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val countryChips = listOf(
                        "Alle" to "🌍 Alle Länder",
                        "Deutschland" to "🇩🇪 Deutschland",
                        "Schweiz" to "🇨🇭 Schweiz",
                        "Österreich" to "🇦🇹 Österreich",
                        "International" to "🌐 International"
                    )
                    items(countryChips) { (key, label) ->
                        val isSelected = selectedCountryFilter == key
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedCountryFilter = key },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) AmberGold else Color(0xFF131A29),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AmberGold else RadioCardBorder
                            )
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color(0xFF1E1500) else TextPrimaryDark,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // Genre Selector Row
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "GENRE & MUSIKRICHTUNG",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextTertiaryDark,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CuratedStations.GENRE_CATEGORIES) { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectCategory(category) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) AmberGold else Color(0xFF131A29),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AmberGold else RadioCardBorder
                            )
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color(0xFF1E1500) else TextPrimaryDark,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUALITÄTSSENDER (${filteredStations.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextTertiaryDark
                )
                Text(
                    text = "Direkt-Stream",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF10B981)
                )
            }
        }

        items(filteredStations, key = { it.id }) { station ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                StationCard(
                    station = station,
                    isCurrentStation = station.id == currentStationId,
                    playbackState = playbackState,
                    waveAmplitudes = waveAmplitudes,
                    activeProfileId = activeProfileId,
                    onPlayClick = { onPlayStation(station) },
                    onFavoriteToggle = { prof -> onFavoriteToggle(station, prof) },
                    onAssignPreset = { slot -> onAssignPreset(station, slot) }
                )
            }
        }

        // Worldwide search banner prompt
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onNavigateToSearch),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF111726),
                border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "🔎", fontSize = 24.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Noch mehr Sender gewünscht?",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Über 30.000 weltweite Sender nach Land & Genre durchsuchen",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }
                    Text(
                        text = "Zur Suche ➔",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AmberGold
                    )
                }
            }
        }
    }
}


@Composable
private fun HistoryAndCustomTabContent(
    historyStations: List<RadioStation>,
    customStations: List<RadioStation>,
    recorderState: RecorderUiState,
    activeProfileId: String,
    currentStationId: String?,
    playbackState: PlaybackState,
    waveAmplitudes: List<Float>,
    onPlayStation: (RadioStation) -> Unit,
    onFavoriteToggle: (RadioStation, String) -> Unit,
    onAssignPreset: (RadioStation, Int) -> Unit,
    onAddCustomStation: () -> Unit,
    onDeleteStation: (RadioStation) -> Unit,
    onPlayRecording: (RadioRecording) -> Unit,
    onDeleteRecording: (RadioRecording) -> Unit,
    onStopRecording: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Active recording banner
        if (recorderState.isRecording) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF3B0707),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth().testTag("active_recording_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🔴", fontSize = 16.sp)
                            Column {
                                Text(
                                    text = "Aufnahme läuft: ${recorderState.recordingStation?.name}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFCA5A5)
                                )
                                Text(
                                    text = "Dauer: ${recorderState.recordingDurationSeconds} Sekunden (Live-Mitschnitt)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondaryDark
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEF4444),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onStopRecording)
                        ) {
                            Text(
                                text = "Beenden",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: Saved Recordings (Audials Feature)
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎙️ MEINE MITSCHNITTE & AUFNAHMEN (${recorderState.recordings.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFFCA5A5)
                )

                Text(
                    text = "MP3 Audio",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiaryDark
                )
            }
        }

        if (recorderState.recordings.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RadioCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Noch keine Mitschnitte. Tippe beim Hören eines Senders im Player auf 'Mitschnitt', um Songs oder Sendungen direkt als MP3 aufzunehmen (wie bei Audials!).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(recorderState.recordings, key = { it.file.absolutePath }) { rec ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF141C2E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder),
                    modifier = Modifier.fillMaxWidth().testTag("recording_item_${rec.file.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "🎵", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = rec.stationName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimaryDark
                                )
                                val sizeMb = String.format(java.util.Locale.US, "%.1f MB", rec.sizeBytes / (1024f * 1024f))
                                Text(
                                    text = "${rec.dateFormatted} • $sizeMb",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondaryDark
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Play recording
                            Surface(
                                shape = CircleShape,
                                color = AmberGold,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .clickable { onPlayRecording(rec) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Abspielen",
                                        tint = Color(0xFF1E1500),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Delete recording
                            IconButton(
                                onClick = { onDeleteRecording(rec) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Löschen",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom Stations Header
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EIGENE WEBRADIO-STREAMS (${customStations.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AmberGoldLight
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1B263C),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable(onClick = onAddCustomStation)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Stream hinzufügen",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AmberGold
                        )
                    }
                }
            }
        }

        if (customStations.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RadioCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Noch keine eigenen Streams hinzugefügt. Tippe oben auf 'Stream hinzufügen', um private Webradios oder URLs zu hinterlegen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(customStations, key = { it.id }) { station ->
                StationCard(
                    station = station,
                    isCurrentStation = station.id == currentStationId,
                    playbackState = playbackState,
                    waveAmplitudes = waveAmplitudes,
                    activeProfileId = activeProfileId,
                    onPlayClick = { onPlayStation(station) },
                    onFavoriteToggle = { prof -> onFavoriteToggle(station, prof) },
                    onAssignPreset = { slot -> onAssignPreset(station, slot) },
                    onDeleteCustomStation = { onDeleteStation(station) }
                )
            }
        }

        // History Section
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ZULETZT GEHÖRT (${historyStations.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextTertiaryDark
            )
        }

        if (historyStations.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RadioCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Hier erscheinen die Sender, die Du oder Deine Frau zuletzt gehört habt.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(historyStations, key = { it.id }) { station ->
                StationCard(
                    station = station,
                    isCurrentStation = station.id == currentStationId,
                    playbackState = playbackState,
                    waveAmplitudes = waveAmplitudes,
                    activeProfileId = activeProfileId,
                    onPlayClick = { onPlayStation(station) },
                    onFavoriteToggle = { prof -> onFavoriteToggle(station, prof) },
                    onAssignPreset = { slot -> onAssignPreset(station, slot) }
                )
            }
        }
    }
}
