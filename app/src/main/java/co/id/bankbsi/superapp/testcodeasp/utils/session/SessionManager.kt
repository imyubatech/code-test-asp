package co.id.bankbsi.superapp.testcodeasp.utils.session

import android.content.Context
import android.os.SystemClock
import android.provider.Settings

class SessionManager(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("testcodeasp_session", Context.MODE_PRIVATE)

    private var lastPersistedElapsed: Long = -1L

    fun login(username: String) {
        prefs.edit()
            .putBoolean(LOGGED_IN, true)
            .putString(USERNAME, username)
            .putLong(LAST_ELAPSED, SystemClock.elapsedRealtime())
            .putLong(LAST_WALL, System.currentTimeMillis())
            .putInt(BOOT_COUNT, deviceBootCount())
            .apply()
        lastPersistedElapsed = SystemClock.elapsedRealtime()
    }

    fun username(): String = prefs.getString(USERNAME, null).orEmpty()

    fun isLoggedIn(): Boolean = prefs.getBoolean(LOGGED_IN, false)

    fun remainingMs(): Long {
        if (!isLoggedIn()) return 0L
        val nowElapsed = SystemClock.elapsedRealtime()
        val savedElapsed = prefs.getLong(LAST_ELAPSED, -1L)
        val nowBoot = deviceBootCount()
        val savedBoot = prefs.getInt(BOOT_COUNT, -1)
        val baseRemaining = SessionTimeoutPolicy.remainingMs(savedElapsed, nowElapsed, savedBoot, nowBoot)

        if (baseRemaining == 0L) return 0L

        // Conservative fallback when OEM does not expose a usable boot counter.
        if (savedBoot < 0 || nowBoot < 0) {
            val savedWall = prefs.getLong(LAST_WALL, -1L)
            val nowWall = System.currentTimeMillis()
            if (savedWall <= 0L || nowWall < savedWall) return 0L
            return minOf(baseRemaining, (SessionTimeoutPolicy.TIMEOUT_MS - (nowWall - savedWall)).coerceAtLeast(0L))
        }
        return baseRemaining
    }

    fun isActive(): Boolean = remainingMs() > 0L

    fun recordInteraction(): Boolean {
        if (!isActive()) return false
        val now = SystemClock.elapsedRealtime()
        if (lastPersistedElapsed >= 0 && now - lastPersistedElapsed < 750L) return false
        prefs.edit()
            .putLong(LAST_ELAPSED, now)
            .putLong(LAST_WALL, System.currentTimeMillis())
            .putInt(BOOT_COUNT, deviceBootCount())
            .apply()
        lastPersistedElapsed = now
        return true
    }

    fun clear() {
        prefs.edit().clear().apply()
        lastPersistedElapsed = -1L
    }

    private fun deviceBootCount(): Int = try {
        Settings.Global.getInt(appContext.contentResolver, Settings.Global.BOOT_COUNT, -1)
    } catch (_: SecurityException) {
        -1
    }

    private companion object {
        const val LOGGED_IN = "logged_in"
        const val USERNAME = "username"
        const val LAST_ELAPSED = "last_elapsed"
        const val LAST_WALL = "last_wall"
        const val BOOT_COUNT = "boot_count"
    }
}
