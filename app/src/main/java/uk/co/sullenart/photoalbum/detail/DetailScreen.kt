package uk.co.sullenart.photoalbum.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Precision
import timber.log.Timber
import uk.co.sullenart.photoalbum.R
import uk.co.sullenart.photoalbum.photos.Photo
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailContent(
    pageCount: Int,
    initialPage: Int,
    getPhotoFromIndex: (Int) -> Photo,
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount },
    )

    var infoVisible by remember { mutableStateOf(false) }

    HorizontalPager(
        state = pagerState,
    ) { index ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            val photo = getPhotoFromIndex(index)
            DetailItem(photo) { infoVisible = !infoVisible }
            if (infoVisible) {
                Info(photo)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DetailItem(
    photo: Photo,
    onLongClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val request = ImageRequest
            .Builder(LocalContext.current)
            .data(photo.usableUrl)
            .diskCacheKey(photo.id)
            .precision(Precision.EXACT)
            .listener(resultListener)
            .build()
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {},
                    onLongClick = onLongClick,
                ),
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun Info(
    photo: Photo,
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(dimensionResource(R.dimen.paddingM)),
            colors = cardColors(
                containerColor = Color.Unspecified.copy(alpha = 0.2f),
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.paddingM))
            ) {
                Text(
                    text = DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                        .withZone(ZoneId.systemDefault())
                        .format(photo.creationTime),
                    color = Color.White.copy(alpha = 0.8f),
                )
                if (photo.camera.isNotEmpty()) {
                    Text(
                        text = photo.camera,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
private val resultListener = object : ImageRequest.Listener {
    override fun onSuccess(request: ImageRequest, result: SuccessResult) {
        // Timber.d("Loaded full image from ${result.dataSource} using key ${result.diskCacheKey}")
        Timber.i("Loaded image ${result.image.width} x ${result.image.height} from ${result.dataSource}")
    }
}