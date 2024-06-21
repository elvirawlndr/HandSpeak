
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.media.Image
import java.io.ByteArrayOutputStream

fun Image.toBitmap(): Bitmap {
    val nv21 = yuv420888ToNv21(this)
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 100, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

private fun yuv420888ToNv21(image: Image): ByteArray {
    val width = image.width
    val height = image.height
    val ySize = width * height
    val uvSize = width * height / 4

    val nv21 = ByteArray(ySize + uvSize * 2)
    val yBuffer = image.planes[0].buffer // Y
    val uBuffer = image.planes[1].buffer // U
    val vBuffer = image.planes[2].buffer // V

    var rowStride = image.planes[0].rowStride
    assert(image.planes[0].pixelStride == 1)

    var pos = 0
    if (rowStride == width) { // likely
        yBuffer.get(nv21, 0, ySize)
        pos += ySize
    } else {
        var yBufferPos = -rowStride.toLong() // not an actual position
        while (pos < ySize) {
            yBufferPos += rowStride.toLong()
            yBuffer.position(yBufferPos.toInt())
            yBuffer.get(nv21, pos, width)
            pos += width
        }
    }

    rowStride = image.planes[2].rowStride
    val pixelStride = image.planes[2].pixelStride

    assert(rowStride == image.planes[1].rowStride)
    assert(pixelStride == image.planes[1].pixelStride)

    var vBufferPos = -rowStride.toLong() // not an actual position
    var uBufferPos = -rowStride.toLong() // not an actual position
    while (pos < nv21.size) {
        vBufferPos += rowStride.toLong()
        uBufferPos += rowStride.toLong()
        vBuffer.position(vBufferPos.toInt())
        uBuffer.position(uBufferPos.toInt())

        val vuPos = pos
        for (x in 0 until width / 2) {
            nv21[vuPos + x * 2] = vBuffer.get()
            nv21[vuPos + x * 2 + 1] = uBuffer.get()
        }

        pos += width
    }
    return nv21
}
