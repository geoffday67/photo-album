package uk.co.sullenart.photoalbum

import android.app.admin.DevicePolicyManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import timber.log.Timber
import uk.co.sullenart.photoalbum.main.MainScreen
import uk.co.sullenart.photoalbum.ui.theme.PhotoAlbumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        Timber.i("Screen size $width x $height")

        setContent {
            val navController = rememberNavController()

            PhotoAlbumTheme {
                MainScreen(navController)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val dpm = getSystemService(DevicePolicyManager::class.java)
        if (dpm.isLockTaskPermitted(packageName)) {
            Log.d("Photo", "Starting in lock task mode")
            startLockTask()
        }
    }
}
