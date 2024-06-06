package uk.co.sullenart.photoalbum

import android.app.admin.DevicePolicyManager
import android.os.Bundle
import android.util.Log
import android.view.View
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

        /*WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val dpm = getSystemService(DevicePolicyManager::class.java)
        if (dpm.isLockTaskPermitted(packageName)) {
            Log.d("Photo", "Starting in lock task mode")
            startLockTask()
        }*/
    }
}
