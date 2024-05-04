package uk.co.sullenart.photoalbum

import android.app.admin.DevicePolicyManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import timber.log.Timber
import uk.co.sullenart.photoalbum.albums.AlbumsScreen
import uk.co.sullenart.photoalbum.detail.DetailScreen
import uk.co.sullenart.photoalbum.photos.PhotosScreen
import uk.co.sullenart.photoalbum.ui.theme.PhotoAlbumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        Timber.i("Screen size $width x $height")

        with(WindowCompat.getInsetsController(window, window.decorView)) {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            val navController = rememberNavController()

            PhotoAlbumTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    /*Button(onClick = {
                        try {
                            val adminName = ComponentName(this@MainActivity, DeviceAdmin::class.java)
                            val dpm = getSystemService(DevicePolicyManager::class.java)
                            dpm.setLockTaskPackages(adminName, arrayOf(packageName))
                            Log.d("Photo", "Lock task mode enabled")
                        } catch (e: Exception) {
                            Log.e("Photo", "Error enabling lock task mode", e)
                        }
                    }) {
                        Text("Lock task mode")
                    }*/
                    NavHost(
                        navController = navController,
                        startDestination = "albums",
                    ) {
                        composable("albums") {
                            AlbumsScreen(
                                navController = navController,
                            )
                        }
                        composable("photos/{albumId}") {
                            val albumId = it.arguments?.getString("albumId") ?: ""
                            PhotosScreen(
                                albumId = albumId,
                                navController = navController,
                            )
                        }
                        composable("detail/{photoId}") {
                            val photoId = it.arguments?.getString("photoId") ?: ""
                            DetailScreen(photoId)
                        }
                    }
                }
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
