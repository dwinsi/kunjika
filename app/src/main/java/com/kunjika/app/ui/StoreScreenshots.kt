package com.kunjika.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kunjika.app.ui.screens.onboarding.OnboardingScreen
import com.kunjika.app.ui.theme.KunjikaTheme
import com.kunjika.app.ui.theme.StrengthVeryStrong

@Preview(device = "spec:width=1080px,height=2340px,dpi=440", showBackground = true)
@Composable
fun Screenshot_1_Onboarding() {
    KunjikaTheme(darkTheme = true) {
        OnboardingScreen(onFinished = {})
    }
}

@Preview(device = "spec:width=1080px,height=2340px,dpi=440", showBackground = true)
@Composable
fun Screenshot_2_Auth() {
    KunjikaTheme(darkTheme = true) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Kunjika", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Enter Master PIN or use Biometrics", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp, bottom = 24.dp))
                    
                    OutlinedTextField(
                        value = "••••",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Master PIN") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
                        Text("Unlock Vault", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("  Unlock with Biometrics")
                    }
                }
            }
        }
    }
}

@Preview(device = "spec:width=1080px,height=2340px,dpi=440", showBackground = true)
@Composable
fun Screenshot_3_Generator() {
    KunjikaTheme(darkTheme = true) {
        Scaffold(
            bottomBar = { MockBottomBar(0) }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text("Password Generator", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)).padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("kunjika-secure-2026", style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Strength
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = StrengthVeryStrong, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Very Strong (128 bits)", color = StrengthVeryStrong, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Copy") }
                            Button(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("Save") }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Slider(value = 0.6f, onValueChange = {})
                Text("Length: 20 characters", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(device = "spec:width=1080px,height=2340px,dpi=440", showBackground = true)
@Composable
fun Screenshot_4_Vault() {
    KunjikaTheme(darkTheme = true) {
        Scaffold(
            bottomBar = { MockBottomBar(1) },
            floatingActionButton = {
                FloatingActionButton(onClick = {}, containerColor = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                OutlinedTextField(value = "", onValueChange = {}, placeholder = { Text("Search vault...") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(20.dp))
                MockVaultItem("Google Account", "dwinsi@gmail.com", "Social")
                Spacer(modifier = Modifier.height(12.dp))
                MockVaultItem("GitHub", "dwinsi", "Work")
                Spacer(modifier = Modifier.height(12.dp))
                MockVaultItem("Binance", "crypto_user", "Finance")
            }
        }
    }
}

@Composable
fun MockVaultItem(title: String, user: String, category: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Text(title.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(user, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun MockBottomBar(selectedIndex: Int) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        val items = listOf("Generator" to Icons.Default.Autorenew, "Vault" to Icons.Default.Lock, "Security" to Icons.Default.HealthAndSafety, "Settings" to Icons.Default.Settings)
        items.forEachIndexed { index, pair ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = {},
                icon = { Icon(pair.second, contentDescription = null) },
                label = { Text(pair.first) }
            )
        }
    }
}
