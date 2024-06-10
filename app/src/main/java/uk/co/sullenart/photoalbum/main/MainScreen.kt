package uk.co.sullenart.photoalbum.main

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.co.sullenart.photoalbum.albums.AlbumsScreen
import uk.co.sullenart.photoalbum.items.ItemsScreen

@Composable
fun MainScreen() {
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
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "albums",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable("albums") {
            AlbumsScreen(
                navController = navController,
            )
        }
        composable("photos/{albumId}") {
            val albumId = it.arguments?.getString("albumId") ?: ""
            ItemsScreen(
                albumId = albumId,
                navController = navController,
            )
        }
    }
}