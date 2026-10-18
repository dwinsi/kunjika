package com.kunjika.app

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kunjika.app.data.local.AppDatabase
import com.kunjika.app.data.preferences.UserPreferences
import com.kunjika.app.data.repository.HistoryRepository
import com.kunjika.app.data.repository.PasswordRepository
import com.kunjika.app.ui.MainNavigation
import com.kunjika.app.ui.NavigationTab
import com.kunjika.app.ui.theme.KunjikaTheme
import com.kunjika.app.ui.viewmodel.AuthViewModel
import com.kunjika.app.ui.viewmodel.GeneratorViewModel
import com.kunjika.app.ui.viewmodel.SettingsViewModel
import com.kunjika.app.ui.viewmodel.VaultViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : FragmentActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var generatorViewModel: GeneratorViewModel
    private lateinit var vaultViewModel: VaultViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    private var appReady by mutableStateOf(false)
    private var startupFailed by mutableStateOf(false)
    private var initializationJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.decorView.filterTouchesWhenObscured = true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            window.decorView.setAccessibilityDataSensitive(View.ACCESSIBILITY_DATA_SENSITIVE_YES)
        }

        val initialTab = when (intent?.data?.host) {
            "generator" -> NavigationTab.GENERATOR
            "vault" -> NavigationTab.VAULT
            else -> NavigationTab.GENERATOR
        }

        setContent {
            if (appReady) {
                val useDynamicColor by settingsViewModel.useDynamicColor.collectAsState()
                val useDarkTheme by settingsViewModel.useDarkTheme.collectAsState()
                KunjikaTheme(darkTheme = useDarkTheme, dynamicColor = useDynamicColor) {
                    MainNavigation(
                        authViewModel = authViewModel,
                        generatorViewModel = generatorViewModel,
                        vaultViewModel = vaultViewModel,
                        settingsViewModel = settingsViewModel,
                        initialTab = initialTab
                    )
                }
            } else {
                KunjikaTheme(darkTheme = true, dynamicColor = false) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (startupFailed) {
                            Text("Kunjika couldn't open the local vault.", style = MaterialTheme.typography.titleMedium)
                            Text("Please retry. If this keeps happening, preserve your backups and contact support.")
                            Button(onClick = { initializeApp() }) { Text("Retry") }
                        } else {
                            CircularProgressIndicator()
                            Text("Loading your encrypted vault…", modifier = Modifier.padding(top = 16.dp))
                        }
                    }
                }
            }
        }

        initializeApp()
    }

    private fun initializeApp() {
        if (initializationJob?.isActive == true) return
        startupFailed = false
        initializationJob = lifecycleScope.launch {
            try {
                val dependencies = withContext(Dispatchers.IO) {
                    val database = AppDatabase.getDatabase(applicationContext)
                    val repository = PasswordRepository(database.passwordDao(), database.blockDao())
                    AppDependencies(
                        userPreferences = UserPreferences(applicationContext),
                        passwordRepository = repository,
                        historyRepository = HistoryRepository(database.historyDao())
                    )
                }

                authViewModel = ViewModelProvider(this@MainActivity, makeViewModelFactory {
                    AuthViewModel(dependencies.userPreferences, dependencies.passwordRepository)
                })[AuthViewModel::class.java]
                generatorViewModel = ViewModelProvider(this@MainActivity, makeViewModelFactory {
                    GeneratorViewModel(dependencies.historyRepository)
                })[GeneratorViewModel::class.java]
                vaultViewModel = ViewModelProvider(this@MainActivity, makeViewModelFactory {
                    VaultViewModel(dependencies.passwordRepository)
                })[VaultViewModel::class.java]
                settingsViewModel = ViewModelProvider(this@MainActivity, makeViewModelFactory {
                    SettingsViewModel(dependencies.userPreferences, dependencies.passwordRepository)
                })[SettingsViewModel::class.java]

                lifecycle.addObserver(object : DefaultLifecycleObserver {
                    override fun onStop(owner: LifecycleOwner) {
                        authViewModel.onAppBackgrounded()
                    }

                    override fun onStart(owner: LifecycleOwner) {
                        authViewModel.onAppForegrounded()
                    }
                })
                appReady = true
            } catch (_: Exception) {
                startupFailed = true
            }
        }
    }

    private fun <T : ViewModel> makeViewModelFactory(createViewModel: () -> T) =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = createViewModel() as VM
        }

    private data class AppDependencies(
        val userPreferences: UserPreferences,
        val passwordRepository: PasswordRepository,
        val historyRepository: HistoryRepository
    )
}
