package uk.co.sullenart.photoalbum.main

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import uk.co.sullenart.photoalbum.albums.AlbumsScreen
import uk.co.sullenart.photoalbum.detail.DetailScreen
import uk.co.sullenart.photoalbum.photos.PhotosScreen
import uk.co.sullenart.photoalbum.settings.SettingsScreen

@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainViewmodel = koinViewModel { parametersOf(navController) },
) {
    var topBarVisible by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            PhotosTopBar(
                onSettings = viewModel::onSettingsClicked,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it)
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
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
            ) {
                composable("albums") {
                    topBarVisible = true
                    AlbumsScreen(
                        navController = navController,
                    )
                }
                composable("photos/{albumId}") {
                    topBarVisible = true
                    val albumId = it.arguments?.getString("albumId") ?: ""
                    PhotosScreen(
                        albumId = albumId,
                        navController = navController,
                    )
                }
                composable("detail/{photoId}") {
                    val photoId = it.arguments?.getString("photoId") ?: ""
                    DetailScreen(photoId)
                    topBarVisible = false
                }
                composable("settings") {
                    topBarVisible = true
                    SettingsScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotosTopBar(
    onSettings: () -> Unit,
) {
    var infoChecked by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Photo Album") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        actions = {
            IconToggleButton(
                checked = infoChecked,
                onCheckedChange = { infoChecked = !infoChecked },
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                )
            }
            IconButton(
                onClick = onSettings,
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                )
            }
        }
    )
}