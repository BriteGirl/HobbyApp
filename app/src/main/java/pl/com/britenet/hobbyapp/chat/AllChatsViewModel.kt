package pl.com.britenet.hobbyapp.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.com.britenet.hobbyapp.data.chat.ChatListItem
import pl.com.britenet.hobbyapp.data.chat.IChatRepository
import pl.com.britenet.hobbyapp.data.user.IUserAuthRepository
import pl.com.britenet.hobbyapp.exceptions.ExceptionMessages
import pl.com.britenet.hobbyapp.exceptions.NoUserSignedInException
import javax.inject.Inject

@HiltViewModel
class AllChatsViewModel @Inject constructor(
    private val chatRepo: IChatRepository,
    private val authRepo: IUserAuthRepository
) : ViewModel() {
    private companion object {
        const val LOG_TAG = "HobbyApp.AllChatsViewModel"
    }

    private val allChats = mutableListOf<ChatListItem>()
    private val _chats = MutableLiveData<List<ChatListItem>>(listOf())
    val chats: LiveData<List<ChatListItem>>
        get() = _chats

    private val _isUserLoggedIn = MutableLiveData<Boolean?>(null)
    val isUserLoggedIn: LiveData<Boolean?>
        get() = _isUserLoggedIn

    private val _errorToShow = MutableLiveData<String?>(null)
    val errorToShow: LiveData<String?>
        get() = _errorToShow

    init {
        checkForUser()
        loadData()
    }

    fun checkForUser() {
        _isUserLoggedIn.postValue(null)
        val user = authRepo.getCurrentUser()
        _isUserLoggedIn.postValue(user != null)
    }

    private fun loadData() {
        try {
            chatRepo.getAllChatsInfo(getChatInfoListener())
        } catch (e: NoUserSignedInException) {
            _isUserLoggedIn.postValue(false)
        }
    }

    private fun getChatInfoListener(): ChildEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            if (_isUserLoggedIn.value != true)
                _isUserLoggedIn.postValue(true)
            val chatInfo = snapshot.getValue(ChatListItem::class.java)
            chatInfo?.otherUserId = snapshot.key.toString()
            if (chatInfo != null) {
                allChats.add(chatInfo)
                _chats.postValue(allChats.toList())
                Log.i(LOG_TAG, "allChats: $allChats")
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            val newChatInfo = snapshot.getValue(ChatListItem::class.java)
            newChatInfo?.otherUserId = snapshot.key.toString()
            if (newChatInfo != null) {
                val changedItemIndex =
                    allChats.indexOfFirst { item -> item.chatId == newChatInfo.chatId }
                if (changedItemIndex != -1) {
                    allChats[changedItemIndex] = newChatInfo
                    _chats.postValue(allChats.toList())
                }
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val removedChatInfo = snapshot.getValue(ChatListItem::class.java)
            removedChatInfo?.otherUserId = snapshot.key.toString()
            if (removedChatInfo != null) {
                allChats.remove(removedChatInfo)
                _chats.postValue(allChats.toList())
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(LOG_TAG, error.message, error.toException())
            if (error.code == DatabaseError.DISCONNECTED || error.code == DatabaseError.NETWORK_ERROR)
                _errorToShow.postValue(ExceptionMessages.NETWORK_ERROR.message)
            else
                _errorToShow.postValue(ExceptionMessages.SOMETHING_WENT_WRONG.message)
        }

        // no feature yet
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
    }

    fun onErrorShown() {
        _errorToShow.postValue(null)
    }
}
