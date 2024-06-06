package uk.co.sullenart.photoalbum.detail

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import timber.log.Timber
import uk.co.sullenart.photoalbum.items.ItemUtils
import uk.co.sullenart.photoalbum.items.MediaItem
import uk.co.sullenart.photoalbum.items.PhotoItem
import uk.co.sullenart.photoalbum.items.Rotation
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
    onCurrentPage: (Int) -> Unit,
    getInfoIndex: () -> Int,
    getItemFromIndex: (Int) -> MediaItem,
    onRotationSelected: (MediaItem, Rotation) -> Unit,
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

            onCurrentPage(it)
        }
    }

    var infoVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        HorizontalPager(
            state = pagerState,
            //beyondBoundsPageCount = 1,
        ) { index ->
            when (val item = getItemFromIndex(index)) {
                is PhotoItem -> {
                    ZoomablePhoto(
                        photo = item,
                        onTap = { infoVisible = !infoVisible },
                    )
                }
                is VideoItem -> {
                    VideoDetail(
                        video = item,
                        register = { visibilityListeners[index] = it },
                        toggleInfo = { infoVisible = !infoVisible },
                    )
                }
            }
        }
        if (infoVisible) {
            MediaInfo(
                item = getItemFromIndex(getInfoIndex()),
                onDismiss = { infoVisible = false },
                onRotationSelected = onRotationSelected,
            )
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
            .setParameter("type", "detail")
            .transformations(RotationTransformation(photo))
            .build(),
        onSuccess = {
            Timber.i("Image ${it.result.drawable.intrinsicWidth} x ${it.result.drawable.intrinsicHeight} loaded from ${it.result.dataSource} for key ${it.result.memoryCacheKey?.key}")
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
                        with(player) {
                            seekTo(0)
                            play()
                        }
                    }

                    override fun onHidden() {
                        with(player) {
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