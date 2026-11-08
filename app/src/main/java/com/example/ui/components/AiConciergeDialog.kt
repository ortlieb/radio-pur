package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiAssistantResponse
import com.example.data.ai.GeminiRadioAssistant
import com.example.data.model.ActiveProfile
import com.example.data.model.ProfileConfig
import com.example.data.model.RadioStation
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDark
import com.example.ui.theme.AmberGoldLight
import com.example.ui.theme.ProfileBothAccent
import com.example.ui.theme.ProfileHerAccent
import com.example.ui.theme.ProfileHisAccent
import com.example.ui.theme.RadioCardBorder
import com.example.ui.theme.RadioCardDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiConciergeDialog(
    profileConfig: ProfileConfig,
    aiResponse: AiAssistantResponse?,
    isLoading: Boolean,
    onAskAi: (String) -> Unit,
    onPlayStation: (RadioStation) -> Unit,
    onFavoriteToggle: (RadioStation, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customPrompt by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val activeProfileTitle = when (profileConfig.activeProfile) {
        ActiveProfile.MINE -> profileConfig.myName
        ActiveProfile.HERS -> profileConfig.herName
        ActiveProfile.BOTH -> profileConfig.sharedName
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F1523),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(RadioCardBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✨", fontSize = 22.sp)
                    }
                    Column {
                        Text(
                            text = "KI-Radio-Concierge",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Persönliche Empfehlungen für $activeProfileTitle",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberGoldLight
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Schließen",
                        tint = TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Mood & Occasion presets
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "STIMMUNG & ANLASS WÄHLEN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = TextTertiaryDark
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(GeminiRadioAssistant.MOOD_PRESETS) { mood ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onAskAi(mood.prompt) }
                                        .testTag("ai_mood_${mood.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF162035),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Text(text = mood.icon, fontSize = 16.sp)
                                        Column {
                                            Text(
                                                text = mood.title,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimaryDark
                                            )
                                            Text(
                                                text = mood.subtitle,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = TextSecondaryDark,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Custom prompt input field
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customPrompt,
                            onValueChange = { customPrompt = it },
                            placeholder = {
                                Text(
                                    text = "Eigener Wunsch (z.B. 'Chanson aus Paris', 'Rock ohne Werbung')...",
                                    color = TextTertiaryDark,
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (customPrompt.isNotBlank()) {
                                    onAskAi(customPrompt)
                                    focusManager.clearFocus()
                                }
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = RadioCardBorder,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark,
                                focusedContainerColor = Color(0xFF131A2B),
                                unfocusedContainerColor = Color(0xFF131A2B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("ai_custom_prompt_input")
                        )

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                                    )
                                )
                                .clickable {
                                    if (customPrompt.isNotBlank()) {
                                        onAskAi(customPrompt)
                                        focusManager.clearFocus()
                                    }
                                }
                                .testTag("ai_send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Absenden",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 3. Loading State
                if (isLoading) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF8B5CF6),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Der KI-Radio-Concierge berät euch...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryDark
                            )
                        }
                    }
                } else if (aiResponse != null) {
                    // 4. AI Message Greeting
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF161F33),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = "💡", fontSize = 20.sp)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = aiResponse.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimaryDark
                                    )
                                    if (aiResponse.isKeyMissing) {
                                        Text(
                                            text = "ℹ️ Hinweis: Für individuelle KI-Freitextprompts kann ein Gemini API-Key in den AI Studio Secrets hinterlegt werden. Diese geprüften Empfehlungen funktionieren bereits perfekt.",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = TextSecondaryDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Recommended Stations
                    items(aiResponse.suggestions) { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF141C2E),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder),
                            modifier = Modifier.fillMaxWidth().testTag("ai_suggestion_${suggestion.stationName}")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = suggestion.stationName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = AmberGoldLight
                                        )
                                        Text(
                                            text = suggestion.tags + " • " + suggestion.country,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextTertiaryDark
                                        )
                                    }

                                    // Play button
                                    Surface(
                                        shape = CircleShape,
                                        color = AmberGold,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                onPlayStation(suggestion.matchedStation)
                                                onDismiss()
                                            }
                                            .testTag("ai_play_${suggestion.stationName}")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Abspielen",
                                                tint = Color(0xFF1E1500),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }

                                // Why it fits
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F1522),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "„${suggestion.whyItFits}“",
                                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                        color = TextSecondaryDark,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }

                                // Quick Favorite Profile Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Favorisieren:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiaryDark
                                    )

                                    // Mine
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = ProfileHisAccent.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, ProfileHisAccent),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { onFavoriteToggle(suggestion.matchedStation, "mine") }
                                    ) {
                                        Text(
                                            text = "👨 Für Mich",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ProfileHisAccent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Hers
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = ProfileHerAccent.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, ProfileHerAccent),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { onFavoriteToggle(suggestion.matchedStation, "hers") }
                                    ) {
                                        Text(
                                            text = "👩 Für Sie",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ProfileHerAccent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Both
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = ProfileBothAccent.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, ProfileBothAccent),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { onFavoriteToggle(suggestion.matchedStation, "both") }
                                    ) {
                                        Text(
                                            text = "👫 Beide",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ProfileBothAccent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
