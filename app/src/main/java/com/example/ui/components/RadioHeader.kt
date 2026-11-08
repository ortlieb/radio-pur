package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveProfile
import com.example.data.model.ProfileConfig
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ProfileBothAccent
import com.example.ui.theme.ProfileHerAccent
import com.example.ui.theme.ProfileHisAccent
import com.example.ui.theme.RadioCardBorder
import com.example.ui.theme.RadioCardDark
import com.example.ui.theme.RadioDarkBg
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun RadioHeader(
    profileConfig: ProfileConfig,
    onSelectProfile: (ActiveProfile) -> Unit,
    onEditProfiles: () -> Unit,
    onAddCustomStation: () -> Unit,
    onOpenAiConcierge: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Brand Row + 100% Ad-Free Badge
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
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(AmberGold, Color(0xFFD97706))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = "Radio Pur",
                        tint = Color(0xFF1E1500),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Radio Pur",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "100% Werbefrei für Euch",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AI Concierge Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF2E1065).copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.7f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenAiConcierge)
                        .testTag("ai_concierge_header_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "✨", fontSize = 13.sp)
                        Text(
                            text = "KI-Butler",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE9D5FF)
                        )
                    }
                }

                IconButton(
                    onClick = onAddCustomStation,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RadioCardDark)
                        .border(1.dp, RadioCardBorder, CircleShape)
                        .testTag("add_custom_station_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Eigenen Sender hinzufügen",
                        tint = TextPrimaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onEditProfiles,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RadioCardDark)
                        .border(1.dp, RadioCardBorder, CircleShape)
                        .testTag("edit_profiles_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Profile bearbeiten",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Duo / Couple Profile Selector Pills
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F1522),
            border = androidx.compose.foundation.BorderStroke(1.dp, RadioCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ProfilePill(
                    title = profileConfig.myName,
                    emoji = "👨",
                    isSelected = profileConfig.activeProfile == ActiveProfile.MINE,
                    accentColor = ProfileHisAccent,
                    onClick = { onSelectProfile(ActiveProfile.MINE) },
                    modifier = Modifier.weight(1f)
                )

                ProfilePill(
                    title = profileConfig.herName,
                    emoji = "👩",
                    isSelected = profileConfig.activeProfile == ActiveProfile.HERS,
                    accentColor = ProfileHerAccent,
                    onClick = { onSelectProfile(ActiveProfile.HERS) },
                    modifier = Modifier.weight(1f)
                )

                ProfilePill(
                    title = profileConfig.sharedName,
                    emoji = "👫",
                    isSelected = profileConfig.activeProfile == ActiveProfile.BOTH,
                    accentColor = ProfileBothAccent,
                    onClick = { onSelectProfile(ActiveProfile.BOTH) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProfilePill(
    title: String,
    emoji: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(150),
        label = "pill_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else Color.Transparent,
        animationSpec = tween(150),
        label = "pill_border"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 14.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = if (isSelected) TextPrimaryDark else TextSecondaryDark,
                maxLines = 1
            )
        }
    }
}
