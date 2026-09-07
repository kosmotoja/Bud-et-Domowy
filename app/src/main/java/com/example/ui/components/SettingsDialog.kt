package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppSurfaceElevated
import com.example.ui.theme.AppSurfaceGlass
import com.example.ui.theme.ColorIncome
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.TextPrimary

@Composable
fun SettingsDialog(
    isBiometricEnabled: Boolean,
    isRolloverEnabled: Boolean,
    onToggleBiometric: (Boolean) -> Unit,
    onToggleRollover: (Boolean) -> Unit,
    onOpenBackup: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = ColorIncome,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Ustawienia Aplikacji",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Setting 1: Biometrics (Feature 7)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppSurfaceGlass)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = ColorIncome,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Blokada biometryczna",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Wymagaj odcisku palca / PINu przy uruchomieniu aplikacji",
                                fontSize = 10.sp,
                                color = Slate400
                            )
                        }
                    }

                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = onToggleBiometric,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ColorIncome
                        )
                    )
                }

                // Setting 2: Carry-over / Rollover (Feature 8)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppSurfaceGlass)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = ColorIncome,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Przenoszenie wolnych środków",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Automatycznie przenoś niewykorzystane środki z poprzedniego miesiąca",
                                fontSize = 10.sp,
                                color = Slate400
                            )
                        }
                    }

                    Switch(
                        checked = isRolloverEnabled,
                        onCheckedChange = onToggleRollover,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ColorIncome
                        )
                    )
                }

                // Setting 3: Backup & Export (Feature 1)
                androidx.compose.material3.Button(
                    onClick = {
                        onDismiss()
                        onOpenBackup()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.Slate800),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kopia zapasowa i eksport danych (JSON / CSV)", fontSize = 11.sp, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Budżet Domowy v2.0 • Immersive Edition",
                    fontSize = 10.sp,
                    color = Slate500,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Gotowe", color = TextPrimary)
            }
        },
        containerColor = AppSurfaceElevated
    )
}
