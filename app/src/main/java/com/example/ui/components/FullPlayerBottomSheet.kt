package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.RadioStation
import com.example.player.PlaybackState
import com.example.player.PlayerUiState
import com.example.player.SoundPreset
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDark
import com.example.ui.theme.AmberGoldLight
import com.example.ui.theme.ProfileBothAccent
import com.example.ui.theme.ProfileHerAccent
import com.example.ui.theme.ProfileHisAccent
import com.example.ui.theme.RadioCardBorder
import com.example.ui.theme.RadioCardDark
import com.example.ui.theme.RadioDarkBg
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerBottomSheet(
    playerState: PlayerUiState,
    myName: String,
    herName: String,
    sharedName: String,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onToggleFavorite: (RadioStation, String) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSoundPresetChange: (SoundPreset) -> Unit,
    onOpenSleepTimer: () -> Unit,
    onAssignPreset: (RadioStation, Int) -> Unit,
    isRecording: Boolean = false,
    recordingDurationSeconds: Int = 0,
    onToggleRecording: (() -> Unit)? = null,
    onOpenAiConcierge: (() -> Unit)? = null
) {
    val station = playerState.currentStation ?: return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isPlaying = playerState.playbackState == PlaybackState.PLAYING
    val isBuffering = playerState.playbackState == PlaybackState.BUFFERING

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0C101A),
        dragHandle = null,
        modifier = Modifier.testTag("full_player_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Close button + Hi-Fi Tuner Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(RadioCardDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Schließen",
                        tint = TextPrimaryDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HI-FI DIGITAL TUNER",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AmberGold
                    )
                    Text(
                        text = "100% Werbefrei",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981)
                    )
                }

                IconButton(
                    onClick = onOpenSleepTimer,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (playerState.sleepTimerActive) AmberGold.copy(alpha = 0.25f) else RadioCardDark)
                        .border(1.dp, if (playerState.sleepTimerActive) AmberGold else RadioCardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Schlummerfunktion",
                        tint = if (playerState.sleepTimerActive) AmberGoldLight else TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Big Glowing Station Artwork
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF131A29))
                    .border(
                        2.dp,
                        Brush.linearGradient(
                            listOf(AmberGold, Color(0xFF1E293B))
                        ),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (station.favicon.isNotBlank()) {
                    AsyncImage(
                        model = station.favicon,
                        contentDescription = station.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(130.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                } else {
                    Text(
                        text = station.name.take(3).uppercase(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 42.sp
                        ),
                        color = AmberGoldLight
                    )
                }
            }

            // Station Name & Genre
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    ),
                    color = TextPrimaryDark,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (station.tags.isNotBlank()) station.tags else "Live Radio Stream",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryDark,
                    textAlign = TextAlign.Center
                )

                // Bitrate & Format Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF172033),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, RadioCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LivePulseBadge()
                        Text(
                            text = "${station.bitrate} kbps • ${station.codec} • ${station.country}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextTertiaryDark
                        )
                    }
                }
            }

            // Live Audio Wave Visualizer (16 bars)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF101726),
                border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AudioVisualizerBars(
                        amplitudes = playerState.waveAmplitudes,
                        barCount = 16,
                        barWidth = 4.dp,
                        barSpacing = 4.dp,
                        maxHeight = 44.dp,
                        primaryColor = AmberGold,
                        secondaryColor = AmberGoldLight
                    )
                }
            }

            // Favorite Profile Selector (Tailored for Frank & Wife)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF131B2C),
                border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "FAVORIT ZUORDNEN:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextTertiaryDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FavoriteProfileChip(
                            title = myName,
                            emoji = "👨",
                            isSelected = station.favoriteProfile == "mine" || station.favoriteProfile == "both",
                            accentColor = ProfileHisAccent,
                            onClick = { onToggleFavorite(station, "mine") },
                            modifier = Modifier.weight(1f)
                        )
                        FavoriteProfileChip(
                            title = herName,
                            emoji = "👩",
                            isSelected = station.favoriteProfile == "hers" || station.favoriteProfile == "both",
                            accentColor = ProfileHerAccent,
                            onClick = { onToggleFavorite(station, "hers") },
                            modifier = Modifier.weight(1f)
                        )
                        FavoriteProfileChip(
                            title = sharedName,
                            emoji = "👫",
                            isSelected = station.favoriteProfile == "both",
                            accentColor = ProfileBothAccent,
                            onClick = { onToggleFavorite(station, "both") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Main Play/Pause Button
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(AmberGold, AmberGoldDark)
                        )
                    )
                    .clickable(onClick = onTogglePlayPause)
                    .testTag("full_player_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = Color(0xFF1E1500),
                        strokeWidth = 3.5.dp
                    )
                } else if (isPlaying) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color(0xFF1E1500),
                        modifier = Modifier.size(42.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Abspielen",
                        tint = Color(0xFF1E1500),
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            // Volume Control Slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (playerState.volume <= 0.05f) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeDown,
                    contentDescription = "Lautstärke leise",
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(20.dp)
                )

                Slider(
                    value = playerState.volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberGold,
                        activeTrackColor = AmberGold,
                        inactiveTrackColor = Color(0xFF22304A)
                    ),
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Lautstärke laut",
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Sound EQ Presets Selector
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF131B2C),
                border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "KLANG-PROFIL (EQUALIZER):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextTertiaryDark
                        )
                        Text(
                            text = playerState.soundPreset.desc,
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberGoldLight
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SoundPreset.entries.forEach { preset ->
                            val isSelected = playerState.soundPreset == preset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AmberGold.copy(alpha = 0.2f) else Color(0xFF0F1522))
                                    .border(1.dp, if (isSelected) AmberGold else RadioCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { onSoundPresetChange(preset) }
                                    .padding(vertical = 6.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) AmberGoldLight else TextSecondaryDark,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Recording & AI Concierge Feature Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Radio Recorder Button
                if (onToggleRecording != null) {
                    val recBg = if (isRecording) Color(0xFF450A0A) else Color(0xFF131B2C)
                    val recBorder = if (isRecording) Color(0xFFEF4444) else RadioCardBorder
                    val recText = if (isRecording) Color(0xFFFCA5A5) else TextPrimaryDark

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = recBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, recBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onToggleRecording)
                            .testTag("full_player_record_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isRecording) "■" else "●",
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRecording) "Stopp (${recordingDurationSeconds}s)" else "Mitschnitt",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = recText
                            )
                        }
                    }
                }

                // AI Concierge Button
                if (onOpenAiConcierge != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2E1065).copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.6f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenAiConcierge)
                            .testTag("full_player_ai_concierge_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "✨", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KI-Butler",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFE9D5FF)
                            )
                        }
                    }
                }
            }

            // Quick Preset Slot Assignment (P1 to P6)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF131B2C),
                border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SCHNELLWAHL-TASTE BELEGEN:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextTertiaryDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        (1..6).forEach { slot ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F1522))
                                    .border(1.dp, RadioCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { onAssignPreset(station, slot) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "P$slot",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AmberGold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FavoriteProfileChip(
    title: String,
    emoji: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color(0xFF0F1522))
            .border(1.dp, if (isSelected) accentColor else RadioCardBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = emoji, fontSize = 12.sp)
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                ),
                color = if (isSelected) TextPrimaryDark else TextSecondaryDark,
                maxLines = 1
            )
        }
    }
}
