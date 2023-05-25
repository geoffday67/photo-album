package uk.co.sullenart.photoalbum

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DeviceAdmin: DeviceAdminReceiver() {
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        Log.d("Admin", "Profile provisioned")
    }

    override fun onEnabled(context: Context, intent: Intent) {
        Log.d("Admin", "Enabled")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Log.d("Admin", "Disabled")
    }
}
