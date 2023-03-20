package pl.com.britenet.hobbyapp.chat

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import pl.com.britenet.hobbyapp.data.chat.Message
import pl.com.britenet.hobbyapp.databinding.ItemMessageReceivedBinding
import pl.com.britenet.hobbyapp.databinding.ItemMessageSentBinding

sealed class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun setMessageContent(text: String)
    abstract fun setTime(time: String)

    class MessageReceivedViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private val binding: ItemMessageReceivedBinding = ItemMessageReceivedBinding.bind(itemView)

        override fun setMessageContent(text: String) {
            binding.messageItemContent.text = text
        }

        override fun setTime(time: String) {
            binding.messageItemTime.text = time
        }
    }

    class MessageSentViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private val binding: ItemMessageSentBinding = ItemMessageSentBinding.bind(itemView)

        override fun setMessageContent(text: String) {
            binding.messageItemContent.text = text
        }

        override fun setTime(time: String) {
            binding.messageItemTime.text = time
        }
    }
}
