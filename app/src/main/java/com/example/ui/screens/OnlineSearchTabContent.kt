package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiAssistantResponse
import com.example.data.ai.AiStationSuggestion
import com.example.data.ai.GeminiRadioAssistant
import com.example.data.model.CountryGenreCombo
import com.example.data.model.CountryItem
import com.example.data.model.FilterPresets
import com.example.data.model.GenreItem
import com.example.data.model.RadioStation
import com.example.player.PlaybackState
import com.example.ui.SearchUiState
import com.example.ui.components.StationCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDark
import com.example.ui.theme.AmberGoldLight
import com.example.ui.theme.RadioCardBorder
import com.example.ui.theme.RadioCardDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark

@Composable
fun OnlineSearchTabContent(
    searchState: SearchUiState,
    activeProfileId: String,
    currentStationId: String?,
    playbackState: PlaybackState,
    waveAmplitudes: List<Float>,
    aiResponse: AiAssistantResponse?,
    isAiLoading: Boolean,
    isAiInlineVisible: Boolean,
    onToggleAiInline: () -> Unit,
    onAskAi: (String) -> Unit,
    onSelectCombo: (CountryGenreCombo) -> Unit,
    onSearchTextChange: (String) -> Unit,
    onSelectCountry: (CountryItem?) -> Unit,
    onSelectGenre: (GenreItem?) -> Unit,
    onOpenCountryPicker: () -> Unit,
    onOpenGenrePicker: () -> Unit,
    onOpenAiDialog: () -> Unit,
    onClearFilters: () -> Unit,
    onPlayStation: (RadioStation) -> Unit,
    onFavoriteToggle: (RadioStation, String) -> Unit,
    onAssignPreset: (RadioStation, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember(searchState.query) { mutableStateOf(searchState.query) }
    var customAiPrompt by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val hasActiveFilters = searchState.selectedCountry != null ||
            searchState.selectedGenre != null ||
            searchState.query.isNotBlank()

    // Smart Suggestion Detection from Query
    val matchedCountrySuggestion = remember(searchQuery) {
        if (searchQuery.trim().length >= 2) {
            val q = searchQuery.trim().lowercase()
            FilterPresets.POPULAR_COUNTRIES.firstOrNull {
                it.name.lowercase().contains(q) ||
                it.englishName.lowercase().contains(q) ||
                (q.length == 2 && it.code.lowercase() == q)
            }
        } else null
    }

    val matchedGenreSuggestion = remember(searchQuery) {
        if (searchQuery.trim().length >= 2) {
            val q = searchQuery.trim().lowercase()
            FilterPresets.POPULAR_GENRES.firstOrNull {
                it.title.lowercase().contains(q) ||
                it.tag.lowercase().contains(q)
            }
        } else null
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Header Bar with AI Fast-Toggle
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WELTWEITE SENDERSUCHE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = AmberGoldLight
                    )
                    Text(
                        text = "Nach Genre, Land & KI-Vorschlägen suchen",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1B2236),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onToggleAiInline)
                        .testTag("toggle_ai_suggestions_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(text = "✨", fontSize = 14.sp)
                        Text(
                            text = if (isAiInlineVisible) "KI-Tipps an" else "KI-Tipps",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = AmberGold
                        )
                        Icon(
                            imageVector = if (isAiInlineVisible) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 2. Search Text Input
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchTextChange(it)
                    },
                    placeholder = {
                        Text(
                            text = "Sender, Genre oder Land suchen (z.B. Rock, Schweiz)...",
                            color = TextTertiaryDark,
                            fontSize = 13.5.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = AmberGold
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                onSearchTextChange("")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Löschen",
                                    tint = TextSecondaryDark
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = RadioCardBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedContainerColor = Color(0xFF111726),
                        unfocusedContainerColor = Color(0xFF111726)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("online_search_input")
                )

                // Smart Auto-Suggestions pills (when text matches country or genre)
                if (matchedCountrySuggestion != null || matchedGenreSuggestion != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vorschlag:",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberGoldLight
                        )

                        matchedCountrySuggestion?.let { country ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1D283E),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        searchQuery = ""
                                        onSearchTextChange("")
                                        onSelectCountry(country)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = country.flag, fontSize = 12.sp)
                                    Text(
                                        text = "Als Land: ${country.name}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AmberGold
                                    )
                                }
                            }
                        }

                        matchedGenreSuggestion?.let { genre ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1D283E),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        searchQuery = ""
                                        onSearchTextChange("")
                                        onSelectGenre(genre)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = genre.icon, fontSize = 12.sp)
                                    Text(
                                        text = "Als Genre: ${genre.title}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AmberGold
                                    )
                                }
                            }
                        }
                    }
                } else if (searchQuery.isBlank() && !hasActiveFilters) {
                    // Trending Search Suggestions
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = TextTertiaryDark,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Trends:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiaryDark
                                )
                            }
                        }

                        items(FilterPresets.TRENDING_SEARCH_SUGGESTIONS) { term ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF141C2E),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        searchQuery = term
                                        onSearchTextChange(term)
                                    }
                            ) {
                                Text(
                                    text = term,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondaryDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. AI Suggestions & Concierge Section (Prominently requested by user)
        item {
            AnimatedVisibility(
                visible = isAiInlineVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF121929),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(AmberGold.copy(alpha = 0.7f), Color(0xFF8B5CF6).copy(alpha = 0.5f))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // AI Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF8B5CF6), AmberGold)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "✨", fontSize = 16.sp)
                                }

                                Column {
                                    Text(
                                        text = "KI-Musik-Vorschläge & Berater",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimaryDark
                                    )
                                    Text(
                                        text = "Gemini KI empfiehlt passende Webradios für jede Stimmung",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmberGoldLight
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(onClick = onOpenAiDialog)
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = "Vollbild ❯",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AmberGold
                                )
                            }
                        }

                        // AI Quick Mood/Situation Chips (horizontal scrolling)
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(GeminiRadioAssistant.MOOD_PRESETS) { preset ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF1A2338),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onAskAi(preset.prompt) }
                                        .testTag("ai_preset_chip_${preset.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = preset.icon, fontSize = 14.sp)
                                        Text(
                                            text = preset.title,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = TextPrimaryDark
                                        )
                                    }
                                }
                            }
                        }

                        // Custom AI Query Input Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customAiPrompt,
                                onValueChange = { customAiPrompt = it },
                                placeholder = {
                                    Text(
                                        text = "Frag die KI: z.B. Werbefreier Jazz, Italien am Abend...",
                                        fontSize = 12.5.sp,
                                        color = TextTertiaryDark
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (customAiPrompt.isNotBlank()) {
                                        onAskAi(customAiPrompt)
                                        customAiPrompt = ""
                                        focusManager.clearFocus()
                                    }
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AmberGold,
                                    unfocusedBorderColor = RadioCardBorder,
                                    focusedTextColor = TextPrimaryDark,
                                    unfocusedTextColor = TextPrimaryDark,
                                    focusedContainerColor = Color(0xFF0F1522),
                                    unfocusedContainerColor = Color(0xFF0F1522)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ai_custom_query_input")
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AmberGold,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (customAiPrompt.isNotBlank()) {
                                            onAskAi(customAiPrompt)
                                            customAiPrompt = ""
                                            focusManager.clearFocus()
                                        }
                                    }
                                    .testTag("ai_send_query_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Fragen",
                                        tint = Color(0xFF1E1500),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Loading or Result View
                        if (isAiLoading) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = AmberGold,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Gemini KI wählt persönliche Sender aus...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AmberGoldLight
                                )
                            }
                        } else if (aiResponse != null) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Message bubble
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF192237),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = aiResponse.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimaryDark,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }

                                // Recommended Stations Cards
                                aiResponse.suggestions.forEach { suggestion ->
                                    AiInlineSuggestionCard(
                                        suggestion = suggestion,
                                        isCurrentStation = suggestion.matchedStation.id == currentStationId,
                                        isPlaying = playbackState == PlaybackState.PLAYING && suggestion.matchedStation.id == currentStationId,
                                        activeProfileId = activeProfileId,
                                        onPlayClick = { onPlayStation(suggestion.matchedStation) },
                                        onFavoriteToggle = { prof -> onFavoriteToggle(suggestion.matchedStation, prof) },
                                        onAssignPreset = { slot -> onAssignPreset(suggestion.matchedStation, slot) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Popular Combination Suggestions (Dual Country + Genre Multi-Filter)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🎯", fontSize = 14.sp)
                        Text(
                            text = "BELIEBTE LÄNDER & GENRE-KOMBINATIONEN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = TextTertiaryDark
                        )
                    }

                    Text(
                        text = "1-Klick Filter",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberGoldLight
                    )
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FilterPresets.COMBINATION_SUGGESTIONS, key = { it.id }) { combo ->
                        val isComboActive = searchState.selectedCountry?.code == combo.country.code &&
                                searchState.selectedGenre?.tag == combo.genre.tag

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isComboActive) AmberGold else Color(0xFF141C2E),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isComboActive) AmberGold else RadioCardBorder
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (isComboActive) {
                                        onClearFilters()
                                    } else {
                                        onSelectCombo(combo)
                                    }
                                }
                                .testTag("combo_chip_${combo.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = combo.icon, fontSize = 16.sp)
                                Column {
                                    Text(
                                        text = combo.title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isComboActive) FontWeight.Bold else FontWeight.SemiBold
                                        ),
                                        color = if (isComboActive) Color(0xFF1E1500) else TextPrimaryDark
                                    )
                                    Text(
                                        text = combo.subtitle,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (isComboActive) Color(0xFF382A00) else TextSecondaryDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Country Filter Row
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "LAND AUSWÄHLEN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = TextTertiaryDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onOpenCountryPicker)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Alle 25 Länder ➔",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AmberGold
                        )
                    }
                }

                // Quick country chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "Alle Länder" chip
                    item {
                        val isAllSelected = searchState.selectedCountry == null
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectCountry(null) }
                                .testTag("country_chip_all"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAllSelected) AmberGold else Color(0xFF131A29),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isAllSelected) AmberGold else RadioCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(text = "🌍", fontSize = 14.sp)
                                Text(
                                    text = "Alle Länder",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isAllSelected) Color(0xFF1E1500) else TextPrimaryDark
                                )
                            }
                        }
                    }

                    // Popular countries
                    items(FilterPresets.POPULAR_COUNTRIES.take(12), key = { it.code }) { country ->
                        val isSelected = searchState.selectedCountry?.code == country.code
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (isSelected) {
                                        onSelectCountry(null)
                                    } else {
                                        onSelectCountry(country)
                                    }
                                }
                                .testTag("country_chip_${country.code}"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) AmberGold else Color(0xFF131A29),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AmberGold else RadioCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(text = country.flag, fontSize = 14.sp)
                                Text(
                                    text = country.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color(0xFF1E1500) else TextPrimaryDark
                                )
                            }
                        }
                    }

                    // "Mehr Länder..." chip
                    item {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onOpenCountryPicker)
                                .testTag("country_chip_more"),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF162033),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = AmberGoldLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Mehr...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AmberGoldLight
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Genre Filter Row
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "GENRE & MUSIKRICHTUNG",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = TextTertiaryDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onOpenGenrePicker)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Alle 19 Genres ➔",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AmberGold
                        )
                    }
                }

                // Quick genre chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "Alle Genres" chip
                    item {
                        val isAllSelected = searchState.selectedGenre == null
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectGenre(null) }
                                .testTag("genre_chip_all"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAllSelected) AmberGold else Color(0xFF131A29),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isAllSelected) AmberGold else RadioCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(text = "🌟", fontSize = 14.sp)
                                Text(
                                    text = "Alle Genres",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isAllSelected) Color(0xFF1E1500) else TextPrimaryDark
                                )
                            }
                        }
                    }

                    // Popular genres
                    items(FilterPresets.POPULAR_GENRES.take(12), key = { it.tag }) { genre ->
                        val isSelected = searchState.selectedGenre?.tag == genre.tag
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (isSelected) {
                                        onSelectGenre(null)
                                    } else {
                                        onSelectGenre(genre)
                                    }
                                }
                                .testTag("genre_chip_${genre.tag}"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) AmberGold else Color(0xFF131A29),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AmberGold else RadioCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(text = genre.icon, fontSize = 14.sp)
                                Text(
                                    text = genre.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color(0xFF1E1500) else TextPrimaryDark
                                )
                            }
                        }
                    }

                    // "Mehr Genres..." chip
                    item {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onOpenGenrePicker)
                                .testTag("genre_chip_more"),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF162033),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = AmberGoldLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Mehr...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AmberGoldLight
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Active Filter Badges Bar
        if (hasActiveFilters) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Aktiv:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryDark
                    )

                    // Country badge
                    searchState.selectedCountry?.let { country ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberGold.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = country.flag, fontSize = 12.sp)
                                Text(
                                    text = country.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AmberGoldLight
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { onSelectCountry(null) }
                                        .padding(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Entfernen",
                                        tint = AmberGoldLight,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Genre badge
                    searchState.selectedGenre?.let { genre ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberGold.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = genre.icon, fontSize = 12.sp)
                                Text(
                                    text = genre.title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AmberGoldLight
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { onSelectGenre(null) }
                                        .padding(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Entfernen",
                                        tint = AmberGoldLight,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Query badge
                    if (searchState.query.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "\"${searchState.query}\"",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimaryDark
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable {
                                            searchQuery = ""
                                            onSearchTextChange("")
                                        }
                                        .padding(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Entfernen",
                                        tint = TextSecondaryDark,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Reset button
                    Text(
                        text = "Zurücksetzen",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFEF4444),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                searchQuery = ""
                                onClearFilters()
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // 8. Results Counter Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val headerTitle = when {
                    searchState.selectedCountry != null && searchState.selectedGenre != null ->
                        "${searchState.selectedCountry.flag} ${searchState.selectedCountry.name} • ${searchState.selectedGenre.title}"
                    searchState.selectedCountry != null ->
                        "${searchState.selectedCountry.flag} ${searchState.selectedCountry.name}"
                    searchState.selectedGenre != null ->
                        "${searchState.selectedGenre.icon} ${searchState.selectedGenre.title}"
                    searchState.query.isNotBlank() ->
                        "Suche nach \"${searchState.query}\""
                    else -> "WELTWEITE TOP-SENDER"
                }

                Text(
                    text = "$headerTitle (${searchState.searchResults.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = AmberGoldLight
                )

                Text(
                    text = "Live Stream",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF10B981)
                )
            }
        }

        // 9. Loading, Error, or Results List
        if (searchState.isSearching) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = AmberGold, strokeWidth = 3.dp)
                    Text(
                        text = "Suche Radiosender weltweit...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryDark
                    )
                }
            }
        } else if (searchState.searchError != null) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = searchState.searchError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryDark,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF172033),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                searchQuery = ""
                                onClearFilters()
                            }
                    ) {
                        Text(
                            text = "Filter zurücksetzen",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = AmberGold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        } else {
            items(searchState.searchResults, key = { it.id }) { station ->
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
        }
    }
}

@Composable
private fun AiInlineSuggestionCard(
    suggestion: AiStationSuggestion,
    isCurrentStation: Boolean,
    isPlaying: Boolean,
    activeProfileId: String,
    onPlayClick: () -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onAssignPreset: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF151E31),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCurrentStation) AmberGold else RadioCardBorder
        ),
        modifier = Modifier.fillMaxWidth().testTag("ai_suggestion_card_${suggestion.stationName}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrentStation) AmberGold else Color(0xFF222E48)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (suggestion.country.contains("Deutschland", ignoreCase = true)) "🇩🇪"
                            else if (suggestion.country.contains("Schweiz", ignoreCase = true)) "🇨🇭"
                            else if (suggestion.country.contains("Österreich", ignoreCase = true)) "🇦🇹"
                            else "📻",
                            fontSize = 18.sp
                        )
                    }

                    Column {
                        Text(
                            text = suggestion.stationName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isCurrentStation) AmberGoldLight else TextPrimaryDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = suggestion.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Play / Pause Button
                Surface(
                    shape = CircleShape,
                    color = AmberGold,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onPlayClick)
                        .testTag("ai_play_${suggestion.stationName}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Tune else Icons.Default.PlayArrow,
                            contentDescription = "Abspielen",
                            tint = Color(0xFF1E1500),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Why it fits reason
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F1524),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "💡", fontSize = 12.sp)
                    Text(
                        text = suggestion.whyItFits,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                        color = AmberGoldLight
                    )
                }
            }
        }
    }
}
