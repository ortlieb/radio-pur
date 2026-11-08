package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ProfileConfig
import com.example.ui.theme.AmberGold
import com.example.ui.theme.RadioCardBorder
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun ProfileEditDialog(
    profileConfig: ProfileConfig,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var myName by remember { mutableStateOf(profileConfig.myName) }
    var herName by remember { mutableStateOf(profileConfig.herName) }
    var sharedName by remember { mutableStateOf(profileConfig.sharedName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131A29),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AmberGold
                )
                Text(
                    text = "Profile anpassen",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimaryDark
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Personalisiert die Profile für Dich und Deine Frau (z. B. Frank & Andrea):",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )

                OutlinedTextField(
                    value = myName,
                    onValueChange = { myName = it },
                    label = { Text("👨 Dein Name / Profil") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = RadioCardBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("my_name_input")
                )

                OutlinedTextField(
                    value = herName,
                    onValueChange = { herName = it },
                    label = { Text("👩 Name Deiner Frau / Profil") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = RadioCardBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("her_name_input")
                )

                OutlinedTextField(
                    value = sharedName,
                    onValueChange = { sharedName = it },
                    label = { Text("👫 Gemeinsam / Wohnzimmer") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = RadioCardBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(myName, herName, sharedName)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberGold,
                    contentColor = Color(0xFF1E1500)
                ),
                modifier = Modifier.testTag("save_profiles_button")
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextSecondaryDark)
            ) {
                Text("Abbrechen")
            }
        },
        modifier = Modifier.testTag("profile_edit_dialog")
    )
}
