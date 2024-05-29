package uk.co.sullenart.photoalbum.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.sullenart.photoalbum.albums.Album
import uk.co.sullenart.photoalbum.albums.Album.SortOrder
import uk.co.sullenart.photoalbum.albums.AlbumsRepository

class ItemsViewmodel(
    private val itemsRepository: MediaItemsRepository,
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
                Timber.d("${it.size} new media items from flow")
                items = it.sortItems()
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

    fun onCurrentPage(current: Int) {
        currentIndex = current
    }

    fun getItemFromIndex(index: Int): MediaItem =
        items[index]

    fun swapSortOrder() {
        val newSortOrder = when (album.sortOrder) {
            SortOrder.NEWEST_FIRST -> SortOrder.OLDEST_FIRST
            SortOrder.OLDEST_FIRST -> SortOrder.NEWEST_FIRST
        }
        viewModelScope.launch {
            album = albumsRepository.setSortOrder(album, newSortOrder)
            items = items.sortItems()
        }
    }

    private fun List<MediaItem>.sortItems(): List<MediaItem> =
        when (album.sortOrder) {
            SortOrder.NEWEST_FIRST -> sortedByDescending { it.creationTime }
            SortOrder.OLDEST_FIRST -> sortedBy { it.creationTime }
        }

    fun setItemRotation(
        item: MediaItem,
        newRotation: Rotation,
    ) {
        if (item !is PhotoItem) {
            Timber.w("Only photos can be rotated")
            return
        }

        viewModelScope.launch {
            itemsRepository.setRotation(item, newRotation)
        }
    }
}