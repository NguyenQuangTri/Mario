package local.vn.suppermario

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class CheepCheep(
    val riverStartX: Float,
    val riverEndX: Float,
    val groundY: Float,
    jumpDelayOffset: Float = 0f
) {
    val width = 52f
    val height = 50f

    var x = (riverStartX + riverEndX) / 2f - width / 2f
    var y = groundY + 120f
    private val startY = groundY + 120f

    var velocityY = 0f
    private val gravity = 950f

    var isJumping = false
    private var launchTimer = jumpDelayOffset
    var isAlive = true

    private val dstBounds = RectF()
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(235, 45, 45) }
    private val bellyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val finPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(255, 170, 0) }
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
    private val lipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(255, 140, 0) }

    fun getBounds(): RectF {
        return RectF(x, y, x + width, y + height)
    }

    fun update(dt: Float) {
        if (!isAlive) return

        if (!isJumping) {
            launchTimer += dt
            if (launchTimer >= 2.6f) {
                launchTimer = 0f
                isJumping = true
                val span = (riverEndX - riverStartX - width - 20f).coerceAtLeast(10f)
                x = riverStartX + 10f + (Math.random().toFloat() * span)
                y = startY
                velocityY = -640f
            }
        } else {
            velocityY += gravity * dt
            y += velocityY * dt

            if (y >= startY) {
                y = startY
                isJumping = false
                velocityY = 0f
            }
        }
    }

    fun draw(canvas: Canvas) {
        if (!isAlive || !isJumping || y >= groundY + 50f) return

        dstBounds.set(x, y, x + width, y + height)

        canvas.save()

        val angle = (velocityY / 12f).coerceIn(-40f, 40f)
        canvas.rotate(angle, x + width / 2f, y + height / 2f)

        // Red Body
        canvas.drawOval(dstBounds, bodyPaint)

        // Yellow Tail Fin
        canvas.drawRect(x - 6f, y + 12f, x + 8f, y + height - 12f, finPaint)

        // White Wing
        canvas.drawCircle(x + width * 0.45f, y + height * 0.55f, 11f, bellyPaint)

        // Eye
        canvas.drawCircle(x + width * 0.72f, y + height * 0.35f, 6f, bellyPaint)
        canvas.drawCircle(x + width * 0.75f, y + height * 0.35f, 3f, eyePaint)

        // Orange Lips
        canvas.drawCircle(x + width + 2f, y + height * 0.6f, 5f, lipPaint)

        canvas.restore()
    }
}
