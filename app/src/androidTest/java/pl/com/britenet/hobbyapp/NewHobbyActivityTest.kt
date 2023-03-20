package pl.com.britenet.hobbyapp

import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.com.britenet.hobbyapp.addhobby.NewHobbyActivity
import pl.com.britenet.hobbyapp.di.DatabaseDependentModule
import pl.com.britenet.hobbyapp.di.ImagesModule
import pl.com.britenet.hobbyapp.exceptions.ExceptionMessages
import pl.com.britenet.hobbyapp.utils.OverlayMatcher

/**
 * Instrumented test, which will execute on an Android device.
 * These tests follow the naming convention: should_expectedBehavior_when_stateUnderTest()
 */

@HiltAndroidTest
@UninstallModules(DatabaseDependentModule::class, ImagesModule::class)
@ExperimentalCoroutinesApi
class NewHobbyActivityTest {
    @get:Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityScenarioRule = activityScenarioRule<NewHobbyActivity>()

    @Before
    fun setUp() {
    }

    @Test
    fun when_noUserSignedIn_should_DisplayMessage() {
        HiltTestModules.DatabaseDependentTestModule.isUserLoggedIn = false
        onView(withId(R.id.submit_new_hobby_btn)).perform(click())
        onView(withText(ExceptionMessages.NO_USER_SIGNED_IN.message))
            .inRoot(OverlayMatcher())
            .check(matches(isDisplayed()))
    }

    @Test
    fun when_noData_should_displayMessage() {
        HiltTestModules.DatabaseDependentTestModule.isUserLoggedIn = true
        onView(withId(R.id.submit_new_hobby_btn)).perform(click())
        onView(withText(ExceptionMessages.FORM_FIELD_EMPTY.message))
            .inRoot(OverlayMatcher())
            .check(matches(isDisplayed()))
    }

    @Test
    fun when_hobbyNameTyped_should_finish() {
        HiltTestModules.DatabaseDependentTestModule.isUserLoggedIn = true
        onView(withId(R.id.new_hobby_name))
            .perform(typeText("Hobby"), closeSoftKeyboard())
        onView(withId(R.id.submit_new_hobby_btn)).perform(click())
        assertEquals(activityScenarioRule.scenario.state, Lifecycle.State.DESTROYED)
    }
}
