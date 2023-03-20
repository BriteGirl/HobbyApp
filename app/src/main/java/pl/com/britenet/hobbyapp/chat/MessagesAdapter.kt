package pl.com.britenet.hobbyapp.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import pl.com.britenet.hobbyapp.R
import pl.com.britenet.hobbyapp.data.chat.Message

class MessagesAdapter : RecyclerView.Adapter<MessageViewHolder>() {
    private val VIEW_TYPE_MESSAGE_RECEIVED = 1
    private val VIEW_TYPE_MESSAGE_SENT = 2
    private var otherUserId: String = ""
    private var messages = listOf<Message>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            val itemView = inflater.inflate(R.layout.item_message_received, parent, false)
            return MessageViewHolder.MessageReceivedViewHolder(itemView)
        }
        else {
            val itemView = inflater.inflate(R.layout.item_message_sent, parent, false)
            return MessageViewHolder.MessageSentViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.setMessageContent(message.content)
        holder.setTime(message.calculateTimeForDisplay())
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        if (otherUserId.isEmpty()) return 0

        val message = messages[position]
        val viewType = if (message.fromUserUid == otherUserId) VIEW_TYPE_MESSAGE_RECEIVED
                        else VIEW_TYPE_MESSAGE_SENT
        return viewType
    }

    fun updateMessages(newMessages: List<Message>) {
        val diff = DiffUtil.calculateDiff(getDiffUtilCallback(messages, newMessages))
        messages = newMessages
        diff.dispatchUpdatesTo(this)
    }

    fun updateOtherUserId(otherUserId: String) {
        if (this.otherUserId != otherUserId) {
            this.otherUserId = otherUserId
        }
    }

    private fun getDiffUtilCallback(
        oldList: List<Message>,
        newList: List<Message>
    ): DiffUtil.Callback = object : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldMessage = oldList[oldItemPosition]
            val newMessage = newList[newItemPosition]
            return oldMessage.messageId == newMessage.messageId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldMessage = oldList[oldItemPosition]
            val newMessage = newList[newItemPosition]
            return oldMessage === newMessage
        }
    }
}