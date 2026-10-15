package local.vn.suppermario

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.abs

class Mario(
    private var worldWidth: Float,
    private var screenHeight: Float,
    private val bitmapCache: BitmapCache,
    var characterType: CharacterType = CharacterType.MARIO
) {
    var weapon = WeaponType.NONE

    var isBig = false
        private set
    var isSuperSpeed = false
        private set

    var speedTimer = 0f
    var invincibleTimer = 0f
    var starInvincibleTimer = 0f
        private set
    val isStarInvincible get() = starInvincibleTimer > 0f

    var size = 80f
        get() = if (isBig) 98f else 80f

    val startX = 100f
    val startY = screenHeight - size - 100f

    var x = startX
    var y = startY

    var velocityY = 0f
    private val gravity = JumpPhysics.GRAVITY
    private val jumpStrength = -JumpPhysics.JUMP_SPEED

    var velocityX = 0f
    private val baseSpeed = JumpPhysics.RUN_SPEED
    private val accel = JumpPhysics.ACCELERATION
    private val friction = 1800f

    var isGrounded = false
        private set

    var isSkidding = false
        private set

    var facingLeft = false
    private var animTime = 0f
    private var spinJumpAngle = 0f

    private val dstRect = RectF()
    private val srcRect = Rect()
    private val spritePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 215, 0)
        alpha = 180
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    private val fireAuraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 90, 0)
        alpha = 160
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val swordPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }
    private val skidDustPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 180
    }

    fun reset() {
        x = startX
        y = startY
        velocityX = 0f
        velocityY = 0f
        isGrounded = true
        isSkidding = false
        facingLeft = false
        isBig = false
        isSuperSpeed = false
        speedTimer = 0f
        invincibleTimer = 0f
        starInvincibleTimer = 0f
        weapon = WeaponType.NONE
        spinJumpAngle = 0f
    }

    fun growBig() {
        if (!isBig) {
            isBig = true
            y -= 18f
        }
    }

    fun shrinkToNormal() {
        isBig = false
        invincibleTimer = 2.0f
    }

    fun enableSuperSpeed() {
        isSuperSpeed = true
        speedTimer = 10.0f
    }

    fun enableStarInvincibility() {
        starInvincibleTimer = 5.0f
        isSuperSpeed = true
        speedTimer = 5.0f
    }

    fun getBounds(): RectF {
        return RectF(x, y, x + size, y + size)
    }

    fun updateX(dt: Float, isLeft: Boolean, isRight: Boolean) {
        if (speedTimer > 0f) {
            speedTimer -= dt
            if (speedTimer <= 0f) {
                isSuperSpeed = false
            }
        }

        if (starInvincibleTimer > 0f) {
            starInvincibleTimer -= dt
        }

        if (invincibleTimer > 0f) {
            invincibleTimer -= dt
        }

        val currentMaxSpeed = if (isSuperSpeed) baseSpeed * 1.55f else baseSpeed

        // Detect Skidding (slid/turnaround from Super Mario model sheet)
        isSkidding = isGrounded && ((isLeft && velocityX > 100f) || (isRight && velocityX < -100f))

        if (isLeft && !isRight) {
            val currentAccel = if (isSkidding) accel * 1.8f else accel
            velocityX = (velocityX - currentAccel * dt).coerceAtLeast(-currentMaxSpeed)
            if (!isSkidding) facingLeft = true
        } else if (isRight && !isLeft) {
            val currentAccel = if (isSkidding) accel * 1.8f else accel
            velocityX = (velocityX + currentAccel * dt).coerceAtMost(currentMaxSpeed)
            if (!isSkidding) facingLeft = false
        } else {
            if (velocityX > 0) {
                velocityX = (velocityX - friction * dt).coerceAtLeast(0f)
            } else if (velocityX < 0) {
                velocityX = (velocityX + friction * dt).coerceAtMost(0f)
            }
        }

        x += velocityX * dt
        if (x < 0f) {
            x = 0f
            velocityX = 0f
        }
        if (x > worldWidth - size) {
            x = worldWidth - size
            velocityX = 0f
        }

        if (abs(velocityX) > 20f && isGrounded) {
            val animRate = if (abs(velocityX) > 280f) 16f else 11f
            animTime += dt * (animRate / 10f)
        } else {
            animTime = 0f
        }

        if (!isGrounded) {
            spinJumpAngle = (spinJumpAngle + 360f * dt * 2.5f) % 360f
        } else {
            spinJumpAngle = 0f
        }
    }

    fun updateY(dt: Float) {
        isGrounded = false
        velocityY += gravity * dt
        y += velocityY * dt

        val groundLevel = screenHeight - size - 100f
        if (y >= groundLevel) {
            y = groundLevel
            velocityY = 0f
            isGrounded = true
        }
    }

    fun jump() {
        if (isGrounded) {
            val currentJumpStrength = if (isBig) jumpStrength * 1.08f else jumpStrength
            velocityY = currentJumpStrength
            isGrounded = false
        }
    }

    fun releaseJump() {
        if (velocityY < -200f) {
            velocityY *= 0.45f
        }
    }

    fun teleportTo(targetX: Float, surfaceTop: Float) {
        x = targetX + 15f
        y = surfaceTop - size - 12f
        velocityX = 0f
        velocityY = 0f
        isGrounded = false
    }

    fun setGroundedOn(topY: Float) {
        y = topY - size
        velocityY = 0f
        isGrounded = true
    }

    fun draw(canvas: Canvas) {
        if (invincibleTimer > 0f && (invincibleTimer * 10f).toInt() % 2 == 0) {
            return
        }

        val (idleBmp, walkABmp, walkBBmp, jumpBmp) = when (characterType) {
            CharacterType.MARIO, CharacterType.BEIGE -> Quadruple(bitmapCache.marioIdle, bitmapCache.marioWalkA, bitmapCache.marioWalkB, bitmapCache.marioJump)
            CharacterType.LUIGI, CharacterType.GREEN -> Quadruple(bitmapCache.luigiIdle, bitmapCache.luigiWalkA, bitmapCache.luigiWalkB, bitmapCache.luigiJump)
            CharacterType.TOAD -> Quadruple(bitmapCache.toadIdle, bitmapCache.toadWalkA, bitmapCache.toadWalkB, bitmapCache.toadJump)
            CharacterType.TOADETTE, CharacterType.PINK -> Quadruple(bitmapCache.toadetteIdle, bitmapCache.toadetteWalkA, bitmapCache.toadetteWalkB, bitmapCache.toadetteJump)
        }

        val animFrame = ((animTime * 10f).toInt() % 2)
        val currentBitmap = when {
            !isGrounded -> jumpBmp
            isSkidding -> walkBBmp
            abs(velocityX) > 20f -> if (animFrame == 0) walkABmp else walkBBmp
            else -> idleBmp
        }

        dstRect.set(x, y, x + size, y + size)
        srcRect.set(0, 0, currentBitmap.width, currentBitmap.height)

        canvas.save()

        val centerX = x + size / 2f
        val centerY = y + size / 2f

        // Skid dust effect (from Super Mario model sheet)
        if (isSkidding) {
            val dustX = if (facingLeft) x + size + 8f else x - 8f
            canvas.drawCircle(dustX, y + size - 10f, 10f, skidDustPaint)
            canvas.drawCircle(dustX + (if (facingLeft) 12f else -12f), y + size - 5f, 6f, skidDustPaint)
        }

        if (facingLeft) {
            canvas.scale(-1f, 1f, centerX, centerY)
        }

        // Spin jump tilt effect
        if (!isGrounded && abs(velocityY) > 100f) {
            val tiltAngle = if (facingLeft) -spinJumpAngle * 0.15f else spinJumpAngle * 0.15f
            canvas.rotate(tiltAngle, centerX, centerY)
        }

        canvas.drawBitmap(currentBitmap, srcRect, dstRect, spritePaint)

        // Star Invincibility Rainbow / Gold Aura Effect (5 seconds)
        if (starInvincibleTimer > 0f) {
            val colorIdx = ((animTime * 25f).toInt() % 3)
            auraPaint.color = when (colorIdx) {
                0 -> Color.rgb(255, 215, 0)   // Gold
                1 -> Color.rgb(255, 105, 180) // Pink
                else -> Color.rgb(0, 230, 255) // Cyan
            }
            val pulsingRadius = size / 2f + 14f + ((animTime * 20f).toInt() % 8)
            canvas.drawCircle(centerX, centerY, pulsingRadius, auraPaint)
            canvas.drawCircle(centerX, centerY, pulsingRadius - 4f, fireAuraPaint)
        } else if (isSuperSpeed) {
            val pulsingRadius = size / 2f + 8f + ((animTime * 15f).toInt() % 6)
            canvas.drawCircle(centerX, centerY, pulsingRadius, auraPaint)
        }

        // Fire / Weapon Power Aura
        if (weapon != WeaponType.NONE && weapon != WeaponType.SWORD) {
            canvas.drawCircle(centerX, centerY, size / 2f + 4f, fireAuraPaint)
        }

        // Sword Effect
        if (weapon == WeaponType.SWORD) {
            val swordX = x + size
            canvas.drawLine(swordX, y + size / 2f, swordX + 35f, y + 10f, swordPaint)
        }

        canvas.restore()
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
