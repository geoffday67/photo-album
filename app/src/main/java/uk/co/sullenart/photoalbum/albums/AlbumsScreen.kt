package uk.co.sullenart.photoalbum

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import uk.co.sullenart.photoalbum.albums.Album
import uk.co.sullenart.photoalbum.albums.AlbumsViewmodel

@Composable
fun AlbumsScreen(
    viewModel: AlbumsViewmodel = koinViewModel()
) {
    Content(
        albums = viewModel.albumFlow.collectAsStateWithLifecycle(initialValue = emptyList()).value,
        onAlbumClicked = viewModel::onAlbumClicked,
    )
}

@Composable
private fun Content(
    albums: List<Album>,
    onAlbumClicked: (Album) -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
    ) {
        items(albums) {
            AlbumItem(
                album = it,
                onClicked = { onAlbumClicked(it) },
            )
        }
    }

}

@Composable
private fun AlbumItem(
    album: Album,
    onClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClicked() },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(album.title)
        Text(album.itemCount.toString())
    }
}