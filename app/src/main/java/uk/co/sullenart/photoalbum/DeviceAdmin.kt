package uk.co.sullenart.photoalbum

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class DeviceAdmin: DeviceAdminReceiver() {
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        Timber.d("Profile provisioned")
    }

    override fun onEnabled(context: Context, intent: Intent) {
        Timber.d("Enabled")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Timber.d("Disabled")
    }
}
