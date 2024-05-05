package uk.co.sullenart.photoalbum.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import uk.co.sullenart.photoalbum.R
import uk.co.sullenart.photoalbum.photos.Photo
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    pageCount: Int,
    initialPage: Int,
    getPhotoFromIndex: (Int) -> Photo,
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount },
    )
    HorizontalPager(
        state = pagerState,
    ) { index ->
        DetailItem(
            photo = getPhotoFromIndex(index),
        )
    }
}

@Composable
private fun BottomSheetContent(
    photo: Photo,
) {
    Info(photo)
}

@Composable
private fun DetailItem(
    photo: Photo,
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
                "Created ${
                    DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .withZone(ZoneId.systemDefault())
                        .format(photo.creationTime)
                }"
            )
            Text("Camera ${photo.camera}")
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