package uk.co.sullenart.photoalbum.albums

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.emptyFlow
import timber.log.Timber

class AlbumsViewmodel(
    albumsRepository: AlbumsRepository,
    private val navController: NavController,
) : ViewModel() {
    val albumFlow = albumsRepository.albumFlow

    fun onAlbumClicked(album: Album) {
        Timber.d("Album ${album.title} clicked")
        navController.navigate("photos/${album.id}")
    }
}
