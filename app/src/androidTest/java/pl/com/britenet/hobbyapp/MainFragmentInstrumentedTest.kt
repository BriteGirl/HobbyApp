package pl.com.britenet.hobbyapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import launchFragmentInHiltContainer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.com.britenet.hobbyapp.data.IHobbiesRepository
import pl.com.britenet.hobbyapp.di.DatabaseDependentModule
import pl.com.britenet.hobbyapp.di.ImagesModule
import javax.inject.Inject

/**
 * Instrumented test, which will execute on an Android device.
 * These tests follow the naming convention: should_expectedBehavior_when_stateUnderTest()
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(DatabaseDependentModule::class, ImagesModule::class)
class MainFragmentInstrumentedTest {
    private val appTheme = R.style.AppTheme

    @get:Rule
    var hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var hobbiesRepository: IHobbiesRepository

    @ExperimentalCoroutinesApi
    val dispatcher = UnconfinedTestDispatcher()

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        hiltAndroidRule.inject()
        Dispatchers.setMain(dispatcher)
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun when_addFabClicked_should_startNewHobbyActivity() {
        launchFragmentInHiltContainer<MainFragment>(null, appTheme)
        onView(withId(R.id.add_hobby_fab)).perform(click())
        // new_hobby_name EditText is displayed in NewHobbyActivity
        onView(withId(R.id.new_hobby_name)).check(matches(isDisplayed()))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun when_launched_should_displayHobbiesList() = runTest {
        launchFragmentInHiltContainer<MainFragment>(null, appTheme)
        val hobbiesCount = hobbiesRepository.getAllHobbies().size
        onView(withId(R.id.hobbies_rv)).check(matches(hasChildCount(hobbiesCount)))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun when_hobbyClicked_should_showHobbyDetails() {
        launchFragmentInHiltContainer<MainFragment>()
        val hobbyName = HiltTestModules.DatabaseDependentTestModule.hobbiesList[0].name
        onView(withText(hobbyName)).apply {
            check(matches(isDisplayed()))
            perform(click())
        }
        onView(withId(R.id.hobby_details_name)).apply {
            check(matches(isDisplayed()))
            check(matches(withText(hobbyName)))
        }
    }
}
