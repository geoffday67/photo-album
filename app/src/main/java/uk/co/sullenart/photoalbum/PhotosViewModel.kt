package uk.co.sullenart.photoalbum

import androidx.lifecycle.ViewModel
import uk.co.sullenart.photoalbum.service.GooglePhotos

class PhotosViewModel(
    private val googlePhotos: GooglePhotos,
) : ViewModel() {
}