package com.kunjika.app.core.update

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.kunjika.app.core.util.KLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Checking : UpdateStatus
    data class UpdateAvailable(
        val appUpdateInfo: AppUpdateInfo,
        val availableVersionCode: Int,
        val isFlexibleAllowed: Boolean,
        val isImmediateAllowed: Boolean
    ) : UpdateStatus
    data class UpToDate(val currentVersionCode: Int) : UpdateStatus
    data class Error(val message: String) : UpdateStatus
}

class AppUpdateChecker(private val context: Context) {

    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(context.applicationContext)
    }

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    /**
     * Queries Google Play Services asynchronously to check if a new app version is available.
     */
    suspend fun checkForUpdates(): UpdateStatus = withContext(Dispatchers.IO) {
        _updateStatus.value = UpdateStatus.Checking

        try {
            val appUpdateInfo = suspendCheckAppUpdateInfo()
            val availability = appUpdateInfo.updateAvailability()

            val currentVersionCode = try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                @Suppress("DEPRECATION")
                pInfo.versionCode
            } catch (_: Exception) {
                0
            }

            if (availability == UpdateAvailability.UPDATE_AVAILABLE || availability == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                val flexibleAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                val immediateAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                val availableCode = appUpdateInfo.availableVersionCode()

                val result = UpdateStatus.UpdateAvailable(
                    appUpdateInfo = appUpdateInfo,
                    availableVersionCode = availableCode,
                    isFlexibleAllowed = flexibleAllowed,
                    isImmediateAllowed = immediateAllowed
                )
                _updateStatus.value = result
                KLog.d("[AppUpdateChecker] Update available! New versionCode=$availableCode (current=$currentVersionCode)")
                result
            } else {
                val result = UpdateStatus.UpToDate(currentVersionCode)
                _updateStatus.value = result
                KLog.d("[AppUpdateChecker] App is up to date (current versionCode=$currentVersionCode)")
                result
            }
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Failed to query Google Play for updates."
            val result = UpdateStatus.Error(errorMsg)
            _updateStatus.value = result
            KLog.e("[AppUpdateChecker] Error checking for updates: $errorMsg", e)
            result
        }
    }

    private suspend fun suspendCheckAppUpdateInfo(): AppUpdateInfo =
        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            appUpdateManager.appUpdateInfo
                .addOnSuccessListener { info ->
                    if (continuation.isActive) continuation.resume(info)
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resumeWith(Result.failure(exception))
                }
        }

    /**
     * Launches the native Google Play In-App Update flow.
     * Fallback opens the Google Play Store app listing page.
     */
    fun startUpdate(
        activity: Activity,
        appUpdateInfo: AppUpdateInfo,
        updateType: Int = AppUpdateType.FLEXIBLE
    ): Boolean {
        return try {
            val options = AppUpdateOptions.newBuilder(updateType).build()
            appUpdateManager.startUpdateFlow(appUpdateInfo, activity, options)
            true
        } catch (e: Exception) {
            KLog.e("[AppUpdateChecker] In-App update flow failed to start. Opening Play Store page fallback.", e)
            openPlayStoreListing(activity)
            false
        }
    }

    /**
     * Directly opens the Google Play Store page for Kunjika.
     */
    fun openPlayStoreListing(context: Context) {
        val packageName = context.packageName
        val playStoreUri = Uri.parse("market://details?id=$packageName")
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")

        val intent = Intent(Intent.ACTION_VIEW, playStoreUri).apply {
            setPackage("com.android.vending")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(webIntent)
            } catch (t: Throwable) {
                KLog.e("[AppUpdateChecker] Failed to open Play Store listing", t)
            }
        }
    }
}
