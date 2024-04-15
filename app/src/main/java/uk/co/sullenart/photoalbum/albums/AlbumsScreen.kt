package uk.co.sullenart.photoalbum

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import uk.co.sullenart.photoalbum.albums.AlbumsViewModel

@Composable
fun AlbumsScreen(
    viewModel: AlbumsViewModel = koinViewModel()
) {
    Content(
        onTestClicked = viewModel::test,
        albums = viewModel.albumFlow.collectAsStateWithLifecycle(initialValue = emptyList<Album>()).value,
        )
}

@Composable
private fun Content(
    onTestClicked: () -> Unit,
    albums: List<Album>,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
    ) {
        item {
            Button(onClick = onTestClicked) {
                Text("Test")
            }
        }
        items(albums) {
            Text(it.title)
        }
    }

}

@Preview
@Composable
fun Preview() {
    Content(
        onTestClicked = { },
        albums = listOf(Album("First"), Album("Second"), Album("Third")),
    )
}
