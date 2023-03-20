package pl.com.britenet.hobbyapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import launchFragmentInHiltContainer
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import pl.com.britenet.hobbyapp.HiltTestModules.DatabaseDependentTestModule.isUserLoggedIn
import pl.com.britenet.hobbyapp.HiltTestModules.DatabaseDependentTestModule.userHobbies
import pl.com.britenet.hobbyapp.data.user.IUserAuthRepository
import pl.com.britenet.hobbyapp.di.DatabaseDependentModule
import pl.com.britenet.hobbyapp.di.ImagesModule
import pl.com.britenet.hobbyapp.userhobbies.UserHobbiesFragment

/**
 * Instrumented test, which will execute on an Android device.
 * These tests follow the naming convention: should_expectedBehavior_when_stateUnderTest()
 */

@HiltAndroidTest
@UninstallModules(DatabaseDependentModule::class, ImagesModule::class)
@RunWith(JUnit4::class)
@ExperimentalCoroutinesApi
class UserHobbiesFragmentTest {
    @get:Rule
    val hiltAndroidRule = HiltAndroidRule(this)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userAuthRepository: IUserAuthRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userAuthRepository = HiltTestModules.DatabaseDependentTestModule.provideUserAuthTestRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun when_noUserLoggedIn_should_displayMessage() {
        // setup repositories to show no user logged in
        userHobbies = mutableListOf()

        // check if the layout is warning user they're not logged in and not showing data
        launchFragmentInHiltContainer<UserHobbiesFragment>()
        onView(withId(R.id.signed_in_only_warning)).check(matches(isDisplayed()))
        onView(withId(R.id.user_hobbies_rv)).check(matches(hasChildCount(0)))
    }

    @Test
    fun when_userLoggedIn_should_displayHobbies() {
        isUserLoggedIn = true
        launchFragmentInHiltContainer<UserHobbiesFragment>()
        onView(withId(R.id.user_hobbies_rv))
            .check(matches(hasChildCount(userHobbies.size)))
        onView(withId(R.id.signed_in_only_warning)).check(matches(not(isDisplayed())))
    }
}
