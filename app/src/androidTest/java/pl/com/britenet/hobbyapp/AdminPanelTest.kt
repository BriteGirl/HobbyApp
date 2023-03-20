package pl.com.britenet.hobbyapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.com.britenet.hobbyapp.admin.AdminPanelActivity
import pl.com.britenet.hobbyapp.di.DatabaseDependentModule
import pl.com.britenet.hobbyapp.di.ImagesModule
import pl.com.britenet.hobbyapp.utils.recyclerViewHasItemCount

@HiltAndroidTest
@UninstallModules(DatabaseDependentModule::class, ImagesModule::class)
class AdminPanelTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val scenarioRule = activityScenarioRule<AdminPanelActivity>()

    @Before
    fun setup() {
        HiltTestModules.DatabaseDependentTestModule.isUserAdmin = true
    }

    @Test
    fun when_inAdminsTab_should_displayAdmins() {
        val admins = HiltTestModules.DatabaseDependentTestModule.admins
        onView(withId(R.id.admin_list_rv))
            .check(matches(recyclerViewHasItemCount(admins.size)))
            .check(matches(hasMinimumChildCount(1)))
    }

    @Test
    fun when_inHobbiesTab_should_displaySuggestions() {
        val hobbiesSuggestions = HiltTestModules.DatabaseDependentTestModule.hobbySuggestions
        onView(withId(R.id.admin_panel_view_pager)).perform(swipeLeft())
        onView(withId(R.id.hobbies_suggestions_rv))
            .check(matches(recyclerViewHasItemCount(hobbiesSuggestions.size)))
            .check(matches(hasMinimumChildCount(1)))

        // check buttons
        onView(withId(R.id.hobby_suggestions_accept_btn)).check(matches(isDisplayed()))
        onView(withId(R.id.hobby_suggestions_reject_btn)).check(matches(isDisplayed()))
    }

    @Test
    fun when_inHobbiesTab_should_displayCheckboxes() {
        onView(withId(R.id.admin_panel_view_pager)).perform(swipeLeft())
        onView(
            allOf(
                isNotChecked(),
                withParent(
                    allOf(
                        withParentIndex(0),
                        withParent(withId(R.id.hobbies_suggestions_rv))
                    )
                )
            )
        ).check(matches(isDisplayed()))
    }

    @Test
    fun when_suggestionDeclined_should_disappear() {
        val listSize = HiltTestModules.DatabaseDependentTestModule.hobbySuggestions.size
        onView(withId(R.id.admin_panel_view_pager)).perform(swipeLeft())
        onView(
            allOf(
                isNotChecked(),
                withParent(
                    allOf(
                        withParentIndex(0),
                        withParent(withId(R.id.hobbies_suggestions_rv))
                    )
                )
            )
        )
            .perform(click())
        onView(withId(R.id.hobby_suggestions_reject_btn)).perform(click())
        onView(withId(R.id.hobbies_suggestions_rv))
            .check(matches(recyclerViewHasItemCount(listSize - 1)))
    }

    @Test
    fun when_suggestionAccepted_should_disappear() {
        val listSize = HiltTestModules.DatabaseDependentTestModule.hobbySuggestions.size
        onView(withId(R.id.admin_panel_view_pager)).perform(swipeLeft())
        onView(
            allOf(
                isNotChecked(),
                withParent(
                    allOf(
                        withParentIndex(0),
                        withParent(withId(R.id.hobbies_suggestions_rv))
                    )
                )
            )
        )
            .perform(click())
        onView(withId(R.id.hobby_suggestions_accept_btn)).perform(click())
        onView(withId(R.id.hobbies_suggestions_rv))
            .check(matches(recyclerViewHasItemCount(listSize - 1)))
    }
}
