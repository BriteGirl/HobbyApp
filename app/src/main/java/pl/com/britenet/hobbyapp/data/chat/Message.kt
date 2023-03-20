package pl.com.britenet.hobbyapp.data.chat

import com.google.firebase.firestore.ServerTimestamp
import java.text.DateFormat
import java.util.*

open class Message(
    var messageId: String,
    var fromUserUid: String,
    var content: String,
    @ServerTimestamp
    var time: Long? = null
) {
    // empty constructor for Firebase deserialization use only
    constructor() : this("", "", "")

    fun calculateTimeForDisplay(): String {
        if (time != null) {
            val timezone = TimeZone.getDefault()

            val date = Date(time!!)
            val dateFormatter = DateFormat.getInstance()
            dateFormatter.timeZone = timezone
            return dateFormatter.format(date)
        }
        return ""
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Message)
            return false
        return messageId == other.messageId
                && fromUserUid == other.fromUserUid
                && content == other.content
                && time == other.time
    }

    override fun hashCode(): Int {
        var result = messageId.hashCode()
        result = 31 * result + fromUserUid.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + (time?.hashCode() ?: 0)
        return result
    }
}