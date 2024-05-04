package uk.co.sullenart.photoalbum.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Precision
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import uk.co.sullenart.photoalbum.photos.Photo

@Composable
fun DetailScreen(
    photoId: String,
    viewModel: DetailViewmodel = koinViewModel { parametersOf(photoId) },
) {
    DetailItem(
        photo = viewModel.photo!!,
    )
}

@Composable
private fun DetailItem(
    photo: Photo,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .transformable(state = state),
        contentAlignment = Alignment.Center,
    ) {
        val request = ImageRequest.Builder(LocalContext.current)
            .data(photo.usableUrl)
            .diskCacheKey(photo.id)
            .precision(Precision.EXACT)
            .listener(resultListener)
            .build()
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
private val resultListener = object : ImageRequest.Listener {
    override fun onSuccess(request: ImageRequest, result: SuccessResult) {
        // Timber.d("Loaded full image from ${result.dataSource} using key ${result.diskCacheKey}")
        Timber.i("Loaded image ${result.image.width} x ${result.image.height} from ${result.dataSource}")
    }
}