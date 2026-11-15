package co.id.bankbsi.superapp.testcodeasp

import android.os.SystemClock
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import co.id.bankbsi.superapp.testcodeasp.presentation.login.LoginActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginUiTest {
    private var scenario: ActivityScenario<LoginActivity>? = null
    private lateinit var previousLocales: LocaleListCompat

    @Before fun launchLogin() {
        val app = ApplicationProvider.getApplicationContext<TestCodeAspApp>()
        app.sessionManager.clear()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            previousLocales = AppCompatDelegate.getApplicationLocales()
        }
        scenario = ActivityScenario.launch(LoginActivity::class.java)
        scenario!!.onActivity {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
        }
        awaitLocale("en")
    }

    @After fun cleanup() {
        scenario?.onActivity { AppCompatDelegate.setApplicationLocales(previousLocales) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        scenario?.close()
    }

    private fun awaitLocale(language: String) {
        val deadline = SystemClock.uptimeMillis() + 5_000L
        var applied = false
        while (!applied && SystemClock.uptimeMillis() < deadline) {
            scenario!!.onActivity { applied = it.resources.configuration.locales[0].language == language }
            if (!applied) SystemClock.sleep(50)
        }
        assertTrue("Expected locale $language", applied)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    @Test fun emptyLoginShowsBothValidationErrors() {
        onView(withId(R.id.buttonLogin)).perform(scrollTo(), click())
        onView(withId(R.id.inputUsername)).check(matches(hasErrorText("Username is required")))
        onView(withId(R.id.inputPassword)).check(matches(hasErrorText("Password is required")))
        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()))
    }

    @Test fun editingInvalidUsernameClearsItsError() {
        onView(withId(R.id.buttonLogin)).perform(scrollTo(), click())
        onView(withId(R.id.inputUsername)).perform(scrollTo(), replaceText("reader"), closeSoftKeyboard())
        scenario!!.onActivity { activity ->
            assertNull(activity.findViewById<android.widget.EditText>(R.id.inputUsername).error)
            assertNotNull(activity.findViewById<android.widget.EditText>(R.id.inputPassword).error)
        }
    }

    @Test fun languageToggleShowsProgressThenRecreatesInIndonesian() {
        var originalActivity: LoginActivity? = null
        scenario!!.onActivity { originalActivity = it }
        onView(withId(R.id.buttonLanguage)).perform(click())
        onView(withText(R.string.changing_language)).check(matches(isDisplayed()))
        val deadline = SystemClock.uptimeMillis() + 5_000L
        var applied = false
        while (!applied && SystemClock.uptimeMillis() < deadline) {
            scenario!!.onActivity { activity ->
                applied = activity !== originalActivity &&
                    activity.resources.configuration.locales[0].language in listOf("id", "in")
            }
            if (!applied) SystemClock.sleep(50)
        }
        assertTrue("Indonesian locale was not applied", applied)
        onView(withText(R.string.changing_language)).check(doesNotExist())
        onView(withId(R.id.buttonLogin)).check(matches(withText("Jelajahi Artikel")))
        scenario!!.recreate()
        onView(withId(R.id.buttonLogin)).check(matches(withText("Jelajahi Artikel")))
    }
}
