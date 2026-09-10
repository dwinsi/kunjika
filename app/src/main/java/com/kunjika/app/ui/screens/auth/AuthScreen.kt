package com.kunjika.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.kunjika.app.core.security.BiometricAuthManager
import com.kunjika.app.core.security.SecurityManager
import com.kunjika.app.ui.components.CustomTextField
import com.kunjika.app.ui.components.GlossyButton
import com.kunjika.app.ui.components.GlossyCard
import com.kunjika.app.ui.components.GlossyIconBox
import com.kunjika.app.ui.components.glossyBorder
import com.kunjika.app.ui.components.glossyTopShine
import com.kunjika.app.ui.viewmodel.AuthState
import com.kunjika.app.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(authViewModel: AuthViewModel) {
    val authState by authViewModel.authState.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val isBiometricEnabled by authViewModel.isBiometricEnabled.collectAsState()

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirmStep by remember { mutableStateOf(false) }

    val isSetup = authState == AuthState.SetupRequired

    fun triggerBiometric() {
        if (!isSetup && isBiometricEnabled && context is FragmentActivity && BiometricAuthManager.canAuthenticate(context)) {
            val cryptoObject = authViewModel.getBiometricCryptoObject()
            BiometricAuthManager.promptBiometric(
                activity = context,
                cryptoObject = cryptoObject,
                onSuccess = { result -> authViewModel.unlockWithBiometrics(result) },
                onError = { /* fallback to PIN */ }
            )
        }
    }

    LaunchedEffect(authState) {
        if (authState == AuthState.Locked) {
            triggerBiometric()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF3A280B), // Ambient Gold Glow
                        Color(0xFF0F141C),
                        Color(0xFF07090E)  // Pitch Black Edge
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlossyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GlossyIconBox(
                    icon = if (isSetup) Icons.Default.Shield else Icons.Default.Lock,
                    size = 72.dp,
                    iconSize = 36.dp,
                    containerColor = MaterialTheme.colorScheme.primary,
                    iconColor = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                val isRooted = remember { SecurityManager.isDeviceRooted() }
                if (isRooted) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Root detected: System environment risk",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "Kunjika",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (isSetup) {
                        if (!isConfirmStep) "Create a Master PIN to protect your vault" else "Confirm your Master PIN"
                    } else {
                        "Enter Master PIN or use Biometrics"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
                )

                CustomTextField(
                    value = if (isSetup && isConfirmStep) confirmPin else pin,
                    onValueChange = { input ->
                        if (input.length <= 8 && input.all { it.isDigit() }) {
                            if (isSetup && isConfirmStep) confirmPin = input else pin = input
                            authViewModel.clearError()
                        }
                    },
                    label = if (isSetup && isConfirmStep) "Confirm PIN" else "Master PIN",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (isSetup) {
                                if (!isConfirmStep) {
                                    if (pin.length >= 4) isConfirmStep = true
                                } else {
                                    if (pin == confirmPin) authViewModel.setupMasterPin(pin)
                                }
                            } else {
                                authViewModel.unlockWithPin(pin)
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                GlossyButton(
                    onClick = {
                        keyboardController?.hide()
                        if (isSetup) {
                            if (!isConfirmStep) {
                                if (pin.length >= 4) isConfirmStep = true
                            } else {
                                if (pin == confirmPin) {
                                    authViewModel.setupMasterPin(pin)
                                }
                            }
                        } else {
                            authViewModel.unlockWithPin(pin)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isSetup) {
                            if (!isConfirmStep) "Next" else "Set Master PIN"
                        } else "Unlock Vault",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                if (!isSetup && isBiometricEnabled && context is FragmentActivity && BiometricAuthManager.canAuthenticate(context)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.20f),
                                        Color(0xFF1E293B),
                                        Color(0xFF0F172A)
                                    )
                                )
                            )
                            .glossyBorder(shape = RoundedCornerShape(14.dp), highlightColor = Color.White.copy(alpha = 0.5f))
                            .glossyTopShine(alpha = 0.3f)
                            .clickable { triggerBiometric() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Unlock with Biometrics",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
