package uk.co.sullenart.photoalbum.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import uk.co.sullenart.photoalbum.background.BackgroundFetcher

class PhotosViewmodel(
    private val backgroundFetcher: BackgroundFetcher,
    photosRepository: PhotosRepository,
    albumId: String,
) : ViewModel() {
    val photoFlow = photosRepository.getPhotoFlowForAlbum(albumId)

    fun refresh() {
        viewModelScope.launch {
            backgroundFetcher.refresh()
        }
    }
}