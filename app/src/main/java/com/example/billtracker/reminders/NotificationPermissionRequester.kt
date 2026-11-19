package com.example.billtracker.reminders

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Asks for the POST_NOTIFICATIONS permission (Android 13+).
 * Create it as a property of MainActivity (it must be registered before the activity starts),
 * then call requestIfNeeded() from onCreate().
 */
class NotificationPermissionRequester(private val activity: ComponentActivity) {

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                activity,
                "Notifications are off, so bill reminders won't show. You can turn them on in system settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun requestIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationHelper.hasPermission(activity)
        ) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
