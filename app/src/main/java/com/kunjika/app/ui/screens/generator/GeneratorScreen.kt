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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.kunjika.app.ui.components.CategoryChip
import com.kunjika.app.ui.components.GlossyButton
import com.kunjika.app.ui.components.GlossyCard
import com.kunjika.app.ui.components.glossyBorder
import com.kunjika.app.ui.components.glossySurface
import com.kunjika.app.ui.components.glossyTopShine
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import com.kunjika.app.core.totp.TotpManager
import com.kunjika.app.ui.components.CustomTextField
import com.kunjika.app.ui.components.StrengthIndicator
import com.kunjika.app.ui.screens.vault.AddEditPasswordDialog
import com.kunjika.app.ui.viewmodel.GeneratorMode
import com.kunjika.app.ui.viewmodel.GeneratorViewModel
import com.kunjika.app.ui.viewmodel.VaultViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    generatorViewModel: GeneratorViewModel,
    vaultViewModel: VaultViewModel,
) {
    val uiState by generatorViewModel.uiState.collectAsState()
    val history by generatorViewModel.history.collectAsState()
    val context = LocalContext.current
    var showSaveDialog by remember { mutableStateOf(false) }
    var showHistoryBottomSheet by remember { mutableStateOf(false) }

    val totpLiveCode by produceState(initialValue = "", key1 = uiState.totpSecret) {
        if (uiState.totpSecret.isNotEmpty()) {
            while (true) {
                value = TotpManager.generateTotp(uiState.totpSecret) ?: ""
                delay(1000)
            }
        } else {
            value = ""
        }
    }

    val totpLiveProgress by produceState(initialValue = 0f) {
        if (uiState.totpSecret.isNotEmpty()) {
            while (true) {
                value = TotpManager.getProgress()
                delay(1000)
            }
        } else {
            value = 0f
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Row with History Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Generator",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Cryptographically secure credentials",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { showHistoryBottomSheet = true }) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Generation History",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mode Tabs (4 Active Generators)
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
                .glossyBorder(shape = RoundedCornerShape(16.dp))
        ) {
            Tab(
                selected = uiState.mode == GeneratorMode.PASSWORD,
                onClick = { generatorViewModel.setMode(GeneratorMode.PASSWORD) },
                text = {
                    Text(
                        text = "Password",
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
            Tab(
                selected = uiState.mode == GeneratorMode.PASSPHRASE,
                onClick = { generatorViewModel.setMode(GeneratorMode.PASSPHRASE) },
                text = {
                    Text(
                        text = "Passphrase",
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
            Tab(
                selected = uiState.mode == GeneratorMode.PIN,
                onClick = { generatorViewModel.setMode(GeneratorMode.PIN) },
                text = {
                    Text(
                        text = "PIN",
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
            Tab(
                selected = uiState.mode == GeneratorMode.TOTP,
                onClick = { generatorViewModel.setMode(GeneratorMode.TOTP) },
                text = {
                    Text(
                        text = "TOTP",
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Generated Output Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glossyBorder(shape = RoundedCornerShape(20.dp)),
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
                val displayBoxBg = MaterialTheme.colorScheme.surfaceVariant
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    displayBoxBg,
                                    displayBoxBg
                                )
                            )
                        )
                        .glossyBorder(shape = RoundedCornerShape(14.dp), highlightColor = Color.White.copy(alpha = 0.6f))
                        .glossyTopShine(alpha = 0.35f)
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.generatedPassword.ifEmpty { "Tap Generate to start" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (uiState.generatedPassword.length > 24) 16.sp else 20.sp
                        ),
                        color = if (uiState.generatedPassword.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time Strength Indicator
                if (uiState.generatedPassword.isNotEmpty()) {
                    StrengthIndicator(strengthResult = uiState.strengthResult)
                } else if (!uiState.isConfigValid && uiState.mode == GeneratorMode.PASSWORD) {
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
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons (Regenerate, Copy, Save)
                val actionsEnabled = uiState.isConfigValid || uiState.mode != GeneratorMode.PASSWORD
                val copyEnabled = actionsEnabled && uiState.generatedPassword.isNotEmpty()
                val disabledBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                val disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlossyButton(
                        onClick = { generatorViewModel.generate() },
                        modifier = Modifier.weight(1.2f),
                        enabled = actionsEnabled
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Generate",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.generatedPassword.isEmpty()) "Generate" else "Regenerate",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Copy Button
                    val copyBtnBg = if (copyEnabled) {
                        listOf(Color(0xFFFDE68A), Color(0xFFF59E0B), Color(0xFFB45309))
                    } else {
                        listOf(disabledBgColor, disabledBgColor)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(brush = Brush.verticalGradient(colors = copyBtnBg))
                            .glossyBorder(
                                shape = RoundedCornerShape(14.dp),
                                highlightColor = if (copyEnabled) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.3f)
                            )
                            .glossyTopShine(alpha = if (copyEnabled) 0.4f else 0.15f)
                            .clickable(enabled = copyEnabled) {
                                ClipboardHelper.copyToClipboard(
                                    context = context,
                                    label = "Generated Password",
                                    text = uiState.generatedPassword,
                                    isSensitive = true,
                                    autoClearSeconds = 30L
                                )
                                Toast.makeText(context, "Copied! Clipboard clears in 30s", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (copyEnabled) Color(0xFF0F172A) else disabledTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Copy",
                                fontWeight = FontWeight.Bold,
                                color = if (copyEnabled) Color(0xFF0F172A) else disabledTextColor,
                                fontSize = 13.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Save Button
                    val saveBtnBg = if (copyEnabled) {
                        listOf(Color(0xFFA5B4FC), Color(0xFF6366F1), Color(0xFF312E81))
                    } else {
                        listOf(disabledBgColor, disabledBgColor)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(brush = Brush.verticalGradient(colors = saveBtnBg))
                            .glossyBorder(
                                shape = RoundedCornerShape(14.dp),
                                highlightColor = if (copyEnabled) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.3f)
                            )
                            .glossyTopShine(alpha = if (copyEnabled) 0.4f else 0.15f)
                            .clickable(enabled = copyEnabled) { showSaveDialog = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "Save",
                                tint = if (copyEnabled) Color.White else disabledTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Save",
                                fontWeight = FontWeight.Bold,
                                color = if (copyEnabled) Color.White else disabledTextColor,
                                fontSize = 13.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Configuration Card
            GlossyCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
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

                        GeneratorMode.TOTP -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Google Authenticator TOTP Secret (160-bit Base32)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                CustomTextField(
                                    value = uiState.totpSecret,
                                    onValueChange = { generatorViewModel.setCustomTotpSecret(it) },
                                    label = "Base32 Secret",
                                    placeholder = "e.g. JBSWY3DPEHPK3PXP..."
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                if (totpLiveCode.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "Live 2FA Code:",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = totpLiveCode.chunked(3).joinToString(" "),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                letterSpacing = 4.sp
                                            )
                                        }

                                        Box(
                                            modifier = Modifier.size(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                progress = { 1f - totpLiveProgress },
                                                modifier = Modifier.fillMaxSize(),
                                                strokeWidth = 3.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        if (showHistoryBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showHistoryBottomSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Generation History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (history.isNotEmpty()) {
                            TextButton(onClick = { generatorViewModel.clearHistory() }) {
                                Text("Clear", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (history.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No history recorded yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        if (showSaveDialog) {
            AddEditPasswordDialog(
                initialPassword = if (uiState.mode == GeneratorMode.TOTP) "" else uiState.generatedPassword,
                initialTotpSecret = if (uiState.mode == GeneratorMode.TOTP) uiState.totpSecret else "",
                vaultViewModel = vaultViewModel
            ) {
                showSaveDialog = false
            }
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
