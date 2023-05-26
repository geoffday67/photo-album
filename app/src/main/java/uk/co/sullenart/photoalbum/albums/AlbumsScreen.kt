package uk.co.sullenart.photoalbum

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel
import uk.co.sullenart.photoalbum.albums.AlbumsViewModel

@Composable
fun AlbumsScreen(
    viewModel: AlbumsViewModel = koinViewModel()
) {
    LazyColumn(
        Modifier.fillMaxSize(),
    ) {
        items(viewModel.albums) {
            Text(it.title)
        }
    }
}
