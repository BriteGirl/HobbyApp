package pl.com.britenet.hobbyapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import launchFragmentInHiltContainer
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import pl.com.britenet.hobbyapp.user.AccountFragment

/**
 * Instrumented test, which will execute on an Android device.
 * These tests follow the naming convention: should_expectedBehavior_when_stateUnderTest()
 */

@HiltAndroidTest
@ExperimentalCoroutinesApi
class UserAccountFragmentTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    @ExperimentalCoroutinesApi
    fun when_noUserLoggedIn_should_displayLoginBtn() {
        HiltTestModules.DatabaseDependentTestModule.isUserLoggedIn = false
        launchFragmentInHiltContainer<AccountFragment>()

        onView(withId(R.id.user_greeting)).check(matches(not(isDisplayed())))
        onView(withId(R.id.log_in_btn)).check(matches(isDisplayed()))
    }

    @Test
    @ExperimentalCoroutinesApi
    fun when_userSignedIn_should_displayUserData() {
        HiltTestModules.DatabaseDependentTestModule.isUserLoggedIn = true
        launchFragmentInHiltContainer<AccountFragment>()
        val user = HiltTestModules.DatabaseDependentTestModule.provideUserAuthTestRepository().getCurrentUser()
        assertNotNull(user)

        onView(withId(R.id.log_in_btn)).check(matches(not(isDisplayed())))
        onView(withId(R.id.user_greeting)).check(matches(isDisplayed()))
        onView(withId(R.id.user_email)).check(matches(withText(user!!.email)))
        onView(withId(R.id.user_greeting)).check(matches(withText("Hello, " + user.displayName + "!")))
    }
}
