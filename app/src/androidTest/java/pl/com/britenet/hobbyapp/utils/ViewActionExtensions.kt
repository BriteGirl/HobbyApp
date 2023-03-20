package pl.com.britenet.hobbyapp.utils

import android.view.View
import androidx.test.espresso.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.*
import java.util.concurrent.TimeoutException

fun waitForViewAction(viewId: Int, millis: Long = 4000L): ViewAction {
    return waitForViewAction(withId(viewId), millis)
}

fun waitForViewAction(viewMatcher: Matcher<View>, millis: Long = 4000L): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isRoot()
        }

        override fun getDescription(): String {
            return "wait for a specific view that matches <$viewMatcher> during $millis millis."
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()
            val startTime = System.currentTimeMillis()
            val endTime = startTime + millis
            do {
                for (child in TreeIterables.breadthFirstViewTraversal(view)) {
                    // found view with required ID
                    if (viewMatcher.matches(child)) {
                        return
                    }
                }

                uiController.loopMainThreadForAtLeast(50)
            } while (System.currentTimeMillis() < endTime)

            // timeout happens
            throw PerformException.Builder()
                .withActionDescription(this.description)
                .withViewDescription(HumanReadables.describe(view))
                .withCause(TimeoutException())
                .build()
        }
    }
}

fun waitFor(millis: Long): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isRoot()
        }

        override fun getDescription(): String {
            return "Wait for $millis milliseconds."
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadForAtLeast(millis)
        }
    }
}

fun waitLongOnUiThread() {
    waitOnUiThread(6000L)
}

fun waitMediumOnUiThread() {
    waitOnUiThread(4000L)
}

fun waitShortOnUiThread() {
    waitOnUiThread(1000L)
}

fun waitOnUiThread(millis: Long = 3000L) {
    Espresso.onView(isRoot()).perform(waitFor(millis))
}

fun waitForView(viewMatcher: Matcher<View>, timeout: Long = 4000) {
    Espresso.onView(isRoot()).perform(waitForViewAction(viewMatcher, timeout))
}
