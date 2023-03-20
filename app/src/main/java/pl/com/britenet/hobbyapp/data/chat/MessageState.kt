package pl.com.britenet.hobbyapp.data.chat

sealed class MessageState() {
    class NewMessage(
        val message: Message
    ) : MessageState()

    class ChangedMessage(
        val message: Message
    ) : MessageState()
}