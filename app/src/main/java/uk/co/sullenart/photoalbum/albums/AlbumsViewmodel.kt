package uk.co.sullenart.photoalbum.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.sullenart.photoalbum.background.BackgroundFetcher

class AlbumsViewmodel(
    private val backgroundFetcher: BackgroundFetcher,
    albumsRepository: AlbumsRepository,
    private val navController: NavController,
) : ViewModel() {
    val albumFlow = albumsRepository.albumFlow

    fun onAlbumClicked(album: Album) {
        Timber.d("Album ${album.title} clicked")
        navController.navigate("photos/${album.id}")
    }

    fun refresh() {
        viewModelScope.launch {
            backgroundFetcher.refresh()
        }
    }
}
