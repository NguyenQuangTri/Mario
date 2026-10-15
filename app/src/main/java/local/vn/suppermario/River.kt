package local.vn.suppermario

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF

class River(
    val startX: Float,
    val endX: Float,
    val groundY: Float,
    private val bitmapCache: BitmapCache
) {
    val bounds = RectF(startX, groundY + 20f, endX, groundY + 200f)
    private val srcRect = Rect()
    private val dstRect = RectF()

    fun draw(canvas: Canvas, screenHeight: Float) {
        val tileSize = 70f
        var currX = startX
        val bitmap = bitmapCache.waterTop
        srcRect.set(0, 0, bitmap.width, bitmap.height)

        while (currX < endX) {
            val tileW = if (currX + tileSize > endX) endX - currX else tileSize
            dstRect.set(currX, groundY, currX + tileW, screenHeight)
            canvas.drawBitmap(bitmap, srcRect, dstRect, null)
            currX += tileSize
        }
    }
}
