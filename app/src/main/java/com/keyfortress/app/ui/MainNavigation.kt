package com.keyfortress.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.keyfortress.app.ui.screens.auth.AuthScreen
import com.keyfortress.app.ui.screens.generator.GeneratorScreen
import com.keyfortress.app.ui.screens.health.SecurityAuditScreen
import com.keyfortress.app.ui.screens.settings.SettingsScreen
import com.keyfortress.app.ui.screens.vault.VaultScreen
import com.keyfortress.app.ui.viewmodel.AuthState
import com.keyfortress.app.ui.viewmodel.AuthViewModel
import com.keyfortress.app.ui.viewmodel.GeneratorViewModel
import com.keyfortress.app.ui.viewmodel.SettingsViewModel
import com.keyfortress.app.ui.viewmodel.VaultViewModel

enum class NavigationTab(val title: String, val icon: ImageVector) {
    GENERATOR("Generator", Icons.Default.Autorenew),
    VAULT("Vault", Icons.Default.Lock),
    AUDIT("Security", Icons.Default.HealthAndSafety),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun MainNavigation(
    authViewModel: AuthViewModel,
    generatorViewModel: GeneratorViewModel,
    vaultViewModel: VaultViewModel,
    settingsViewModel: SettingsViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    var selectedTab by remember { mutableStateOf(NavigationTab.GENERATOR) }

    if (authState != AuthState.Authenticated) {
        AuthScreen(authViewModel = authViewModel)
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    NavigationTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    NavigationTab.GENERATOR -> GeneratorScreen(
                        generatorViewModel = generatorViewModel,
                        vaultViewModel = vaultViewModel
                    )
                    NavigationTab.VAULT -> VaultScreen(vaultViewModel = vaultViewModel)
                    NavigationTab.AUDIT -> SecurityAuditScreen(vaultViewModel = vaultViewModel)
                    NavigationTab.SETTINGS -> SettingsScreen(
                        settingsViewModel = settingsViewModel,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
