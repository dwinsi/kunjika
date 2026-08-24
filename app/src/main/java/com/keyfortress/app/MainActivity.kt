package com.keyfortress.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.keyfortress.app.data.local.AppDatabase
import com.keyfortress.app.data.preferences.UserPreferences
import com.keyfortress.app.data.repository.PasswordRepository
import com.keyfortress.app.ui.MainNavigation
import com.keyfortress.app.ui.theme.KeyFortressTheme
import com.keyfortress.app.ui.viewmodel.AuthViewModel
import com.keyfortress.app.ui.viewmodel.GeneratorViewModel
import com.keyfortress.app.ui.viewmodel.SettingsViewModel
import com.keyfortress.app.ui.viewmodel.VaultViewModel

class MainActivity : FragmentActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var generatorViewModel: GeneratorViewModel
    private lateinit var vaultViewModel: VaultViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // HARDEN SCREEN SECURITY: Prevent screenshots, screen recording, and hide app preview in recents
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PasswordRepository(database.passwordDao())
        val userPreferences = UserPreferences(applicationContext)

        authViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(userPreferences) as T
            }
        })[AuthViewModel::class.java]

        generatorViewModel = ViewModelProvider(this)[GeneratorViewModel::class.java]

        vaultViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VaultViewModel(repository) as T
            }
        })[VaultViewModel::class.java]

        settingsViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(userPreferences, repository) as T
            }
        })[SettingsViewModel::class.java]

        // Auto-lock lifecycle observer
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                authViewModel.onAppBackgrounded()
            }

            override fun onStart(owner: LifecycleOwner) {
                authViewModel.onAppForegrounded()
            }
        })

        setContent {
            val useDynamicColor by settingsViewModel.useDynamicColor.collectAsState()
            val useDarkTheme by settingsViewModel.useDarkTheme.collectAsState()

            KeyFortressTheme(
                darkTheme = useDarkTheme,
                dynamicColor = useDynamicColor
            ) {
                MainNavigation(
                    authViewModel = authViewModel,
                    generatorViewModel = generatorViewModel,
                    vaultViewModel = vaultViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
