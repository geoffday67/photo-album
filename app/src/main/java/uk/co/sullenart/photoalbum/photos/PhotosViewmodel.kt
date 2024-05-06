package uk.co.sullenart.photoalbum.photos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import timber.log.Timber

class PhotosViewmodel(
    photosRepository: PhotosRepository,
    albumId: String,
) : ViewModel() {
    var isDetail by mutableStateOf(false)
    var currentIndex = 0
    var firstIndex: Int = 0
    var firstOffset = 0
    val photoFlow = photosRepository.getPhotoFlowForAlbum(albumId)

    private val photos = mutableListOf<Photo>()

    val photoCount: Int
        get() = photos.size

    init {
        viewModelScope.launch {
            photosRepository.getPhotoFlowForAlbum(albumId).collect {
                Timber.d("${it.size} photos received")
                photos.clear()
                photos.addAll(it)
            }
        }
    }

    fun onPhotoClicked(photo: Photo, index: Int, offset: Int) {
        firstIndex = index
        firstOffset = offset
        currentIndex = photos.indexOf(photo)
        isDetail = true
    }

    fun onDetailBack() {
        isDetail = false
    }

    fun getPhotoFromIndex(index: Int): Photo =
        photos[index]
}