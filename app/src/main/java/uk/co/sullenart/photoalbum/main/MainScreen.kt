package uk.co.sullenart.photoalbum.main

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import uk.co.sullenart.photoalbum.albums.AlbumsScreen
import uk.co.sullenart.photoalbum.photos.PhotosScreen
import uk.co.sullenart.photoalbum.settings.SettingsScreen

@Composable
fun MainScreen(
    navController: NavHostController,
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
            PhotosScreen(
                albumId = albumId,
                navController = navController,
            )
        }
    }
}