package pl.com.britenet.hobbyapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.Test
import pl.com.britenet.hobbyapp.di.DatabaseDependentModule
import pl.com.britenet.hobbyapp.di.ImagesModule
import pl.com.britenet.hobbyapp.user.LoginActivity
import pl.com.britenet.hobbyapp.utils.googleButtonHasText

@HiltAndroidTest
@UninstallModules(DatabaseDependentModule::class, ImagesModule::class)
class LoginActivityTest {
    @get:Rule(order = 0)
    val hilAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val intentsTestRule = IntentsTestRule(LoginActivity::class.java)

    @Test
    fun when_noUserLoggedIn_should_displayLoginPage() {
        HiltTestModules.DatabaseDependentTestModule.isUserLoggedIn = false
        onView(withId(R.id.email_input)).check(matches(isDisplayed()))
        onView(withId(R.id.password_input)).check(matches(isDisplayed()))
        onView(withId(R.id.login_or_register_label)).check(matches(withText(R.string.sign_in_label)))
        onView(withId(R.id.google_sign_in_btn)).check(matches(googleButtonHasText(R.string.sign_in_with_google)))
        onView(withId(R.id.login_or_register_link)).check(matches(withText(R.string.register)))
    }

    @Test
    fun when_noUserLoggedIn_should_displayRegisterPage() {
        HiltTestModules.DatabaseDependentTestModule.isUserLoggedIn = false
        // go to register screen
        onView(withId(R.id.login_or_register_link)).perform(click())

        // check displayed elements
        onView(withId(R.id.email_input)).check(matches(isDisplayed()))
        onView(withId(R.id.password_input)).check(matches(isDisplayed()))
        onView(withId(R.id.login_or_register_label)).check(matches(withText(R.string.register_label)))
        onView(withId(R.id.google_sign_in_btn)).check(matches(googleButtonHasText(R.string.sign_up_with_google)))
        onView(withId(R.id.login_or_register_link)).check(matches(withText(R.string.log_in)))
    }

    @Test
    fun when_userClicksGoogleSignIn_should_goStartGoogleSignIn() {
        HiltTestModules.DatabaseDependentTestModule.isUserLoggedIn = false
        onView(withId(R.id.google_sign_in_btn)).perform(click())
        intending(hasAction("com.google.android.gms.auth.GOOGLE_SIGN_IN"))
    }
}
