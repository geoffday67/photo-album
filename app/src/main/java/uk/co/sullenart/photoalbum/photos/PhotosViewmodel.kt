package uk.co.sullenart.photoalbum.photos

import androidx.lifecycle.ViewModel

class PhotosViewmodel(
    photosRepository: PhotosRepository,
) : ViewModel() {
    val photoFlow = photosRepository.photoFlow
}