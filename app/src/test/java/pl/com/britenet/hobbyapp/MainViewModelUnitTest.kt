package pl.com.britenet.hobbyapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coVerify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import pl.com.britenet.hobbyapp.HiltTestModules.DatabaseDependentTestModule.provideHobbyTestDatabase
import pl.com.britenet.hobbyapp.HiltTestModules.DatabaseDependentTestModule.provideUserAuthTestRepository
import pl.com.britenet.hobbyapp.data.HobbiesRepository
import pl.com.britenet.hobbyapp.data.IHobbiesRepository
import javax.inject.Inject

/**
 * These tests follow the naming convention: should_expectedBehavior_when_stateUnderTest()
 */
class MainViewModelUnitTest {
    @get:Rule var rule: TestRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    val dispatcher = UnconfinedTestDispatcher()

    @Inject
    lateinit var mockHobbiesRepo: IHobbiesRepository

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @Before
    fun setupMocks() {
        mockHobbiesRepo = HobbiesRepository(
            provideUserAuthTestRepository(),
            provideHobbyTestDatabase()
        )
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun should_exposeHobbiesList_when_Initialized() {
        val mainViewModel = MainViewModel(mockHobbiesRepo)
        runTest {
            coVerify(exactly = 1) { mockHobbiesRepo.getAllHobbies() }
            val result = mainViewModel.hobbies.value
            assertEquals(mockHobbiesRepo.getAllHobbies(), result)
        }
    }
}
