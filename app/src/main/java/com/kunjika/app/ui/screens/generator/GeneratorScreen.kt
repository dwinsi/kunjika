package com.kunjika.app.ui.screens.generator

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kunjika.app.core.security.ClipboardHelper
import com.kunjika.app.ui.components.StrengthIndicator
import com.kunjika.app.ui.screens.vault.AddEditPasswordDialog
import com.kunjika.app.ui.viewmodel.GeneratorMode
import com.kunjika.app.ui.viewmodel.GeneratorViewModel
import com.kunjika.app.ui.viewmodel.VaultViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeneratorScreen(
    generatorViewModel: GeneratorViewModel,
    vaultViewModel: VaultViewModel,
) {
    val uiState by generatorViewModel.uiState.collectAsState()
    val history by generatorViewModel.history.collectAsState()
    val context = LocalContext.current
    var showSaveDialog by remember { mutableStateOf(value = false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Tabs
        TabRow(
            selectedTabIndex = uiState.mode.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[uiState.mode.ordinal]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Tab(
                selected = uiState.mode == GeneratorMode.PASSWORD,
                onClick = { generatorViewModel.setMode(GeneratorMode.PASSWORD) },
                text = { Text("Password", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = uiState.mode == GeneratorMode.PASSPHRASE,
                onClick = { generatorViewModel.setMode(GeneratorMode.PASSPHRASE) },
                text = { Text("Passphrase", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = uiState.mode == GeneratorMode.PIN,
                onClick = { generatorViewModel.setMode(GeneratorMode.PIN) },
                text = { Text("PIN", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = uiState.mode == GeneratorMode.HISTORY,
                onClick = { generatorViewModel.setMode(GeneratorMode.HISTORY) },
                text = { Text("History", fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.mode != GeneratorMode.HISTORY) {
            // Generated Output Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Password Display Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.generatedPassword,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (uiState.generatedPassword.length > 24) 17.sp else 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Real-time Strength Indicator
                    if (uiState.isConfigValid || uiState.mode != GeneratorMode.PASSWORD) {
                        StrengthIndicator(strengthResult = uiState.strengthResult)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Please select at least 3 character types",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons (Regenerate, Copy, Save)
                    val actionsEnabled = uiState.isConfigValid || uiState.mode != GeneratorMode.PASSWORD
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { generatorViewModel.generate() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = actionsEnabled
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Generate new random password",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New")
                        }

                        Button(
                            onClick = {
                                ClipboardHelper.copyToClipboard(
                                    context = context,
                                    label = "Generated Password",
                                    text = uiState.generatedPassword,
                                    isSensitive = true,
                                    autoClearSeconds = 30L
                                )
                                Toast.makeText(context, "Copied! Clipboard clears in 30s", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = actionsEnabled && uiState.generatedPassword.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy password to clipboard",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy")
                        }

                        Button(
                            onClick = { showSaveDialog = true },
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            enabled = actionsEnabled && uiState.generatedPassword.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "Save generated password to vault",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Customization Options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    when (uiState.mode) {
                        GeneratorMode.PASSWORD -> {
                            // Length Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Password Length", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "${uiState.length} chars",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Slider(
                                value = uiState.length.toFloat(),
                                onValueChange = { generatorViewModel.setLength(it.toInt()) },
                                valueRange = 8f..64f,
                                steps = 55,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Charset toggles
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = uiState.includeUppercase,
                                    onClick = { generatorViewModel.toggleUppercase() },
                                    label = { Text("A-Z") }
                                )
                                FilterChip(
                                    selected = uiState.includeLowercase,
                                    onClick = { generatorViewModel.toggleLowercase() },
                                    label = { Text("a-z") }
                                )
                                FilterChip(
                                    selected = uiState.includeNumbers,
                                    onClick = { generatorViewModel.toggleNumbers() },
                                    label = { Text("0-9") }
                                )
                                FilterChip(
                                    selected = uiState.includeSymbols,
                                    onClick = { generatorViewModel.toggleSymbols() },
                                    label = { Text("!@#$%^&*()_") }
                                )
                                FilterChip(
                                    selected = uiState.excludeAmbiguous,
                                    onClick = { generatorViewModel.toggleExcludeAmbiguous() },
                                    label = { Text("No Ambiguous") }
                                )
                            }
                        }

                        GeneratorMode.PASSPHRASE -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Word Count", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "${uiState.wordCount} words",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Slider(
                                value = uiState.wordCount.toFloat(),
                                onValueChange = { generatorViewModel.setWordCount(it.toInt()) },
                                valueRange = 3f..8f,
                                steps = 4,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = uiState.capitalize,
                                    onClick = { generatorViewModel.toggleCapitalize() },
                                    label = { Text("Capitalize") }
                                )
                                FilterChip(
                                    selected = uiState.includeNumberInPassphrase,
                                    onClick = { generatorViewModel.toggleIncludeNumberInPassphrase() },
                                    label = { Text("Include Number") }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(text = "Separator", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("-", "_", ".", " ", "#").forEach { sep ->
                                    FilterChip(
                                        selected = uiState.separator == sep,
                                        onClick = { generatorViewModel.setSeparator(sep) },
                                        label = { Text(if (sep == " ") "Space" else sep) }
                                    )
                                }
                            }
                        }

                        GeneratorMode.PIN -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "PIN Digits", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "${uiState.pinLength} digits",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Slider(
                                value = uiState.pinLength.toFloat(),
                                onValueChange = { generatorViewModel.setPinLength(it.toInt()) },
                                valueRange = 4f..12f,
                                steps = 7,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        else -> {}
                    }
                }
            }
        } else {
            // History Tab Content
            if (history.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Generation History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(onClick = { generatorViewModel.clearHistory() }) {
                                Text("Clear", color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            history.forEach { item ->
                                HistoryItemRow(
                                    item = item,
                                    onCopy = {
                                        ClipboardHelper.copyToClipboard(
                                            context = context,
                                            label = "History Password",
                                            text = item.plaintextPassword,
                                            isSensitive = true
                                        )
                                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No history yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showSaveDialog) {
        AddEditPasswordDialog(
            initialPassword = uiState.generatedPassword,
            vaultViewModel = vaultViewModel
        ) {
            showSaveDialog = false
        }
    }
}

@Composable
fun HistoryItemRow(
    item: com.kunjika.app.data.repository.DecryptedHistoryItem,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable { onCopy() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.plaintextPassword,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = "${item.type} • ${android.text.format.DateUtils.getRelativeTimeSpanString(item.timestamp)}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
    }
}
