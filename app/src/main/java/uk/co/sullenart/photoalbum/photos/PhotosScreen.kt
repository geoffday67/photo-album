package uk.co.sullenart.photoalbum.photos

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.request.SuccessResult
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun PhotosScreen(
    viewModel: PhotosViewmodel = koinViewModel(),
) {
    Content(
        photos = viewModel.photoFlow.collectAsStateWithLifecycle(initialValue = emptyList()).value,
    )
}

@Composable
private fun Content(
    photos: List<Photo>,
) {
    // TODO Check for recomposition
    Timber.d("Recomposing photos content")
    LazyColumn(
        Modifier.fillMaxSize(),
    ) {
        items(photos) {
            val request = ImageRequest.Builder(LocalContext.current)
                .data("${it.url}=w2048-h2048")
                .diskCacheKey(it.id)
                .listener(resultListener)
                .build()
            AsyncImage(model = request, contentDescription = null)
        }
    }
}

private val resultListener = object: ImageRequest.Listener {
    override fun onSuccess(request: ImageRequest, result: SuccessResult) {
        Timber.d("Loaded image from ${result.dataSource} using key ${result.diskCacheKey}")
    }
}
