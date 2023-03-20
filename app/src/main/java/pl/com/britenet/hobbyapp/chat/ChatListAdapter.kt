package pl.com.britenet.hobbyapp.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import pl.com.britenet.hobbyapp.R
import pl.com.britenet.hobbyapp.data.chat.ChatListItem

class ChatListAdapter(val goToChatWith: (String, String, String?) -> Unit) : RecyclerView.Adapter<ChatItemViewHolder>() {
    private var chatList = listOf<ChatListItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_chat, parent, false)
        return ChatItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
        val currentChatItem = chatList[position]
        holder.setMessage(currentChatItem.lastMessage.content)
        holder.setTitle(currentChatItem.otherUserUsername ?: "")
        holder.setTime(currentChatItem.lastMessage.calculateTimeForDisplay())
        holder.setOnClickListener() { goToChatWith(currentChatItem.chatId, currentChatItem.otherUserId, currentChatItem.otherUserUsername) }
    }

    override fun getItemCount(): Int = chatList.size

    fun updateList(newList: List<ChatListItem>) {
        val diff = DiffUtil.calculateDiff(getDiffCb(chatList, newList))
        chatList = newList
        diff.dispatchUpdatesTo(this)
    }

    private fun getDiffCb(
        oldList: List<ChatListItem>,
        newList: List<ChatListItem>
    ) = object : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.chatId == newItem.chatId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem == newItem
        }
    }
}
