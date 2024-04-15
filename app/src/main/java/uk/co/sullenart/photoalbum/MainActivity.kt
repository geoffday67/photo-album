package uk.co.sullenart.photoalbum

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.android.ext.android.get
import timber.log.Timber
import uk.co.sullenart.photoalbum.config.Config
import uk.co.sullenart.photoalbum.sign_in.SignInScreen
import uk.co.sullenart.photoalbum.ui.theme.PhotoAlbumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("Album = ${get<Config>().album}")

        setContent {
            val navController = rememberNavController()

            PhotoAlbumTheme {
                Column(
                    modifier = Modifier.padding(12.dp),
                ) {
                    Button(onClick = {
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
                    }
                    NavHost(
                        navController = navController,
                        startDestination = "albums",
                    ) {
                        composable("sign-in") {
                            SignInScreen()
                        }
                        composable("albums") {
                            AlbumsScreen()
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
