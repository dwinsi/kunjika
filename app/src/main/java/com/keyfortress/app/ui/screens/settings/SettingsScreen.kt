package com.keyfortress.app.ui.screens.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keyfortress.app.core.security.ClipboardHelper
import com.keyfortress.app.ui.components.CustomTextField
import com.keyfortress.app.ui.viewmodel.AuthViewModel
import com.keyfortress.app.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel
) {
    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()
    val lockOnExit by settingsViewModel.lockOnExit.collectAsState()
    val autoLockTimeout by settingsViewModel.autoLockTimeoutSec.collectAsState()
    val securityStatus by settingsViewModel.securityStatus.collectAsState()
    val useDynamicColor by settingsViewModel.useDynamicColor.collectAsState()
    val useDarkTheme by settingsViewModel.useDarkTheme.collectAsState()
    val backupStatus by settingsViewModel.backupStatus.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showChangePinDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Air-Gapped Security Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (securityStatus.isRooted) 
                    MaterialTheme.colorScheme.errorContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (securityStatus.isRooted) Icons.Default.Security else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = if (securityStatus.isRooted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = if (securityStatus.isRooted) "SECURITY WARNING: Device Rooted" else "100% Offline & Air-Gapped",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (securityStatus.isRooted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (securityStatus.isRooted) 
                            "Root access detected. Your encrypted vault is at risk from other apps with root privileges."
                        else 
                            "Zero internet permissions. Your encrypted credentials never leave this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (securityStatus.isRooted) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security & Authentication Settings
        SettingsSection(title = "Security & Access") {
            // Biometrics Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Biometric Unlock", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Use fingerprint or face recognition", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = isBiometricEnabled,
                    onCheckedChange = { settingsViewModel.setBiometricEnabled(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }

            // Lock on Exit Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Lock on Exit", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Require unlock every time you leave app", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = lockOnExit,
                    onCheckedChange = { settingsViewModel.setLockOnExit(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Auto-lock options
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Auto-Lock Timeout", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Instant", 30 to "30s", 60 to "1m", 300 to "5m").forEach { (sec, label) ->
                        FilterChip(
                            selected = autoLockTimeout == sec,
                            onClick = { settingsViewModel.setAutoLockTimeout(sec) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Change Master PIN
            OutlinedButton(
                onClick = { showChangePinDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Change Master PIN")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lock Vault Now
            Button(
                onClick = { authViewModel.lock() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lock Vault Now")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // UI & Personalization Settings
        SettingsSection(title = "UI & Personalization") {
            // Dark Theme Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Dark Theme", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Force dark mode for privacy", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = useDarkTheme,
                    onCheckedChange = { settingsViewModel.setDarkTheme(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }

            // Dynamic Color Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Material You", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Use system colors (Android 12+)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = useDynamicColor,
                    onCheckedChange = { settingsViewModel.setDynamicColor(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Encrypted Backup & Restore
        SettingsSection(title = "Encrypted Offline Backup") {
            Text(
                text = "Export and import your encrypted database securely. All backups are encrypted with AES-256-GCM using your custom passphrase.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export")
                }

                OutlinedButton(
                    onClick = { showImportDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import")
                }
            }

            AnimatedVisibility(visible = backupStatus != null) {
                Text(
                    text = backupStatus ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hardware & Architecture info
        SettingsSection(title = "Security Architecture") {
            Text(text = "• Target Platform: Android 16+ (API 36)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "• Vault Encryption: SQLCipher + Android KeyStore (AES-GCM)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "• Screen Security: FLAG_SECURE active (Screenshots blocked)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "• Clipboard: Android 13+ EXTRA_IS_SENSITIVE with 30s auto-purge", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "• Internet Permission: None (Zero network socket access)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Change Master PIN") },
            text = {
                Column {
                    CustomTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) newPin = it },
                        label = "New PIN (min 4 digits)",
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    CustomTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) confirmPin = it },
                        label = "Confirm New PIN",
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newPin.length < 4) {
                        error = "PIN must be at least 4 digits"
                    } else if (newPin != confirmPin) {
                        error = "PINs do not match"
                    } else {
                        settingsViewModel.changeMasterPin(newPin)
                        showChangePinDialog = false
                        Toast.makeText(context, "PIN changed successfully", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Update PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        var exportPassphrase by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Encrypted Backup") },
            text = {
                Column {
                    Text("Choose a strong passphrase to encrypt your exported backup payload:")
                    Spacer(modifier = Modifier.height(10.dp))
                    CustomTextField(
                        value = exportPassphrase,
                        onValueChange = { exportPassphrase = it },
                        label = "Backup Passphrase",
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (exportPassphrase.isNotBlank()) {
                            settingsViewModel.exportEncryptedBackup(exportPassphrase) { payload ->
                                ClipboardHelper.copyToClipboard(context, "Encrypted Backup", payload, isSensitive = true)
                                Toast.makeText(context, "Encrypted backup copied to clipboard!", Toast.LENGTH_LONG).show()
                            }
                            showExportDialog = false
                        }
                    },
                    enabled = exportPassphrase.isNotBlank()
                ) {
                    Text("Export to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        var importPayload by remember { mutableStateOf("") }
        var importPassphrase by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Encrypted Backup") },
            text = {
                Column {
                    CustomTextField(
                        value = importPayload,
                        onValueChange = { importPayload = it },
                        label = "Paste Backup JSON Payload",
                        singleLine = false,
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    CustomTextField(
                        value = importPassphrase,
                        onValueChange = { importPassphrase = it },
                        label = "Backup Passphrase",
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importPayload.isNotBlank() && importPassphrase.isNotBlank()) {
                            settingsViewModel.importEncryptedBackup(importPayload, importPassphrase)
                            showImportDialog = false
                        }
                    },
                    enabled = importPayload.isNotBlank() && importPassphrase.isNotBlank()
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
