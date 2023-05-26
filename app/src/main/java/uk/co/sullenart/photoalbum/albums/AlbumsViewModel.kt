package uk.co.sullenart.photoalbum.albums

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sullenart.photoalbum.Album
import uk.co.sullenart.photoalbum.service.GooglePhotos

class AlbumsViewModel(
    private val googlePhotos: GooglePhotos,
    ): ViewModel() {
    val albums = mutableStateListOf<Album>()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            albums.clear()
            albums.addAll(googlePhotos.getSharedAlbums())
        }
    }
}
