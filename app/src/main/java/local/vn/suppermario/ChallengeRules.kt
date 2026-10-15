package local.vn.suppermario

/** Difficulty affects encounter timing as well as lives. All modes use the same jump physics. */
data class ChallengeRules(
    val enemySpeed: Float,
    val hazardTempo: Float,
    val platformSpeed: Float,
    val timeScale: Float,
    val bossHp: Int,
    val bossShotInterval: Float,
    val bossShotSpeed: Float
) {
    companion object {
        fun forDifficulty(difficulty: Difficulty) = when (difficulty) {
            Difficulty.EASY -> ChallengeRules(0.82f, 0.8f, 0.85f, 1.25f, 10, 1.8f, 360f)
            Difficulty.MEDIUM -> ChallengeRules(0.95f, 0.95f, 0.95f, 1.15f, 12, 1.5f, 400f)
            Difficulty.HARD -> ChallengeRules(1.15f, 1.15f, 1.05f, 1.0f, 16, 1.25f, 460f)
        }
    }
}
