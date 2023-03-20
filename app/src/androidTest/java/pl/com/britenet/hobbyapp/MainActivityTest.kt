package pl.com.britenet.hobbyapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import pl.com.britenet.hobbyapp.di.DatabaseDependentModule
import pl.com.britenet.hobbyapp.di.ImagesModule
import pl.com.britenet.hobbyapp.utils.recyclerViewHasItemCount

/**
 * Instrumented test, which will execute on an Android device.
 * These tests follow the naming convention: should_expectedBehavior_when_stateUnderTest()
 */

@HiltAndroidTest
@UninstallModules(DatabaseDependentModule::class, ImagesModule::class)
class MainActivityTest {
    @get:Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @Test
    fun when_hobbyAdded_should_goBack() {
        HiltTestModules.DatabaseDependentTestModule.isUserLoggedIn = true
        onView(withId(R.id.add_hobby_fab)).perform(click())
        onView(withId(R.id.new_hobby_name))
            .perform(typeText("Hobby"), closeSoftKeyboard())
        onView(withId(R.id.submit_new_hobby_btn)).perform(click())
        onView(withId(R.id.add_hobby_fab)).check(matches(isDisplayed()))
    }

    @Test
    fun when_userIsAdmin_should_displayHobbySuggestion() {
        HiltTestModules.DatabaseDependentTestModule.isUserLoggedIn = true
        HiltTestModules.DatabaseDependentTestModule.isUserAdmin = true
        val hobbyName = "Hobby"
        val suggestedHobbiesNum = HiltTestModules.DatabaseDependentTestModule.hobbySuggestions.size

        // submit new hobby suggestion
        onView(withId(R.id.add_hobby_fab)).perform(click())
        onView(withId(R.id.new_hobby_name))
            .perform(typeText(hobbyName), closeSoftKeyboard())
        onView(withId(R.id.submit_new_hobby_btn)).perform(click())

        // go to the admin fragment and check count
        onView(withId(R.id.accountFragment)).perform(click())
        onView(withId(R.id.admin_panel_btn)).perform(click())
        onView(withId(R.id.admin_panel_view_pager)).perform(swipeLeft())
        onView(withId(R.id.hobbies_suggestions_rv))
            .check(matches(recyclerViewHasItemCount(suggestedHobbiesNum + 1)))
    }

    @Test
    fun when_userIsAdmin_should_acceptHobbySuggestion() {
        HiltTestModules.DatabaseDependentTestModule.isUserLoggedIn = true
        HiltTestModules.DatabaseDependentTestModule.isUserAdmin = true
        val hobbiesNum = HiltTestModules.DatabaseDependentTestModule.hobbiesList.size

        // go to the suggestions tab in the admin fragment
        onView(withId(R.id.accountFragment)).perform(click())
        onView(withId(R.id.admin_panel_btn)).perform(click())
        onView(withId(R.id.admin_panel_view_pager)).perform(swipeLeft())

        // accept the first suggestion
        onView(
            Matchers.allOf(
                ViewMatchers.isNotChecked(),
                ViewMatchers.withParent(
                    Matchers.allOf(
                        ViewMatchers.withParentIndex(0),
                        ViewMatchers.withParent(withId(R.id.hobbies_suggestions_rv))
                    )
                )
            )
        )
            .perform(click())
        onView(withId(R.id.hobby_suggestions_accept_btn)).perform(click())

        // go back
        pressBack()
        onView(withId(R.id.mainFragment)).perform(click())
        onView(withId(R.id.hobbies_rv))
            .check(matches(recyclerViewHasItemCount(hobbiesNum + 1)))
    }
}
