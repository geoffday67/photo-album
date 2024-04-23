package uk.co.sullenart.photoalbum.albums

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.emptyFlow
import timber.log.Timber

class AlbumsViewmodel(
    albumsRepository: AlbumsRepository,
) : ViewModel() {
    val albumFlow = albumsRepository.albumFlow

    fun onAlbumClicked(album: Album) {
        Timber.d("${album.title}, ${album.id}")
    }
}
