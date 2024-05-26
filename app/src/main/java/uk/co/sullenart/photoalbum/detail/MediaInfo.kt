package uk.co.sullenart.photoalbum.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import uk.co.sullenart.photoalbum.R
import uk.co.sullenart.photoalbum.items.PhotoItem

@Composable
fun MediaInfo(
    photo: PhotoItem,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(dimensionResource(R.dimen.paddingM))
    ) {
        Text(
            "${stringResource(R.string.created)} " +
                DateTimeFormatter
                    .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                    .withZone(ZoneId.systemDefault())
                    .format(photo.creationTime),
        )
        if (photo.camera.isNotEmpty()) {
            Text("${stringResource(R.string.camera)} ${photo.camera}")
        }
        //Text("Description")
        var description by remember { mutableStateOf("") }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = description,
            onValueChange = { description = it },
            minLines = 5,
        )
        DialogButtons (onDismiss)
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