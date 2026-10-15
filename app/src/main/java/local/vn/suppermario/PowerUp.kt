package local.vn.suppermario

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF

enum class PowerUpType {
    MUSHROOM,
    STAR,
    COIN
}

class PowerUp(
    var x: Float,
    var y: Float,
    val type: PowerUpType,
    private val bitmapCache: BitmapCache
) {
    val size = 56f
    var isCollected = false
    private var speedX = if (type == PowerUpType.MUSHROOM || type == PowerUpType.STAR) 130f else 0f
    private var velocityY = 0f
    private val gravity = 1200f
    private val popY = y - size
    private var popping = true

    private val dstRect = RectF()
    private val srcRect = Rect()

    fun getBounds(): RectF {
        return RectF(x, y, x + size, y + size)
    }

    fun update(
        dt: Float,
        groundY: Float = 0f,
        blocks: List<Block> = emptyList(),
        pipes: List<Pipe> = emptyList()
    ) {
        if (isCollected) return

        if (popping) {
            y -= 90f * dt
            if (y <= popY) {
                y = popY
                popping = false
            }
        } else {
            x += speedX * dt

            if (type != PowerUpType.COIN) {
                velocityY += gravity * dt
                y += velocityY * dt

                val footY = y + size
                var onSurface = false

                for (block in blocks) {
                    val b = block.bounds
                    val overlapX = (x + size > b.left + 3f) && (x < b.right - 3f)
                    if (overlapX && velocityY >= 0f) {
                        if (footY >= b.top && footY - velocityY * dt <= b.top + 16f) {
                            y = b.top - size
                            if (type == PowerUpType.STAR) velocityY = -380f else velocityY = 0f
                            onSurface = true
                            break
                        }
                    }
                }

                if (!onSurface) {
                    for (pipe in pipes) {
                        val p = pipe.bounds
                        val overlapX = (x + size > p.left + 3f) && (x < p.right - 3f)
                        if (overlapX && velocityY >= 0f) {
                            if (footY >= pipe.topY && footY - velocityY * dt <= pipe.topY + 16f) {
                                y = pipe.topY - size
                                if (type == PowerUpType.STAR) velocityY = -380f else velocityY = 0f
                                onSurface = true
                                break
                            }
                        }
                    }
                }

                if (!onSurface && groundY > 0f && footY >= groundY) {
                    y = groundY - size
                    if (type == PowerUpType.STAR) velocityY = -380f else velocityY = 0f
                    onSurface = true
                }

                val pBounds = getBounds()
                for (pipe in pipes) {
                    if (RectF.intersects(pBounds, pipe.bounds) && y + size > pipe.topY + 12f) {
                        speedX = -speedX
                        if (x < pipe.x) x = pipe.x - size
                        else x = pipe.bounds.right
                    }
                }
                for (block in blocks) {
                    val b = block.bounds
                    if (RectF.intersects(pBounds, b) && y + size > b.top + 12f) {
                        speedX = -speedX
                        if (x < block.x) x = block.x - size
                        else x = b.right
                    }
                }
            }
        }
    }

    fun draw(canvas: Canvas) {
        if (isCollected) return

        val bitmap = when (type) {
            PowerUpType.MUSHROOM -> bitmapCache.mushroomRed
            PowerUpType.STAR -> bitmapCache.star
            PowerUpType.COIN -> bitmapCache.coinGold
        }

        dstRect.set(x, y, x + size, y + size)
        srcRect.set(0, 0, bitmap.width, bitmap.height)
        canvas.drawBitmap(bitmap, srcRect, dstRect, null)
    }
}
