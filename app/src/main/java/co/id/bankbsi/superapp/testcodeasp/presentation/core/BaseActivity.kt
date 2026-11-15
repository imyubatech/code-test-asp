package co.id.bankbsi.superapp.testcodeasp.presentation.core

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AlertDialog
import co.id.bankbsi.superapp.testcodeasp.R
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import co.id.bankbsi.superapp.testcodeasp.TestCodeAspApp
import co.id.bankbsi.superapp.testcodeasp.presentation.login.LoginActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

abstract class BaseActivity : AppCompatActivity() {
    protected val app: TestCodeAspApp get() = application as TestCodeAspApp
    protected open val sessionRequired: Boolean = false

    private var languageDialog: AlertDialog? = null
    private var languageChangeJob: Job? = null
    private var expiryJob: Job? = null
    private var redirectingToLogin = false

    override fun onResume() {
        super.onResume()
        if (sessionRequired && ensureSessionActive()) scheduleExpiry()
    }

    override fun onPause() {
        expiryJob?.cancel()
        expiryJob = null
        super.onPause()
    }

    override fun onDestroy() {
        languageChangeJob?.cancel()
        languageDialog?.dismiss()
        languageDialog = null
        expiryJob?.cancel()
        expiryJob = null
        super.onDestroy()
    }

    protected fun ensureSessionActive(): Boolean {
        if (!sessionRequired) return true
        if (redirectingToLogin) return false
        if (!app.sessionManager.isActive()) {
            navigateToLogin(timedOut = true)
            return false
        }
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (sessionRequired && (ev.actionMasked == MotionEvent.ACTION_DOWN ||
                    ev.actionMasked == MotionEvent.ACTION_MOVE)) {
            if (!ensureSessionActive()) return true
            recordInteraction()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (sessionRequired && event.action == KeyEvent.ACTION_DOWN) {
            if (!ensureSessionActive()) return true
            recordInteraction()
        }
        return super.dispatchKeyEvent(event)
    }

    private fun recordInteraction() {
        if (app.sessionManager.recordInteraction()) scheduleExpiry()
    }

    private fun scheduleExpiry() {
        expiryJob?.cancel()
        expiryJob = null
        if (redirectingToLogin || isFinishing || isDestroyed) return
        val remaining = app.sessionManager.remainingMs()
        if (remaining <= 0L) {
            navigateToLogin(timedOut = true)
        } else {
            expiryJob = lifecycleScope.launch {
                delay(remaining)
                if (ensureSessionActive()) scheduleExpiry()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Also handle hosts that apply the locale without recreating the Activity.
        languageDialog?.dismiss()
        languageDialog = null
    }

    protected fun toggleLanguage() {
        if (languageDialog != null || isFinishing || isDestroyed) return
        val currentLanguage = resources.configuration.locales[0].language
        val nextLanguage = if (currentLanguage == "id" || currentLanguage == "in") "en" else "id"
        languageDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_language_loading)
            .setCancelable(false)
            .create()
            .also { it.show() }
        languageChangeJob = lifecycleScope.launch {
            delay(1.seconds)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(nextLanguage))
        }
    }

    protected fun logout() = navigateToLogin(timedOut = false)

    private fun navigateToLogin(timedOut: Boolean) {
        if (redirectingToLogin || isFinishing || isDestroyed) return
        redirectingToLogin = true
        expiryJob?.cancel()
        app.sessionManager.clear()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(LoginActivity.EXTRA_TIMED_OUT, timedOut)
        })
        finish()
    }
}
