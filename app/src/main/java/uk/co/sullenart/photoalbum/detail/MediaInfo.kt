package uk.co.sullenart.photoalbum.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import uk.co.sullenart.photoalbum.R
import uk.co.sullenart.photoalbum.items.MediaItem
import uk.co.sullenart.photoalbum.items.PhotoItem
import uk.co.sullenart.photoalbum.items.Rotation

@Composable
fun MediaInfo(
    item: MediaItem,
    onDismiss: () -> Unit,
    onRotationSelected: (PhotoItem, Rotation) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card {
            Column(
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.paddingM), start = dimensionResource(R.dimen.paddingM), end = dimensionResource(R.dimen.paddingM)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.paddingS))
            ) {
                Text(
                    "${stringResource(R.string.created)} " +
                        DateTimeFormatter
                            .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                            .withZone(ZoneId.systemDefault())
                            .format(item.creationTime),
                )
                if (item.camera.isNotEmpty()) {
                    Text("${stringResource(R.string.camera)} ${item.camera}")
                }
                if (item is PhotoItem) {
                    Rotation(
                        photo = item,
                        onRotationSelected = { onRotationSelected(item, it) },
                    )
                }
                DialogButtons(onDismiss)
            }
        }
    }
}

@Composable
private fun Rotation(
    photo: PhotoItem,
    onRotationSelected: (Rotation) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Rotation:")
        Rotation.values().forEach {
            RotationOption(
                rotation = it,
                selected = photo.rotation == it,
                onSelected = { onRotationSelected(it) },
            )
        }
    }
}

@Composable
private fun RotationOption(
    rotation: Rotation,
    selected: Boolean,
    onSelected: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = dimensionResource(R.dimen.paddingM)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelected,
        )
        Text(
            when (rotation) {
                Rotation.NONE -> "None"
                Rotation.LEFT -> "Left"
                Rotation.RIGHT -> "Right"
                Rotation.INVERT -> "Upside down"
            }
        )
    }
}

@Composable
private fun DialogButtons(
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(
            onClick = onDismiss,
        ) {
            Text("Close")
        }
    }
}