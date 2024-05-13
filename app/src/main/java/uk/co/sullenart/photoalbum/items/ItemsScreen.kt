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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import uk.co.sullenart.photoalbum.R
import uk.co.sullenart.photoalbum.albums.Album
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
            )
            ItemsContent(
                state = LazyGridState(viewModel.firstIndex, viewModel.firstOffset),
                items = viewModel.itemFlow.collectAsStateWithLifecycle(initialValue = emptyList()).value,
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
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onBack,
            ) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, null)
            }

        },
        title = { Text("${album.title} (${album.itemCount})") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
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
    val request = ImageRequest.Builder(LocalContext.current)
        .data(item.usableUrl)
        .diskCacheKey(item.id)
        .listener(resultListener)
        .build()
    AsyncImage(
        modifier = Modifier
            .clickable { onClicked() }
            .aspectRatio(1.0f),
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}

private val resultListener = object : ImageRequest.Listener {
    override fun onSuccess(request: ImageRequest, result: SuccessResult) {
        //Timber.d("Loaded image from ${result.dataSource} using key ${result.diskCacheKey}")
    }
}
