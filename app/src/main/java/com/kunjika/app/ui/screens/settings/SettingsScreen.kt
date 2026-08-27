package com.kunjika.app.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.autofill.AutofillManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kunjika.app.core.security.ClipboardHelper
import com.kunjika.app.ui.components.CustomTextField
import com.kunjika.app.ui.viewmodel.AuthViewModel
import com.kunjika.app.ui.viewmodel.SettingsViewModel

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
    val pinHint by settingsViewModel.pinHint.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showChangePinDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    var exportFilePassphrase by remember { mutableStateOf("") }
    var importFileUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showImportFilePassphraseDialog by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { settingsViewModel.exportToFile(context, it, exportFilePassphrase) }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { 
            importFileUri = it
            showImportFilePassphraseDialog = true
        }
    }

    val autofillManager = remember { context.getSystemService(AutofillManager::class.java) }
    val isAutofillEnabled = autofillManager?.hasEnabledAutofillServices() == true

    val autofillLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // Refresh check if needed
    }

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

            Spacer(modifier = Modifier.height(8.dp))

            // Enable Autofill Button
            OutlinedButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                        data = android.net.Uri.parse("package:${context.packageName}")
                    }
                    try {
                        autofillLauncher.launch(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Please enable Kunjika in System Autofill settings", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = if (isAutofillEnabled) 
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                else 
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(
                    imageVector = if (isAutofillEnabled) Icons.Default.CheckCircle else Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isAutofillEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAutofillEnabled) "Autofill: Enabled" else "Enable Autofill Service",
                    color = if (isAutofillEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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

        // Emergency Recovery Section
        SettingsSection(title = "Security & Recovery") {
            Text(
                text = "Set a hint for your Master PIN and generate a physical recovery kit. This is your ultimate safety net if you lose access to your device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = pinHint,
                onValueChange = { settingsViewModel.setPinHint(it) },
                label = "Master PIN Hint",
                placeholder = "e.g. My favorite car + graduation year"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    settingsViewModel.generateRecoveryKit(context) { file ->
                        if (file != null) {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Open Recovery Kit"))
                        } else {
                            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Recovery Kit (PDF)")
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
                Column(modifier = Modifier.weight(1f)) {
                    Text("Clipboard", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export")
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("File (.json)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { 
                            // We need a passphrase first, then launch picker
                            showExportDialog = true 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save File")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showImportDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Text")
                }

                OutlinedButton(
                    onClick = { openDocumentLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*")) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Load File")
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


        // Security Architecture Info
        SettingsSection(title = "Security Architecture") {
            SecurityInfoRow(
                label = "KeyStore Storage",
                value = if (securityStatus.isHardwareBacked) "Hardware (TEE/HSM)" else "Software Bound",
                isGood = securityStatus.isHardwareBacked
            )
            SecurityInfoRow(
                label = "Device Integrity",
                value = if (securityStatus.isRooted) "Compromised (Rooted)" else "Official / Secure",
                isGood = !securityStatus.isRooted
            )
            SecurityInfoRow(
                label = "Execution Environment",
                value = if (securityStatus.isEmulator) "Virtual (Emulator)" else "Physical Device",
                isGood = !securityStatus.isEmulator
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(text = "• Vault Encryption: SQLCipher + AES-GCM", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "• Screen Security: FLAG_SECURE Active", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "• Internet Permission: None", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About Section
        SettingsSection(title = "About Kunjika") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Kunjika", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Kunjika is a zero-network, hardware-hardened password manager designed for users who demand absolute privacy.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Developed by", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("Ashwin Singh", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("License", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("MIT License", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://dwinsi.github.io/kunjika/PRIVACY_POLICY.html"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Privacy Policy", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://dwinsi.github.io/kunjika/docs/"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Documentation", fontSize = 12.sp)
                }
            }
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
                    Text("Choose a strong passphrase to encrypt your exported backup payload. This passphrase is required to restore on any device.")
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
                Row {
                    TextButton(
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
                        Text("To Clipboard")
                    }
                    Button(
                        onClick = {
                            if (exportPassphrase.isNotBlank()) {
                                exportFilePassphrase = exportPassphrase
                                createDocumentLauncher.launch("kunjika_backup_${System.currentTimeMillis()}.json")
                                showExportDialog = false
                            }
                        },
                        enabled = exportPassphrase.isNotBlank()
                    ) {
                        Text("To File")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Import from File Passphrase Dialog
    if (showImportFilePassphraseDialog) {
        var passphrase by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showImportFilePassphraseDialog = false },
            title = { Text("Import from File") },
            text = {
                Column {
                    Text("Enter the passphrase used to encrypt this backup file:")
                    Spacer(modifier = Modifier.height(10.dp))
                    CustomTextField(
                        value = passphrase,
                        onValueChange = { passphrase = it },
                        label = "Backup Passphrase",
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        importFileUri?.let { uri ->
                            settingsViewModel.importFromFile(context, uri, passphrase)
                        }
                        showImportFilePassphraseDialog = false
                    },
                    enabled = passphrase.isNotBlank()
                ) {
                    Text("Restore File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportFilePassphraseDialog = false }) { Text("Cancel") }
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
private fun SecurityInfoRow(
    label: String,
    value: String,
    isGood: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = if (isGood) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
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
