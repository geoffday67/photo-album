package uk.co.sullenart.photoalbum.albums

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.map
import timber.log.Timber

class AlbumsViewmodel(
    private val albumsRepository: AlbumsRepository,
    private val navController: NavController,
) : ViewModel() {
    val albumFlow = albumsRepository.albumFlow.map {
        it.sortedBy { it.title }
    }

    var settingsVisible by mutableStateOf(false)

    fun onAlbumClicked(album: Album) {
        Timber.d("Album ${album.title} clicked")
        navController.navigate("photos/${album.id}")
    }

    fun showSettings() {
        settingsVisible = true
    }

    fun hideSettings() {
        settingsVisible = false
    }
}
