package co.id.bankbsi.superapp.testcodeasp.utils.session

object SessionTimeoutPolicy {
    const val TIMEOUT_MS = 180_000L

    fun remainingMs(savedElapsed: Long, nowElapsed: Long, savedBoot: Int, nowBoot: Int): Long {
        if (savedElapsed !in 0L..nowElapsed) return 0L
        if (savedBoot >= 0 && nowBoot >= 0 && savedBoot != nowBoot) return 0L
        return (TIMEOUT_MS - (nowElapsed - savedElapsed)).coerceAtLeast(0L)
    }
}
