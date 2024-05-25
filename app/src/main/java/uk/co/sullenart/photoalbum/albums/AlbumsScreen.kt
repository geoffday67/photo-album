package uk.co.sullenart.photoalbum.albums

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import uk.co.sullenart.photoalbum.R
import uk.co.sullenart.photoalbum.items.MediaItem
import uk.co.sullenart.photoalbum.settings.SettingsScreen

@Composable
fun AlbumsScreen(
    navController: NavController,
    viewModel: AlbumsViewmodel = koinViewModel { parametersOf(navController) },
) {
    Scaffold(
        topBar = { AlbumsTopBar(viewModel::showSettings) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(dimensionResource(R.dimen.paddingM))
        ) {
            Content(
                albums = viewModel.albumFlow.collectAsStateWithLifecycle(emptyList()).value,
                onAlbumClicked = viewModel::onAlbumClicked,
            )
            if (viewModel.settingsVisible) {
                SettingsScreen(
                    onDismiss = viewModel::hideSettings,
                )
            }
        }
    }
}

@Composable
private fun Content(
    albums: List<Album>,
    onAlbumClicked: (Album) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
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
private fun TestItem(
    item: MediaItem,
) {
    AsyncImage(
        model = item,
        contentDescription = null,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumsTopBar(
    onSettings: () -> Unit,
) {
    TopAppBar(
        title = { Text("Photo Album") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        actions = {
            IconButton(
                onClick = onSettings,
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                )
            }
        }
    )
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