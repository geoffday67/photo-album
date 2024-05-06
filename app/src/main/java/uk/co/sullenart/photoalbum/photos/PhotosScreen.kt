package uk.co.sullenart.photoalbum.photos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
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
import timber.log.Timber
import uk.co.sullenart.photoalbum.R
import uk.co.sullenart.photoalbum.detail.DetailContent

@Composable
fun PhotosScreen(
    albumId: String,
    navController: NavController,
    viewModel: PhotosViewmodel = koinViewModel { parametersOf(albumId, navController) },
) {
    if (viewModel.isDetail) {
        BackHandler(onBack = viewModel::onDetailBack)
        DetailContent(
            pageCount = viewModel.photoCount,
            initialPage = viewModel.currentIndex,
            getPhotoFromIndex = viewModel::getPhotoFromIndex,
        )
    } else {
        PhotosContent(
            state = LazyGridState(viewModel.firstIndex, viewModel.firstOffset),
            photos = viewModel.photoFlow.collectAsStateWithLifecycle(initialValue = emptyList()).value,
            onPhotoClicked = viewModel::onPhotoClicked,
        )
    }
}

@Composable
private fun PhotosContent(
    state: LazyGridState,
    photos: List<Photo>,
    onPhotoClicked: (Photo, Int, Int) -> Unit,
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
                count = photos.size,
                key = { photos[it].id },
            ) {
                PhotoItem(
                    photo = photos[it],
                    onClicked = { onPhotoClicked(photos[it], state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset) }
                )
            }
        }
    }
}

@Composable
private fun PhotoItem(
    photo: Photo,
    onClicked: () -> Unit,
) {
    val request = ImageRequest.Builder(LocalContext.current)
        .data(photo.usableUrl)
        .diskCacheKey(photo.id)
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
