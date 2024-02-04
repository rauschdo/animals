package de.rauschdo.animals.utility

import android.graphics.*

// Given a bitmap, convert it to a byte array
fun Bitmap.toByteArray(): ByteArray {
    return try {
        this.toByteArray()
    } catch (e: Exception) {
        byteArrayOf()
    }
}

// Given a byte array containing an image, return the corresponding bitmap
fun ByteArray.toBitmap(): Bitmap {
    // If the string is empty, just return an empty white bitmap
    if (this.isEmpty()) return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}

// Get the smallest dimension in a non-square image to crop and resize it
fun Bitmap.getSquareSize() = this.width.coerceAtMost(this.height)

// Transform a square bitmap in a circular bitmap, useful for notification
fun Bitmap.getCircularBitmap(): Bitmap {
    val output = Bitmap.createBitmap(
        this.width,
        this.height,
        Bitmap.Config.ARGB_8888,
    )
    val canvas = Canvas(output)
    val color: Int = Color.GRAY
    val paint = Paint()
    val rect = Rect(0, 0, this.width, this.height)
    val rectF = RectF(rect)
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawOval(rectF, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)
    return output
}