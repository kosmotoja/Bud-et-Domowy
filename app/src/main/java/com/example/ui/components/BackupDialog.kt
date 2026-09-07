package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BudgetBackupManager
import com.example.ui.theme.AppBorderSubtle
import com.example.ui.theme.AppSurfaceElevated
import com.example.ui.theme.AppSurfaceGlass
import com.example.ui.theme.ColorFreeFunds
import com.example.ui.theme.ColorIncome
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupDialog(
    onDismiss: () -> Unit,
    onExportJson: suspend () -> String,
    onExportCsv: suspend () -> String,
    onImportJson: suspend (jsonString: String, replaceExisting: Boolean) -> Result<Int>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var replaceExistingOnImport by remember { mutableStateOf(true) }

    // Launcher for exporting JSON file
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isProcessing = true
                val json = onExportJson()
                val success = BudgetBackupManager.writeTextToUri(context, uri, json)
                isProcessing = false
                statusMessage = if (success) "Pomyślnie wyeksportowano kopię zapasową JSON!" else "Błąd zapisu pliku."
            }
        }
    }

    // Launcher for exporting CSV file
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isProcessing = true
                val csv = onExportCsv()
                val success = BudgetBackupManager.writeTextToUri(context, uri, csv)
                isProcessing = false
                statusMessage = if (success) "Pomyślnie wyeksportowano plik CSV!" else "Błąd zapisu pliku CSV."
            }
        }
    }

    // Launcher for importing JSON file
    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isProcessing = true
                val jsonContent = BudgetBackupManager.readTextFromUri(context, uri)
                if (jsonContent.isNullOrBlank()) {
                    statusMessage = "Nie udało się odczytać pliku."
                } else {
                    val result = onImportJson(jsonContent, replaceExistingOnImport)
                    statusMessage = if (result.isSuccess) {
                        "Zaimportowano pomyślnie! Baza danych została przywrócona."
                    } else {
                        "Błąd podczas importu: ${result.exceptionOrNull()?.message}"
                    }
                }
                isProcessing = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderZip,
                    contentDescription = null,
                    tint = ColorIncome,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Kopia Zapasowa i Eksport",
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
                Text(
                    text = "Zapisz kopię bazy danych (zarobki, wydatki, opłaty cykliczne, cele) lub wyeksportuj dane do arkusza kalkulacyjnego CSV.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )

                if (isProcessing) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = ColorIncome,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Przetwarzanie danych...", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                if (statusMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate800)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = statusMessage!!,
                            fontSize = 11.sp,
                            color = ColorFreeFunds
                        )
                    }
                }

                // Section 1: Eksport
                Text(
                    text = "EKSPORTUJ DANE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                            exportJsonLauncher.launch("budzet_kopia_$timestamp.json")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = ColorIncome)
                            Text("Kopia JSON", fontSize = 11.sp, color = TextPrimary)
                        }
                    }

                    Button(
                        onClick = {
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                            exportCsvLauncher.launch("wydatki_$timestamp.csv")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = ColorIncome)
                            Text("Arkusz CSV", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }

                // Section 2: Import
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "PRZYWRÓĆ KOPIĘ (IMPORT)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 0.5.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = replaceExistingOnImport,
                        onCheckedChange = { replaceExistingOnImport = it },
                        colors = CheckboxDefaults.colors(checkedColor = ColorIncome)
                    )
                    Text(
                        text = "Zastąp istniejące dane (bezpieczna transakcja)",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }

                Button(
                    onClick = {
                        importJsonLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorIncome.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = ColorIncome)
                        Text("Wybierz plik kopii zapasowej (.json)", fontSize = 11.sp, color = ColorIncome, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zamknij", color = TextPrimary)
            }
        },
        containerColor = AppSurfaceElevated
    )
}
