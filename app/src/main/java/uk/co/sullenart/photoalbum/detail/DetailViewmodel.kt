package uk.co.sullenart.photoalbum.detail

import androidx.lifecycle.ViewModel
import uk.co.sullenart.photoalbum.photos.Photo
import uk.co.sullenart.photoalbum.photos.PhotosRepository

class DetailViewmodel(
    photosRepository: PhotosRepository,
    photoId: String,
    ): ViewModel() {
        val photo: Photo? = photosRepository.getPhotoFromId(photoId)
}