package uk.co.sullenart.photoalbum.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sullenart.photoalbum.albums.Album
import uk.co.sullenart.photoalbum.albums.AlbumsRepository

class ItemsViewmodel(
    itemsRepository: MediaItemsRepository,
    albumsRepository: AlbumsRepository,
    albumId: String,
) : ViewModel() {
    var isDetail by mutableStateOf(false)
    var currentIndex = 0
    var firstIndex: Int = 0
    var firstOffset = 0
    val itemFlow = itemsRepository.getItemFlowForAlbum(albumId)
    val album = albumsRepository.getAlbum(albumId) ?: Album.EMPTY

    private val items = mutableListOf<MediaItem>()

    val itemCount: Int
        get() = items.size

    init {
        viewModelScope.launch {
            itemsRepository.getItemFlowForAlbum(albumId).collect {
                items.clear()
                items.addAll(it)
            }
        }
    }

    fun onItemClicked(item: MediaItem, index: Int, offset: Int) {
        firstIndex = index
        firstOffset = offset
        currentIndex = items.indexOf(item)
        isDetail = true
    }

    fun onDetailBack() {
        isDetail = false
    }

    fun getItemFromIndex(index: Int): MediaItem =
        items[index]
}