package pl.com.britenet.hobbyapp.utils

import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.SignInButton
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun recyclerViewHasItemCount(num: Int): Matcher<in View> {
    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description?) {
            description?.appendText("recyclerview has item count: $num")
        }

        override fun matchesSafely(item: View?): Boolean {
            val rv: RecyclerView = item as RecyclerView
            return rv.adapter?.itemCount == num
        }
    }
}

fun googleButtonHasText(resId: Int): Matcher<in View> {
    return object : TypeSafeMatcher<View>() {
        var btn: SignInButton? = null
        var text: String = ""
        override fun describeTo(description: Description?) {
            description?.appendText("has text: $text")
        }

        override fun matchesSafely(item: View?): Boolean {
            btn = item as SignInButton?
            if (btn != null) {
                text = btn!!.resources.getString(resId)
                return text == (btn!!.getChildAt(0) as Button).text
            }
            return false
        }
    }
}
