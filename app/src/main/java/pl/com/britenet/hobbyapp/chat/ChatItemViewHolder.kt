package pl.com.britenet.hobbyapp.chat

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import pl.com.britenet.hobbyapp.databinding.ItemChatBinding

class ChatItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val binding = ItemChatBinding.bind(itemView)
    fun setMessage(message: String) {
        binding.chatItemMessage.text = message
    }

    fun setTitle(title: String) {
        binding.chatItemTitle.text = title
    }

    fun setTime(time: String) {
        binding.chatItemTime.text = time
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        binding.root.setOnClickListener(listener)
    }
}
