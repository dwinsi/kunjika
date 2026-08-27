package com.keyfortress.app.ui.screens.vault

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.keyfortress.app.core.generator.PasswordStrengthEvaluator
import com.keyfortress.app.core.qr.QrSyncPayload
import com.keyfortress.app.core.security.ClipboardHelper
import com.keyfortress.app.core.totp.TotpManager
import com.keyfortress.app.data.repository.DecryptedPasswordItem
import com.keyfortress.app.ui.components.CategoryChip
import com.keyfortress.app.ui.components.CustomTextField
import com.keyfortress.app.ui.components.qr.BarcodeScannerView
import com.keyfortress.app.ui.theme.StrengthFair
import com.keyfortress.app.ui.theme.StrengthStrong
import com.keyfortress.app.ui.theme.StrengthVeryStrong
import com.keyfortress.app.ui.theme.StrengthVeryWeak
import com.keyfortress.app.ui.theme.StrengthWeak
import com.keyfortress.app.ui.viewmodel.VaultViewModel

@Composable
fun VaultScreen(vaultViewModel: VaultViewModel) {
    val filteredPasswords by vaultViewModel.filteredPasswords.collectAsState()
    val selectedCategory by vaultViewModel.selectedCategory.collectAsState()
    val searchQuery by vaultViewModel.searchQuery.collectAsState()

    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(value = false) }
    var showScanScreen by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScanScreen = true
        } else {
            Toast.makeText(context, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show()
        }
    }

    var scannedEncryptedData by remember { mutableStateOf<String?>(null) }
    var transferCodeInput by remember { mutableStateOf("") }
    var showCodeDialog by remember { mutableStateOf(false) }

    var selectedItemForDetail by remember { mutableStateOf<DecryptedPasswordItem?>(null) }
    var selectedItemForEdit by remember { mutableStateOf<DecryptedPasswordItem?>(null) }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan QR code to import password"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add new password to vault"
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            CustomTextField(
                value = searchQuery,
                onValueChange = { vaultViewModel.onSearchQueryChanged(it) },
                label = "Search vault",
                placeholder = "Search accounts, sites, emails...",
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { vaultViewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Category Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                vaultViewModel.categories.forEach { category ->
                    CategoryChip(
                        label = category,
                        isSelected = selectedCategory == category,
                        onClick = { vaultViewModel.selectCategory(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password List or Empty State
            if (filteredPasswords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No results found" else "Vault is empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try a different search keyword" else "Tap '+' below to add your first password",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPasswords, key = { it.id }) { item ->
                        PasswordCardItem(
                            item = item,
                            onClick = { selectedItemForDetail = item },
                            onToggleFavorite = { vaultViewModel.toggleFavorite(item) }
                        ) {
                            ClipboardHelper.copyToClipboard(
                                context = context,
                                label = item.title,
                                text = item.plaintextPassword,
                                isSensitive = true,
                                autoClearSeconds = 30L
                            )
                            Toast.makeText(context, "${item.title} password copied (auto-clears in 30s)", Toast.LENGTH_SHORT).show()
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddEditPasswordDialog(
            vaultViewModel = vaultViewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    selectedItemForDetail?.let { item ->
        PasswordDetailDialog(
            item = item,
            onEdit = {
                selectedItemForDetail = null
                selectedItemForEdit = item
            },
            onDelete = { vaultViewModel.deletePassword(item.id) },
            onToggleFavorite = { vaultViewModel.toggleFavorite(item) },
            onDismiss = { selectedItemForDetail = null }
        )
    }

    selectedItemForEdit?.let { item ->
        AddEditPasswordDialog(
            existingItem = item,
            vaultViewModel = vaultViewModel,
            onDismiss = { selectedItemForEdit = null }
        )
    }

    if (showScanScreen) {
        Dialog(onDismissRequest = { showScanScreen = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    BarcodeScannerView(onBarcodeDetected = { data ->
                        scannedEncryptedData = data
                        showScanScreen = false
                        showCodeDialog = true
                    })
                    
                    Text(
                        text = "Scan QR to Import",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }

    if (showCodeDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCodeDialog = false },
            title = { Text("Enter Transfer Code") },
            text = {
                Column {
                    Text("The sender's screen is displaying a 6-digit code. Please enter it here to decrypt the vault entry.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomTextField(
                        value = transferCodeInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) transferCodeInput = it },
                        label = "6-Digit Code",
                        placeholder = "000000",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val data = scannedEncryptedData
                        if (data != null) {
                            vaultViewModel.importEncryptedQr(data, transferCodeInput) { success ->
                                if (success) {
                                    Toast.makeText(context, "Entry imported successfully", Toast.LENGTH_SHORT).show()
                                    showCodeDialog = false
                                    scannedEncryptedData = null
                                    transferCodeInput = ""
                                } else {
                                    Toast.makeText(context, "Invalid code or corrupted data", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    enabled = transferCodeInput.length == 6
                ) {
                    Text("Decrypt & Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCodeDialog = false
                    scannedEncryptedData = null
                    transferCodeInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PasswordCardItem(
    item: DecryptedPasswordItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopyPassword: () -> Unit
) {
    val context = LocalContext.current
    val strength = remember(item.plaintextPassword) {
        PasswordStrengthEvaluator.evaluate(item.plaintextPassword)
    }

    val totpCode by produceState(initialValue = "", key1 = item.totpSecret) {
        if (!item.totpSecret.isNullOrEmpty()) {
            while (true) {
                value = TotpManager.generateTotp(item.totpSecret) ?: ""
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    val totpProgress by produceState(initialValue = 0f) {
        if (!item.totpSecret.isNullOrEmpty()) {
            while (true) {
                value = TotpManager.getProgress()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    val strengthColor = when (strength.score) {
        com.keyfortress.app.core.generator.PasswordStrength.VERY_WEAK -> StrengthVeryWeak
        com.keyfortress.app.core.generator.PasswordStrength.WEAK -> StrengthWeak
        com.keyfortress.app.core.generator.PasswordStrength.FAIR -> StrengthFair
        com.keyfortress.app.core.generator.PasswordStrength.STRONG -> StrengthStrong
        com.keyfortress.app.core.generator.PasswordStrength.VERY_STRONG -> StrengthVeryStrong
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category Icon Badge & Info
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.title.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (item.username.isNotEmpty()) {
                            Text(
                                text = item.username,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(strengthColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.category,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Action icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onCopyPassword) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Password",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (totpCode.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        .clickable {
                            ClipboardHelper.copyToClipboard(context, item.title + " 2FA", totpCode)
                            Toast
                                .makeText(context, "TOTP Code copied", Toast.LENGTH_SHORT)
                                .show()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2FA Code:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = totpCode.chunked(3).joinToString(" "),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            progress = { 1f - totpProgress },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}
