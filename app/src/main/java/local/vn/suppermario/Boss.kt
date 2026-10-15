package local.vn.suppermario

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF

class Boss(
    var x: Float,
    private val groundY: Float,
    val width: Float = 160f,
    val height: Float = 160f,
    private val bitmapCache: BitmapCache,
    private val rules: ChallengeRules
) {
    var y = groundY - height
    private val combat = BossCombat(rules.bossHp, rules.bossShotInterval)
    val maxHp get() = combat.maxHp
    val hp get() = combat.hp
    val isDefeated get() = combat.isDefeated
    val isEnraged get() = combat.isEnraged
    val invincibleTimer get() = combat.invincibleTimer
    private val homeX = x
    private var direction = -1f

    private var velocityY = 0f
    private var isGrounded = true
    private var jumpTimer = 0f
    private var animTime = 0f

    private val dstRect = RectF()
    private val srcRect = Rect()
    private val hpBarBgRect = RectF()
    private val hpBarFillRect = RectF()

    private val spritePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val enragedAuraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 60, 0)
        alpha = 180
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    private val hpBarBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(40, 40, 40)
        style = Paint.Style.FILL
    }
    private val hpBarBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 215, 0)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val hpBarFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(220, 40, 40)
        style = Paint.Style.FILL
    }
    private val hpTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    fun getBounds(): RectF {
        return RectF(x, y, x + width, y + height)
    }

    fun takeDamage(amount: Int = 1) = combat.takeDamage(amount)

    fun consumeShot() = combat.consumeShot()

    fun update(dt: Float, active: Boolean) {
        combat.update(dt, active)
        if (!active || isDefeated) return

        animTime += dt
        val speed = if (isEnraged) 190f else 125f
        x += direction * speed * dt

        if (x < homeX - 230f || x > homeX + 230f) {
            x = x.coerceIn(homeX - 230f, homeX + 230f)
            direction = -direction
        }

        // Bowser Stomp Jump Attack
        jumpTimer += dt
        val jumpCooldown = if (isEnraged) 1.8f else 3.2f
        if (jumpTimer >= jumpCooldown && isGrounded) {
            jumpTimer = 0f
            velocityY = -420f
            isGrounded = false
        }

        if (!isGrounded) {
            velocityY += 1200f * dt
            y += velocityY * dt
            val baseGround = groundY - height
            if (y >= baseGround) {
                y = baseGround
                velocityY = 0f
                isGrounded = true
            }
        }
    }

    fun draw(canvas: Canvas) {
        if (isDefeated) return

        if (invincibleTimer > 0f && (invincibleTimer * 10f).toInt() % 2 == 0) {
            return
        }

        val bitmap = bitmapCache.bossTurtle
        dstRect.set(x, y, x + width, y + height)
        srcRect.set(0, 0, bitmap.width, bitmap.height)

        canvas.save()

        // Flip sprite based on movement direction
        if (direction > 0) {
            canvas.scale(-1f, 1f, x + width / 2f, y + height / 2f)
        }

        // Draw Enraged Bowser Flame Aura
        if (isEnraged) {
            val auraPulse = 8f + ((animTime * 12f).toInt() % 6)
            canvas.drawCircle(x + width / 2f, y + height / 2f, width / 2f + auraPulse, enragedAuraPaint)
        }

        canvas.drawBitmap(bitmap, srcRect, dstRect, spritePaint)
        canvas.restore()

        // Health Bar Rendering
        val barW = 160f
        val barH = 18f
        val barX = x + (width - barW) / 2f
        val barY = y - 35f

        hpBarBgRect.set(barX, barY, barX + barW, barY + barH)
        canvas.drawRoundRect(hpBarBgRect, 6f, 6f, hpBarBgPaint)
        canvas.drawRoundRect(hpBarBgRect, 6f, 6f, hpBarBorderPaint)

        val healthRatio = (hp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)
        if (healthRatio > 0f) {
            hpBarFillPaint.color = if (isEnraged) Color.rgb(255, 140, 0) else Color.rgb(220, 40, 40)
            hpBarFillRect.set(barX + 2f, barY + 2f, barX + 2f + (barW - 4f) * healthRatio, barY + barH - 2f)
            canvas.drawRoundRect(hpBarFillRect, 4f, 4f, hpBarFillPaint)
        }

        val hpLabel = if (isEnraged) "BOWSER [ENRAGED]" else "KING BOWSER"
        canvas.drawText(hpLabel, x + width / 2f, barY - 8f, hpTextPaint)
    }
}
