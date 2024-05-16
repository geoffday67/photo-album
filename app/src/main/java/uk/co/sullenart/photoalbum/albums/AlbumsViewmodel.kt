package uk.co.sullenart.photoalbum.albums

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.sullenart.photoalbum.background.BackgroundFetcher
import uk.co.sullenart.photoalbum.items.MediaItemsRepository

class AlbumsViewmodel(
    albumsRepository: AlbumsRepository,
    private val navController: NavController,
) : ViewModel() {
    val albumFlow = albumsRepository.albumFlow
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
