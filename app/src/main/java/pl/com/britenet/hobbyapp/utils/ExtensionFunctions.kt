package pl.com.britenet.hobbyapp.utils

import android.util.Log
import android.view.View
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CancellableContinuation
import pl.com.britenet.hobbyapp.data.Hobby
import kotlin.coroutines.resumeWithException

fun <T, R> Task<T>.addOnNoSuccessListeners(
    continuation: CancellableContinuation<R>,
    logTag: String,
    exceptionToThrow: Exception? = null
): Task<T> {
    this
        .addOnFailureListener {
            val exception = exceptionToThrow ?: it
            Log.e(logTag, it.message, it)
            continuation.resumeWithException(exception)
        }
        .addOnCanceledListener { continuation.cancel() }
    return this
}

/**
 * Adds callbacks for task failure and cancellation.
 * Throws an error if one occurs.
 */
fun <T> Task<T>.addOnNoSuccessListeners(
    logTag: String,
    exceptionToThrow: Exception? = null
): Task<T> {
    this
        .addOnFailureListener {
            val exception = exceptionToThrow ?: it
            Log.e(logTag, it.message, it)
            throw exception
        }
        .addOnCanceledListener { Log.e(logTag, "Operation cancelled") }
    return this
}

fun View.setOnSafeClickListener(waitingInterval: Int = 2000, onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener(waitingInterval) { onSafeClick(it) }
    setOnClickListener(safeClickListener)
}

fun List<Hobby>.getNames(): Array<String> {
    return this.map { it.name }
        .toTypedArray()
}

fun List<Hobby>.getNamesString(): String {
    val sb = StringBuilder("")
    forEachIndexed { index, hobby ->
        sb.append(hobby.name)
        if (index != lastIndex) sb.append(", ")
    }
    return sb.toString()
}
