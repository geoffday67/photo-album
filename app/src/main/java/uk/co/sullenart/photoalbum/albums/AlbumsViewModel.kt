package uk.co.sullenart.photoalbum.albums

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sullenart.photoalbum.Album
import uk.co.sullenart.photoalbum.service.GooglePhotos

class AlbumsViewModel(
    private val googlePhotos: GooglePhotos,
    private val albumsRepository: AlbumsRepository,
    ): ViewModel() {
    val albums = mutableStateListOf<Album>()

    val albumFlow = albumsRepository.albumFlow

    init {
        viewModelScope.launch {
            albumsRepository.albumFlow.collect {
                albums.clear()
                albums.addAll(it)
            }
        }
    }

    fun test() {
        viewModelScope.launch {
            albumsRepository.test("Hello")
        }
    }

    fun refresh() {
        viewModelScope.launch {
            albums.clear()
            albums.addAll(googlePhotos.getSharedAlbums())
        }
    }
}
