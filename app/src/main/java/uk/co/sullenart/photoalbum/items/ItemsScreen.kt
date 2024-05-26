package uk.co.sullenart.photoalbum.items

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import uk.co.sullenart.photoalbum.R
import uk.co.sullenart.photoalbum.albums.Album
import uk.co.sullenart.photoalbum.albums.Album.SortOrder
import uk.co.sullenart.photoalbum.detail.DetailContent

@Composable
fun ItemsScreen(
    albumId: String,
    navController: NavController,
    viewModel: ItemsViewmodel = koinViewModel { parametersOf(albumId, navController) },
) {
    if (viewModel.isDetail) {
        BackHandler(onBack = viewModel::onDetailBack)
        DetailContent(
            pageCount = viewModel.itemCount,
            initialPage = viewModel.currentIndex,
            getItemFromIndex = viewModel::getItemFromIndex,
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            ItemsTopBar(
                album = viewModel.album,
                onBack = { navController.popBackStack() },
                sortOrder = viewModel.album.sortOrder,
                sortOrderClicked = viewModel::swapSortOrder,
            )
            ItemsContent(
                state = rememberLazyGridState(viewModel.firstIndex, viewModel.firstOffset),
                items = viewModel.items,
                onItemClicked = viewModel::onItemClicked,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemsTopBar(
    album: Album,
    onBack: () -> Unit,
    sortOrder: SortOrder,
    sortOrderClicked: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onBack,
            ) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, null)
            }

        },
        title = {
            Text("${album.title} (${album.itemCount})")
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        actions = {
            Text(
                text = when (sortOrder) {
                    SortOrder.NEWEST_FIRST -> stringResource(R.string.newest_first)
                    SortOrder.OLDEST_FIRST -> stringResource(R.string.oldest_first)
                    else -> stringResource(R.string.newest_first)
                },
            )
            IconButton(
                onClick = sortOrderClicked,
            ) {
                Icon(
                    imageVector = Icons.Filled.SwapVert,
                    contentDescription = null,
                )
            }
        }
    )
}


@Composable
private fun ItemsContent(
    state: LazyGridState,
    items: List<MediaItem>,
    onItemClicked: (MediaItem, Int, Int) -> Unit,
) {
    // TODO Check for recomposition

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.paddingM))
    ) {
        LazyVerticalGrid(
            state = state,
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.paddingS)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.paddingS)),
        ) {
            items(
                count = items.size,
                key = { items[it].id },
            ) {
                MediaItem(
                    item = items[it],
                    onClicked = { onItemClicked(items[it], state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset) },
                )
            }
        }
    }
}

@Composable
private fun MediaItem(
    item: MediaItem,
    onClicked: () -> Unit,
) {
    val context = LocalContext.current
    val request = remember {
        ImageRequest.Builder(context)
            .data(item)
            .setParameter(
                key = "type",
                value = "thumbnail",
            )
            .memoryCacheKey(item.id)
            .listener { _, result ->
                Timber.d("Fetch image from ${result.dataSource} for id ${item.id}")
            }
            .build()
    }

    AsyncImage(
        modifier = Modifier
            .clickable { onClicked() }
            .aspectRatio(1.0f),
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}