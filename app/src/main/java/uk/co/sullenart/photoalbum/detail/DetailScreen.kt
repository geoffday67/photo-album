package uk.co.sullenart.photoalbum.detail

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.mxalbert.zoomable.Zoomable
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

private interface VisibilityListener {
    fun onVisible()
    fun onHidden()
}

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

    var previouslyVisible: Int? = remember { null }
    val visibilityListeners = remember { mutableMapOf<Int, VisibilityListener>() }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            visibilityListeners[it]?.onVisible()
            visibilityListeners[previouslyVisible]?.onHidden()
            previouslyVisible = it
        }
    }

    var infoVisible by remember { mutableStateOf(false) }

    HorizontalPager(
        state = pagerState,
        beyondBoundsPageCount = 1,
    ) { index ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            when (val item = getItemFromIndex(index)) {
                is PhotoItem -> {
                    ZoomablePhoto(
                        photo = item,
                        onTap = { infoVisible = !infoVisible },
                    )
                    if (infoVisible) {
                        PhotoInfo(item)
                    }
                }
                is VideoItem -> {
                    VideoDetail(
                        video = item,
                        register = { visibilityListeners[index] = it },
                        toggleInfo = { infoVisible = !infoVisible },
                    )
                    if (infoVisible) {
                        VideoInfo(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomablePhoto(
    photo: PhotoItem,
    onTap: () -> Unit,
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(photo)
            .size(Size.ORIGINAL)
            .memoryCacheKey("${photo.id}-detail")
            .setParameter("type", "detail")
            .build(),
        onSuccess = {
            Timber.i("Image loaded from ${it.result.dataSource} for ${photo.id}")
        },
    )
    Zoomable(
        onTap = { onTap() },
    ) {
        if (painter.state is AsyncImagePainter.State.Success) {
            Image(
                modifier = Modifier
                    .aspectRatio(painter.intrinsicSize.width / painter.intrinsicSize.height)
                    .fillMaxSize(),
                painter = painter,
                contentDescription = null,
            )
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
private fun VideoDetail(
    video: VideoItem,
    register: (VisibilityListener) -> Unit,
    toggleInfo: () -> Unit,
    itemUtils: ItemUtils = koinInject(),
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val context = LocalContext.current
        val player = remember {
            Timber.i("Preparing video player")
            val uri = Uri.fromFile(File(itemUtils.getDetailFilename(video)))
            val item = androidx.media3.common.MediaItem.fromUri(uri)
            ExoPlayer.Builder(context)
                .build().apply {
                    setMediaItem(item)
                    playWhenReady = false
                    prepare()
                }
        }

        AndroidView(
            factory = {
                val view = PlayerView(it).apply {
                    this.player = player
                    controllerAutoShow = false
                    hideController()
                    videoSurfaceView?.setOnLongClickListener {
                        toggleInfo()
                        true
                    }
                }

                register(object : VisibilityListener {
                    override fun onVisible() {
                        with (player) {
                            seekTo(0)
                            play()
                        }
                    }

                    override fun onHidden() {
                        with (player) {
                            pause()
                        }
                    }
                })

                view
            },
            onRelease = {
                Timber.i("Releasing video player")
                player.release()
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
