package uk.co.sullenart.photoalbum.detail

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Size
import org.koin.compose.koinInject
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import timber.log.Timber
import uk.co.sullenart.photoalbum.R
import uk.co.sullenart.photoalbum.items.ItemUtils
import uk.co.sullenart.photoalbum.items.MediaItem
import uk.co.sullenart.photoalbum.items.PhotoItem
import uk.co.sullenart.photoalbum.items.VideoItem
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailContent(
    pageCount: Int,
    initialPage: Int,
    getItemFromIndex: (Int) -> MediaItem,
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount },
    )


    HorizontalPager(
        state = pagerState,
    ) { index ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            when (val item = getItemFromIndex(index)) {
                is PhotoItem -> {
                    PhotoItem(item)
                }
                is VideoItem -> {
                    VideoDetail(
                        video = item,
                        onLongClick = { },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoItem(
    photo: PhotoItem,
    itemUtils: ItemUtils = koinInject()
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }.roundToInt()
        val maxHeightPx = with(LocalDensity.current) { maxHeight.toPx() }.roundToInt()

        var infoVisible by remember { mutableStateOf(false) }

        var offset by remember { mutableStateOf(IntOffset.Zero) }
        val image = remember { BitmapFactory.decodeFile(itemUtils.getDetailFilename(photo)) }
        val minScale = remember {
            var result: Float = maxWidthPx.toFloat() / image.width.toFloat()
            if ((result * image.height.toFloat()).roundToInt() > maxHeightPx) {
                result = maxHeightPx.toFloat() / image.height.toFloat()
            }
            Timber.d("Minimum scale = $result")
            result
        }
        var scale by remember { mutableFloatStateOf(minScale) }
        val state = rememberTransformableState { zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceAtLeast(minScale)
            offset = IntOffset(
                (offset.x - (panChange.x / scale).roundToInt()).coerceIn(0, image.width),
                (offset.y - (panChange.y / scale).roundToInt()).coerceIn(0, image.height),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state)
                .drawWithContent {
                    val srcOffset = IntOffset(offset.x, offset.y)
                    val srcSize = IntSize((size.width / scale).roundToInt(), (size.height / scale).roundToInt())

                    val dstOffset = IntOffset(
                        ((size.width - image.width * scale) / 2).roundToInt().coerceAtLeast(0),
                        ((size.height - image.height * scale) / 2).roundToInt().coerceAtLeast(0),
                    )
                    val dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())

                    drawImage(
                        image = image.asImageBitmap(),
                        srcOffset = srcOffset,
                        srcSize = srcSize,
                        dstSize = dstSize,
                        dstOffset = dstOffset,
                    )
                },
        )
        if (infoVisible) {
            PhotoInfo(photo)
        }
    }
}

@Composable
private fun PhotoInfo(
    photo: PhotoItem,
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
                    color = Color.White,
                )
                if (photo.camera.isNotEmpty()) {
                    Text(
                        text = photo.camera,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoDetail(
    video: VideoItem,
    onLongClick: () -> Unit,
    itemUtils: ItemUtils = koinInject(),
) {
    var player: ExoPlayer? = remember { null }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
                onLongClick = { Timber.d("Video long click") },
            ),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = {
                val uri = Uri.fromFile(File(itemUtils.getDetailFilename(video)))
                val item = androidx.media3.common.MediaItem.fromUri(uri)
                player = ExoPlayer.Builder(it)
                    .build().apply {
                        setMediaItem(item)
                        playWhenReady = true
                    }
                val view = PlayerView(it).apply {
                    this.player = player
                    controllerAutoShow = false
                    hideController()
                }
                player?.prepare()
                view
            },
            onRelease = {
                player?.release()
            },
        )
    }
}

@Composable
private fun VideoInfo(
    video: VideoItem,
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
                        .format(video.creationTime),
                    color = Color.White,
                )
                if (video.camera.isNotEmpty()) {
                    Text(
                        text = video.camera,
                        color = Color.White,
                    )
                }
            }
        }
    }
}
