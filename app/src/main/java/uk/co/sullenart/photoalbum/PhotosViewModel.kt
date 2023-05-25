package uk.co.sullenart.photoalbum

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sullenart.photoalbum.service.Auth
import uk.co.sullenart.photoalbum.service.GooglePhotos

class PhotosViewModel : ViewModel() {
    val auth = Auth()
    val googlePhotos = GooglePhotos()
    val albums = mutableStateListOf<Album>()

    fun completeAuth(code: String) {
        viewModelScope.launch {
            auth.exchangeCode(code)
            albums.clear()
            albums.addAll(googlePhotos.getAlbums())
        }
    }

    fun fetchAlbums() {
        viewModelScope.launch {
            albums.clear()
            albums.addAll(googlePhotos.getAlbums())
        }
    }
}