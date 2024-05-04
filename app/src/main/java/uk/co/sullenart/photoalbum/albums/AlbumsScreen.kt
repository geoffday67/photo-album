package uk.co.sullenart.photoalbum.albums

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import uk.co.sullenart.photoalbum.R
import uk.co.sullenart.photoalbum.config.ConfigPanel

@Composable
fun AlbumsScreen(
    navController: NavController,
    viewModel: AlbumsViewmodel = koinViewModel { parametersOf(navController) },
) {
    Column(
        modifier = Modifier
            .padding(dimensionResource(R.dimen.paddingM))
    ) {
        ConfigPanel(
            modifier = Modifier
                .padding(bottom = dimensionResource(R.dimen.paddingM))
        )
        Content(
            albums = viewModel.albumFlow.collectAsStateWithLifecycle(initialValue = emptyList()).value,
            onAlbumClicked = viewModel::onAlbumClicked,
        )
    }
}

@Composable
private fun Content(
    albums: List<Album>,
    onAlbumClicked: (Album) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.paddingM)),
    ) {
        items(albums) {
            AlbumItem(
                modifier = Modifier
                    .clickable { onAlbumClicked(it) },
                album = it,
            )
        }
    }
}

@Composable
private fun AlbumItem(
    modifier: Modifier = Modifier,
    album: Album,
) {
    Card() {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = modifier.padding(start = dimensionResource(R.dimen.paddingM)),
                text = album.title,
            )
            Text(
                modifier = modifier.padding(end = dimensionResource(R.dimen.paddingM)),
                text = album.itemCount.toString(),
            )
        }
    }
}