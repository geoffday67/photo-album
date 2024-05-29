package uk.co.sullenart.photoalbum.detail

import android.graphics.Bitmap
import android.graphics.Matrix
import coil.size.Size
import coil.transform.Transformation
import uk.co.sullenart.photoalbum.items.MediaItem
import uk.co.sullenart.photoalbum.items.PhotoItem
import uk.co.sullenart.photoalbum.items.Rotation

class RotationTransformation(
    private val item: MediaItem,
) : Transformation {
    override val cacheKey: String = ""

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        if (item !is PhotoItem) {
            return input
        }

        if (item.rotation == Rotation.NONE) {
            return input
        }

        val matrix = Matrix()
        when (item.rotation) {
            Rotation.LEFT -> matrix.postRotate(-90f)
            Rotation.RIGHT -> matrix.postRotate(90f)
            Rotation.INVERT -> matrix.postRotate(180f)
            else -> {}
        }

        return Bitmap.createBitmap(input, 0, 0, input.width, input.height, matrix, true)
    }
}