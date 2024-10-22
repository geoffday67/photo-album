package uk.co.sullenart.photoalbum

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import timber.log.Timber
import uk.co.sullenart.photoalbum.main.MainScreen
import uk.co.sullenart.photoalbum.ui.theme.PhotoAlbumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PhotoAlbumTheme {
                MainScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val dpm = getSystemService(DevicePolicyManager::class.java)
        if (dpm.isLockTaskPermitted(packageName)) {
            Timber.d("Starting in lock task mode")
            startLockTask()
        } else {
            Timber.d("Lock task mode not permitted")
        }
    }
}
