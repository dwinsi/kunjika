package com.kunjika.app.ui.screens.vault

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.kunjika.app.core.security.BiometricAuthManager
import com.kunjika.app.core.webdrop.KunjikaBlePeripheral
import com.kunjika.app.core.webdrop.WebDropCrypto
import com.kunjika.app.core.webdrop.WebDropQrPayload
import com.kunjika.app.data.repository.DecryptedPasswordItem
import com.kunjika.app.ui.components.qr.BarcodeScannerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class WebDropState {
    REQUESTING_PERMISSIONS,
    SCANNING,
    CONFIRMING_PAIRING,
    TRANSFERRING,
    SUCCESS,
    ERROR
}

@Composable
fun WebDropScannerDialog(
    item: DecryptedPasswordItem,
    onDismiss: () -> Unit,
    onRecordAudit: (action: String, itemId: Long, title: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var state by remember { mutableStateOf(WebDropState.REQUESTING_PERMISSIONS) }
    var errorMessage by remember { mutableStateOf("") }
    var totpVerificationCode by remember { mutableStateOf("") }
    var sharedSecret by remember { mutableStateOf<ByteArray?>(null) }
    var phonePublicKeyBytes by remember { mutableStateOf<ByteArray?>(null) }
    var blePeripheral by remember { mutableStateOf<KunjikaBlePeripheral?>(null) }
    var isBleConnected by remember { mutableStateOf(false) }

    // Required permissions
    val permissionsToRequest = remember {
        val list = mutableListOf(Manifest.permission.CAMERA)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            list.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            list.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        list.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val cameraGranted = results[Manifest.permission.CAMERA] ?: false
        if (cameraGranted) {
            state = WebDropState.SCANNING
        } else {
            errorMessage = "Camera permission is required to scan the laptop QR code."
            state = WebDropState.ERROR
        }
    }

    LaunchedEffect(Unit) {
        val hasPermissions = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (hasPermissions) {
            state = WebDropState.SCANNING
        } else {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    // Clean up BLE on dismiss
    DisposableEffect(Unit) {
        onDispose {
            blePeripheral?.stop()
        }
    }

    fun handleQrDetected(rawValue: String) {
        if (state != WebDropState.SCANNING) return

        val payload = WebDropCrypto.parseQrPayload(rawValue)
        if (payload == null) {
            errorMessage = "Invalid Kunjika QR format."
            state = WebDropState.ERROR
            return
        }

        // 1. Verify Proof of Work
        val isPowValid = WebDropCrypto.verifyProofOfWork(payload.sid, payload.t, payload.nonce)
        if (!isPowValid) {
            errorMessage = "Proof-of-work verification failed. Unauthenticated session."
            state = WebDropState.ERROR
            return
        }

        // 2. Verify Time Window (prevent replay)
        val isTimeValid = WebDropCrypto.verifyTimeWindow(payload.t)
        if (!isTimeValid) {
            errorMessage = "QR Code session has expired. Please refresh the web page."
            state = WebDropState.ERROR
            return
        }

        try {
            // 3. Derive Shared Secret (ECDH)
            val webPublicKey = WebDropCrypto.parseUncompressedP256PublicKey(payload.pk)
            val phoneKeyPair = WebDropCrypto.generateEphemeralKeyPair()
            val secret = WebDropCrypto.deriveSharedSecret(phoneKeyPair.private, webPublicKey)
            sharedSecret = secret
            phonePublicKeyBytes = WebDropCrypto.exportUncompressedPublicKeyBytes(phoneKeyPair.public)

            // 4. Compute 6-digit TOTP confirmation code
            val code = WebDropCrypto.computeTotpCode(secret, payload.t)
            totpVerificationCode = "${code.take(3)} ${code.takeLast(3)}"

            // 5. Start BLE Peripheral Advertising
            val peripheral = KunjikaBlePeripheral(
                context = context,
                onConnected = {
                    isBleConnected = true
                },
                onTransferSuccess = {
                    state = WebDropState.SUCCESS
                    onRecordAudit("EXPORT_BLE", item.id, item.title)
                    scope.launch {
                        delay(2000)
                        onDismiss()
                    }
                },
                onError = { err ->
                    errorMessage = err
                    state = WebDropState.ERROR
                }
            )
            peripheral.start()
            blePeripheral = peripheral
            state = WebDropState.CONFIRMING_PAIRING

        } catch (e: Exception) {
            errorMessage = "Cryptographic initialization failed: ${e.message}"
            state = WebDropState.ERROR
        }
    }

    fun triggerBiometricAndSend() {
        val secret = sharedSecret
        val peripheral = blePeripheral

        if (secret == null || peripheral == null) {
            errorMessage = "Session expired or invalid"
            state = WebDropState.ERROR
            return
        }

        val activity = context as? FragmentActivity
        val performSend = {
            state = WebDropState.TRANSFERRING
            scope.launch(Dispatchers.IO) {
                // Wait up to 5 seconds if laptop BLE connection is pending
                var attempts = 0
                while (!isBleConnected && attempts < 50) {
                    delay(100)
                    attempts++
                }

                val payloadMap = mapOf(
                    "title" to item.title,
                    "username" to item.username,
                    "password" to item.plaintextPassword,
                    "websiteUrl" to item.websiteUrl,
                    "notes" to item.notes,
                    "totpSecret" to item.totpSecret
                )
                val json = Gson().toJson(payloadMap)
                val encryptedBytes = WebDropCrypto.encryptPayload(json, secret)
                
                // Prepend Phone's 65-byte uncompressed public key for Web Companion ECDH derivation
                val pubKeyBytes = phonePublicKeyBytes ?: ByteArray(0)
                val fullPacket = ByteArray(pubKeyBytes.size + encryptedBytes.size)
                System.arraycopy(pubKeyBytes, 0, fullPacket, 0, pubKeyBytes.size)
                System.arraycopy(encryptedBytes, 0, fullPacket, pubKeyBytes.size, encryptedBytes.size)

                val sent = peripheral.sendEncryptedPayload(fullPacket)
                withContext(Dispatchers.Main) {
                    if (!sent) {
                        errorMessage = "Transfer failed. Ensure laptop is connected via Bluetooth."
                        state = WebDropState.ERROR
                    }
                }
            }
        }

        if (activity != null && BiometricAuthManager.canAuthenticate(context)) {
            BiometricAuthManager.promptBiometric(
                activity = activity,
                title = "Authorize Password Export",
                subtitle = "Send '${item.title}' to paired laptop", negativeButtonText = "Cancel",
                onSuccess = { performSend() },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            performSend()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Web Drop (Air-Gapped)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Zero-Internet BLE Transfer",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (state) {
                    WebDropState.REQUESTING_PERMISSIONS -> {
                        CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                        Text("Requesting Camera & Bluetooth permissions...", style = MaterialTheme.typography.bodySmall)
                    }

                    WebDropState.SCANNING -> {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        ) {
                            BarcodeScannerView(onBarcodeDetected = { raw ->
                                handleQrDetected(raw)
                            })
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Point camera at the QR code on your laptop browser (web-companion)",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    WebDropState.CONFIRMING_PAIRING -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // BLE Connection status indicator
                            val badgeBg = if (isBleConnected) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f) else Color(0xFFFFAB00).copy(alpha = 0.15f)
                            val badgeTextColor = if (isBleConnected) MaterialTheme.colorScheme.tertiary else Color(0xFFFFAB00)
                            val badgeText = if (isBleConnected) "Laptop Connected ✓" else "Awaiting Laptop Pairing..."

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isBleConnected) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            color = badgeTextColor,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeTextColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "PAIRING CODE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = totpVerificationCode,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isBleConnected) {
                                    "Code verified. Tap below to authorize transfer."
                                } else {
                                    "On your laptop screen, click 'Pair Bluetooth (BLE)' to connect."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { triggerBiometricAndSend() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Authorize & Send", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    WebDropState.TRANSFERRING -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(24.dp))
                        Text(
                            text = "Encrypting & Streaming to PC...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    WebDropState.SUCCESS -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Transferred & Sealed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Password copied on laptop. Block recorded in blockchain.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    WebDropState.ERROR -> {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Connection Failed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { state = WebDropState.SCANNING }) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}
