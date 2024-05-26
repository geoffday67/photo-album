package uk.co.sullenart.photoalbum.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sullenart.photoalbum.albums.Album
import uk.co.sullenart.photoalbum.albums.Album.SortOrder
import uk.co.sullenart.photoalbum.albums.AlbumsRepository

class ItemsViewmodel(
    itemsRepository: MediaItemsRepository,
    private val albumsRepository: AlbumsRepository,
    private val albumId: String,
) : ViewModel() {
    var isDetail by mutableStateOf(false)
    var currentIndex = 0
    var firstIndex: Int = 0
    var firstOffset = 0

    var album by mutableStateOf(albumsRepository.getAlbum(albumId) ?: Album.EMPTY)
    var items by mutableStateOf<List<MediaItem>>(emptyList())

    val itemCount: Int
        get() = items.size

    init {
        viewModelScope.launch {
            itemsRepository.getItemFlowForAlbum(albumId).collect {
                items = it
                sortItems()
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

    fun swapSortOrder() {
        val newSortOrder = when (album.sortOrder) {
            SortOrder.NEWEST_FIRST -> SortOrder.OLDEST_FIRST
            SortOrder.OLDEST_FIRST -> SortOrder.NEWEST_FIRST
            else -> SortOrder.OLDEST_FIRST
        }
        viewModelScope.launch {
            album = albumsRepository.setSortOrder(album, newSortOrder)
            sortItems()
        }
    }

    private fun sortItems() {
        items = when (album.sortOrder) {
            SortOrder.NEWEST_FIRST -> items.sortedByDescending { it.creationTime }
            SortOrder.OLDEST_FIRST -> items.sortedBy { it.creationTime }
            else -> items
        }
    }
}