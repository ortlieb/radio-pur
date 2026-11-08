package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.RadioStation
import com.example.player.PlaybackState
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldLight
import com.example.ui.theme.ProfileBothAccent
import com.example.ui.theme.ProfileHerAccent
import com.example.ui.theme.ProfileHisAccent
import com.example.ui.theme.RadioCardBorder
import com.example.ui.theme.RadioCardDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark

@Composable
fun StationCard(
    station: RadioStation,
    isCurrentStation: Boolean,
    playbackState: PlaybackState,
    waveAmplitudes: List<Float>,
    activeProfileId: String,
    onPlayClick: () -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onAssignPreset: (Int) -> Unit,
    onDeleteCustomStation: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isPlaying = isCurrentStation && playbackState == PlaybackState.PLAYING
    val isBuffering = isCurrentStation && playbackState == PlaybackState.BUFFERING

    var showMenu by remember { mutableStateOf(false) }

    val cardBorderColor = if (isPlaying) {
        AmberGold
    } else if (isBuffering) {
        AmberGold.copy(alpha = 0.5f)
    } else {
        RadioCardBorder
    }

    val cardBgColor = if (isPlaying) {
        Color(0xFF192236)
    } else {
        RadioCardDark
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onPlayClick)
            .testTag("station_card_${station.id}"),
        shape = RoundedCornerShape(14.dp),
        color = cardBgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Station Logo / Favicon Box
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F1522))
                    .border(1.dp, RadioCardBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (station.favicon.isNotBlank()) {
                    AsyncImage(
                        model = station.favicon,
                        contentDescription = station.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = station.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AmberGold
                        )
                    }
                }

                // Playing Overlay Glow
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(AmberGold.copy(alpha = 0.15f))
                    )
                }
            }

            // Station Metadata Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = if (isPlaying) AmberGoldLight else TextPrimaryDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Profile Favorite Tag Badge
                    when (station.favoriteProfile) {
                        "mine" -> ProfileTagBadge("👨", ProfileHisAccent)
                        "hers" -> ProfileTagBadge("👩", ProfileHerAccent)
                        "both" -> ProfileTagBadge("👫", ProfileBothAccent)
                    }
                }

                Text(
                    text = if (station.tags.isNotBlank()) station.tags else "Web Radio • Live Stream",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextSecondaryDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (station.bitrate > 0) {
                        Text(
                            text = "${station.bitrate} kbps ${station.codec}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextTertiaryDark
                        )
                    }
                    if (station.country.isNotBlank()) {
                        Text(
                            text = "• ${station.country}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextTertiaryDark
                        )
                    }
                    if (isPlaying) {
                        LivePulseBadge()
                    }
                }
            }

            // Right Action Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isPlaying) {
                    AudioVisualizerBars(
                        amplitudes = waveAmplitudes,
                        barCount = 4,
                        maxHeight = 22.dp,
                        barWidth = 3.dp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

                // Play / Buffer Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) AmberGold else Color(0xFF243048))
                        .clickable(onClick = onPlayClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AmberGoldLight,
                            strokeWidth = 2.dp
                        )
                    } else if (isPlaying) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color(0xFF1E1500),
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Abspielen",
                            tint = TextPrimaryDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Options Menu (Favorite assignment & Presets)
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (station.favoriteProfile != "none") Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Optionen",
                            tint = when (station.favoriteProfile) {
                                "mine" -> ProfileHisAccent
                                "hers" -> ProfileHerAccent
                                "both" -> ProfileBothAccent
                                else -> TextTertiaryDark
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF172033))
                    ) {
                        DropdownMenuItem(
                            text = { Text("👨 Für Mich markieren", color = TextPrimaryDark) },
                            onClick = {
                                showMenu = false
                                onFavoriteToggle("mine")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("👩 Für Meine Frau markieren", color = TextPrimaryDark) },
                            onClick = {
                                showMenu = false
                                onFavoriteToggle("hers")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("👫 Für Beide (Gemeinsam)", color = TextPrimaryDark) },
                            onClick = {
                                showMenu = false
                                onFavoriteToggle("both")
                            }
                        )
                        if (station.favoriteProfile != "none") {
                            DropdownMenuItem(
                                text = { Text("❌ Favorit entfernen", color = TextSecondaryDark) },
                                onClick = {
                                    showMenu = false
                                    onFavoriteToggle(station.favoriteProfile)
                                }
                            )
                        }

                        // Presets sub-options
                        (1..6).forEach { pIndex ->
                            DropdownMenuItem(
                                text = { Text("📻 Auf Preset P$pIndex speichern", color = AmberGoldLight) },
                                onClick = {
                                    showMenu = false
                                    onAssignPreset(pIndex)
                                }
                            )
                        }

                        if (station.isCustom && onDeleteCustomStation != null) {
                            DropdownMenuItem(
                                text = { Text("🗑️ Sender löschen", color = Color(0xFFEF4444)) },
                                onClick = {
                                    showMenu = false
                                    onDeleteCustomStation()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTagBadge(emoji: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.2f))
            .border(0.5.dp, color, RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 11.sp
        )
    }
}
