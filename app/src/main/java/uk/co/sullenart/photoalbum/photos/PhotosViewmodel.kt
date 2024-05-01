package uk.co.sullenart.photoalbum.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.sullenart.photoalbum.background.BackgroundFetcher

class PhotosViewmodel(
    private val backgroundFetcher: BackgroundFetcher,
    photosRepository: PhotosRepository,
    albumId: String,
    private val navController: NavController,
    ) : ViewModel() {
    val photoFlow = photosRepository.getPhotoFlowForAlbum(albumId)

    fun refresh() {
        viewModelScope.launch {
            backgroundFetcher.refresh()
        }
    }

    fun onPhotoClicked(photo: Photo) {
        Timber.d("Photo ${photo.id} clicked")
        navController.navigate("detail/${photo.id}")
    }
}