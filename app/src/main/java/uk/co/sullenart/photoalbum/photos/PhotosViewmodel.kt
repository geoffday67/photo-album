package uk.co.sullenart.photoalbum.photos

import androidx.lifecycle.ViewModel

class PhotosViewmodel(
    photosRepository: PhotosRepository,
    albumId: String,
) : ViewModel() {
    val photoFlow = photosRepository.getPhotoFlowForAlbum(albumId)
}