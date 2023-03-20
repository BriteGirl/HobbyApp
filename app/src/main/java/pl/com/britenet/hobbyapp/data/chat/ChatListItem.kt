package pl.com.britenet.hobbyapp.data.chat

/**
 * An item on the list of all user's chats
 */
data class ChatListItem(
    var chatId: String,
    var lastMessage: Message,
    var otherUserId: String = "",
    var otherUserUsername: String? = null
) {
    // empty constructor for Firebase deserialization use only
    constructor() : this("", Message())
}