package pl.com.britenet.hobbyapp.data.chat

import com.google.firebase.database.ChildEventListener
import kotlinx.coroutines.flow.Flow

interface IChatRepository {
    fun getAllChatsInfo(listener: ChildEventListener)
    suspend fun getChatId(otherUserId: String): String?
    suspend fun getMessages(chatId: String): Flow<MessageState?>
    suspend fun sendMessage(
        otherUserId: String,
        otherUserUsername: String,
        chatId: String,
        messageContent: String
    )

    suspend fun getCurrentUserUsername(): String?
    suspend fun createChatWith(
        otherUserId: String,
        otherUserUsername: String?,
        currentUserUsername: String?
    ): String
}
