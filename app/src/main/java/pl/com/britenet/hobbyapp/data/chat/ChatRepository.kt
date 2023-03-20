package pl.com.britenet.hobbyapp.data.chat

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import pl.com.britenet.hobbyapp.data.firestore.UserDocFields
import pl.com.britenet.hobbyapp.data.user.UserData
import pl.com.britenet.hobbyapp.data.user.UserRepository
import pl.com.britenet.hobbyapp.exceptions.NoUserSignedInException
import pl.com.britenet.hobbyapp.utils.addOnNoSuccessListeners
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ChatRepository @Inject constructor(private val DB_URL: String) : IChatRepository {
    private companion object {
        const val LOG_TAG = "HobbyApp.ChatRepository"

        const val CHAT_INFO_NODE = "chats"
        const val CHAT_INFO_ID_FIELD = "chatId"
        const val CHAT_INFO_LAST_MESSAGE_FIELD = "lastMessage"
        const val CHAT_INFO_USERNAME_FIELD = "otherUserUsername"

        const val MESSAGES_NODE = "messages"
        const val MESSAGE_TIME_FIELD = "time"
    }

    override fun getAllChatsInfo(listener: ChildEventListener) {
        val userId = Firebase.auth.uid ?: throw NoUserSignedInException()
        getDatabaseRef().child(CHAT_INFO_NODE).child(userId)
            .addChildEventListener(listener)
    }

    override suspend fun getChatId(otherUserId: String): String? {
        val currentUserId = Firebase.auth.uid ?: throw NoUserSignedInException()
        val db = getDatabaseRef()
        val chatInfoQuery = db.child(CHAT_INFO_NODE).child(currentUserId)
            .child(otherUserId).child(CHAT_INFO_ID_FIELD)

        return suspendCancellableCoroutine { continuation ->
            chatInfoQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        val chatId = snapshot.getValue(String::class.java)!!
                        continuation.resume(chatId)
                    } else continuation.resume(null)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(LOG_TAG, error.message, error.toException())
                    continuation.resumeWithException(error.toException())
                }
            })
        }
    }

    override suspend fun getMessages(chatId: String): Flow<MessageState?> {
        return callbackFlow {
            val messageListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(Message::class.java)
                    val returnVal = if (message != null) MessageState.NewMessage(message)
                    else null
                    trySend(returnVal)
                }

                override fun onChildChanged(
                    snapshot: DataSnapshot,
                    previousChildName: String?
                ) {
                    val incomingMessage = snapshot.getValue(Message::class.java)
                    val returnVal = if (incomingMessage == null) null
                    else MessageState.ChangedMessage(incomingMessage)
                    trySend(returnVal)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(LOG_TAG, error.message, error.toException())
                    throw CancellationException()
                }

                // no feature yet
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            }
            try {
                getDatabaseRef().child(MESSAGES_NODE).child(chatId)
                    .addChildEventListener(messageListener)
            } catch (e: CancellationException) {
                cancel(e)
            } finally {
                awaitClose()
            }
        }
    }

    override suspend fun sendMessage(
        otherUserId: String,
        otherUserUsername: String,
        chatId: String,
        messageContent: String
    ) {
        val currentUserId = Firebase.auth.uid ?: throw NoUserSignedInException()
        val db = getDatabaseRef()

        val chatRef = db.child(MESSAGES_NODE).child(chatId)
        val newMessageRef = chatRef.push()
        val messageId = newMessageRef.key!!
        val message = Message(messageId, currentUserId, messageContent)

        // save the message
        chatRef.child(messageId).setValue(message)
            .addOnNoSuccessListeners(LOG_TAG)
            .await()
        // set timestamp
        chatRef.child(messageId).child(MESSAGE_TIME_FIELD).setValue(ServerValue.TIMESTAMP)
            .addOnNoSuccessListeners(LOG_TAG)
            .await()

        // get the message & save it as lastMessage for this chat
        val messageSnapshot = chatRef.child(messageId).get().await()
        val messageData = messageSnapshot.getValue(Message::class.java)
        db.child(CHAT_INFO_NODE).child(currentUserId).child(otherUserId)
            .child(CHAT_INFO_LAST_MESSAGE_FIELD).setValue(messageData)
            .addOnNoSuccessListeners(LOG_TAG)
        db.child(CHAT_INFO_NODE).child(otherUserId).child(currentUserId)
            .child(CHAT_INFO_LAST_MESSAGE_FIELD).setValue(messageData)
            .addOnNoSuccessListeners(LOG_TAG)
            .await()
        val currentUserDataSnapshot = Firebase.firestore
            .collection(UserRepository.USERS_COLLECTION)
            .whereEqualTo(UserDocFields.USER_ID.fieldName, currentUserId).get()
            .await()
        val currentUserUsername = currentUserDataSnapshot
            .toObjects(UserData::class.java)[0].username ?: ""

        updateChatInfoUsernames(currentUserId, otherUserId, currentUserUsername, otherUserUsername)
    }

    override suspend fun getCurrentUserUsername(): String? {
        val currentUserId = Firebase.auth.uid
        val usernameQuery = Firebase.firestore.collection(UserRepository.USERS_COLLECTION)
            .whereEqualTo(UserDocFields.USER_ID.fieldName, currentUserId).get()
        return suspendCancellableCoroutine { continuation ->
            usernameQuery.addOnSuccessListener {
                val currentUserUsername = it
                    .toObjects(UserData::class.java)[0].username
                continuation.resume(currentUserUsername)
            }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
    }

    override suspend fun createChatWith(
        otherUserId: String,
        otherUserUsername: String?,
        currentUserUsername: String?
    ): String {
        val currentUserId = Firebase.auth.uid ?: throw NoUserSignedInException()
        val newChatRef = getDatabaseRef().child(MESSAGES_NODE).push()
        val chatId = newChatRef.key!!
        val chatInfoRef =
            getDatabaseRef().child(CHAT_INFO_NODE).child(currentUserId).child(otherUserId)
        val otherUserChatInfoRef =
            getDatabaseRef().child(CHAT_INFO_NODE).child(otherUserId).child(currentUserId)

        chatInfoRef.child(CHAT_INFO_ID_FIELD).setValue(chatId).await()
        otherUserChatInfoRef.child(CHAT_INFO_ID_FIELD).setValue(chatId).await()

        updateChatInfoUsernames(
            currentUserId,
            otherUserId,
            currentUserUsername ?: "",
            otherUserUsername ?: ""
        )

        return chatId
    }

    private suspend fun updateChatInfoUsernames(
        currentUserId: String,
        otherUserId: String,
        currentUserUsername: String,
        otherUserUsername: String
    ) {
        val db = getDatabaseRef()
        db.child(CHAT_INFO_NODE).child(currentUserId).child(otherUserId)
            .child(CHAT_INFO_USERNAME_FIELD).setValue(otherUserUsername).await()
        db.child(CHAT_INFO_NODE).child(otherUserId).child(currentUserId)
            .child(CHAT_INFO_USERNAME_FIELD).setValue(currentUserUsername).await()
    }

    private fun getDatabaseRef() = Firebase
        .database(DB_URL)
        .reference
}