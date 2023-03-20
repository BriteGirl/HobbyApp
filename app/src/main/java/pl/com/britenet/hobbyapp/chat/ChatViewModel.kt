package pl.com.britenet.hobbyapp.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.com.britenet.hobbyapp.data.chat.IChatRepository
import pl.com.britenet.hobbyapp.data.chat.Message
import pl.com.britenet.hobbyapp.data.chat.MessageState
import pl.com.britenet.hobbyapp.exceptions.ExceptionMessages
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(private val chatRepo: IChatRepository) : ViewModel() {
    private companion object {
        const val LOG_TAG = "HobbyApp.ChatViewModel"
    }

    private lateinit var otherUserId: String
    private lateinit var otherUserUsername: String
    private lateinit var chatId: String

    private val allMessages: MutableList<Message> = mutableListOf()
    private val _messages: MutableLiveData<List<Message>> = MutableLiveData(listOf())
    val messages: LiveData<List<Message>>
        get() = _messages
    private val _displayedUsername = MutableLiveData("")
    val displayedUsername: LiveData<String>
        get() = _displayedUsername

    private val _secondUserId = MutableLiveData<String>()
    val secondUserId: LiveData<String>
        get() = _secondUserId

    private val _errorToShow = MutableLiveData<String?>(null)
    val errorToShow: LiveData<String?>
        get() = _errorToShow

    fun loadData(otherUserId: String?, otherUserUsername: String?, chatId: String?) {
        // check data received from previous activity
        if (!setInitialData(otherUserId, otherUserUsername, chatId)) return

        viewModelScope.launch(Dispatchers.IO) {
            supplyMissingAttributes(chatId, otherUserId)
            // load data
            chatRepo.getMessages(this@ChatViewModel.chatId).collect { messageState ->
                when (messageState) {
                    null -> {}
                    is MessageState.NewMessage -> {
                        allMessages.add(messageState.message)
                        _messages.postValue(allMessages.toList())
                    }
                    is MessageState.ChangedMessage -> {
                        val message = messageState.message
                        val changedMessageIndex = allMessages
                            .indexOfFirst { listMessage -> listMessage.messageId == message.messageId }
                        if (changedMessageIndex != -1) {
                            allMessages[changedMessageIndex] = message
                            _messages.postValue(allMessages.toList())
                        }
                    }
                }
            }
        }
    }

    private fun setInitialData(
        otherUserId: String?,
        otherUserUsername: String?,
        chatId: String?
    ): Boolean {
        if (chatId == null && otherUserId == null) {
            Log.e(
                LOG_TAG,
                "Initial data incorrect: otherUserId=$otherUserId, otherUserUsername=$otherUserUsername"
            )
            _errorToShow.postValue(ExceptionMessages.SOMETHING_WENT_WRONG.message)
            return false
        }
        if (otherUserId != null) {
            this.otherUserId = otherUserId
            this._secondUserId.postValue(otherUserId)
        }
        if (chatId != null)
            this.chatId = chatId

        // without this the activity will work
        if (otherUserUsername != null) {
            this.otherUserUsername = otherUserUsername
            _displayedUsername.postValue(otherUserUsername)
        }
        return true
    }

    private suspend fun supplyMissingAttributes(chatId: String?, otherUserId: String?) {
        if (chatId == null) {
            // get username of the current user
            val currentUserUsername = chatRepo.getCurrentUserUsername()
            // get chatId
            val repoChatId = chatRepo.getChatId(this.otherUserId)
            if (repoChatId == null) this.chatId = chatRepo.createChatWith(
                this.otherUserId,
                otherUserUsername,
                currentUserUsername
            )
            else this.chatId = repoChatId
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepo.sendMessage(otherUserId, otherUserUsername, chatId, text)
        }
    }

    fun onErrorShown() {
        _errorToShow.postValue(null)
    }
}
