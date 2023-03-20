package pl.com.britenet.hobbyapp.data.user

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import pl.com.britenet.hobbyapp.data.firestore.FriendRequestDocFields.FROM_USER
import pl.com.britenet.hobbyapp.data.firestore.FriendRequestDocFields.TO_USER
import pl.com.britenet.hobbyapp.data.firestore.UserDocFields.*
import pl.com.britenet.hobbyapp.data.firestore.UserFriendsDocFields.FRIENDS_IDS
import pl.com.britenet.hobbyapp.exceptions.*
import pl.com.britenet.hobbyapp.utils.addOnNoSuccessListeners
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import pl.com.britenet.hobbyapp.data.firestore.UserFriendsDocFields.USER_ID as FRIENDS_DOC_UID

class UserRepository @Inject constructor(private val userAuthRepository: IUserAuthRepository) :
    IUserRepository {
    companion object {
        private const val LOG_TAG = "HobbyApp.UserRepository"
        const val USERS_COLLECTION = "users"
        private const val FRIEND_REQUEST_COLLECTION = "friend_requests"
        private const val USER_FRIENDS_COLLECTION = "user_friends"
        private const val ADMIN_ROLES_FILE = "admins/roles"
    }

    override suspend fun getUserData(uid: String): UserData? {
        val database = Firebase.firestore
        val task = database.collection(USERS_COLLECTION).whereEqualTo(USER_ID.fieldName, uid).get()
        return suspendCancellableCoroutine<UserData?> { continuation ->
            task
                .addOnSuccessListener {
                    val documents = it.documents
                    val userDoc = if (documents.isEmpty()) null else documents[0]
                    val userData = userDoc?.toObject(UserData::class.java)

                    continuation.resume(userData)
                }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
    }

    override suspend fun getUserData(uids: List<String>): List<UserData> {
        if (uids.isEmpty()) return emptyList()

        // query docs where user id is in uids
        val database = Firebase.firestore
        val queryTask = database.collection(USERS_COLLECTION)
            .whereIn(USER_ID.fieldName, uids).get()

        return suspendCancellableCoroutine { continuation ->
            queryTask
                .addOnSuccessListener {
                    val result = mutableListOf<UserData>()
                    for (docSnapshot in it) {
                        val userData: UserData = docSnapshot.toObject(UserData::class.java)
                        result.add(userData)
                    }
                    continuation.resume(result)
                }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
    }

    override suspend fun getFriendRequests(uid: String): List<UserData> {
        val userIds = getFriendRequestsUserIds(uid)
        if (userIds.isEmpty()) return emptyList()

        return suspendCancellableCoroutine { continuation ->
            val database = Firebase.firestore
            val queryTask = database.collection(USERS_COLLECTION)
                .whereIn(USER_ID.fieldName, userIds).get()

            queryTask
                .addOnSuccessListener {
                    val users = mutableListOf<UserData>()
                    for (doc in it) {
                        val userData = doc.toObject(UserData::class.java)
                        users.add(userData)
                    }
                    continuation.resume(users)
                }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
    }

    override suspend fun isUserAdmin(uid: String): Boolean {
        val database = Firebase.firestore
        val getIsAdminTask = database.document(ADMIN_ROLES_FILE).get()
        return suspendCancellableCoroutine { continuation ->
            getIsAdminTask
                .addOnSuccessListener { continuation.resume(true) }
                .addOnFailureListener { exception ->
                    if (exception is FirebaseFirestoreException) {
                        if (exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
                            continuation.resume(false)
                    } else {
                        Log.e(LOG_TAG, exception.message ?: exception.stackTraceToString())
                        continuation.resumeWithException(exception)
                    }
                }
                .addOnCanceledListener { continuation.cancel() }
        }
    }

    private suspend fun getFriendRequestsUserIds(uid: String): List<String> {
        val database = Firebase.firestore
        val queryTask = database.collection(FRIEND_REQUEST_COLLECTION)
            .whereEqualTo(TO_USER.fieldName, uid).get()

        return suspendCancellableCoroutine { continuation ->
            queryTask.addOnSuccessListener {
                val requestUsersIds = mutableListOf<String>()
                for (requestDoc in it) {
                    val fromUserId = requestDoc.data[FROM_USER.fieldName] as String
                    requestUsersIds.add(fromUserId)
                }
                continuation.resume(requestUsersIds)
            }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
    }

    override suspend fun registerUser() {
        val currentUser = userAuthRepository.getCurrentUser() ?: throw NoUserSignedInException()
        val name = currentUser.displayName ?: "Anonymous"
        val userId = currentUser.uid
        val email = currentUser.email ?: ""

        return createUserDataFiles(userId, name, email)
    }

    override suspend fun verifyEmail(user: FirebaseUser) {
        val sendVerificationEmailTask = user.sendEmailVerification()
        sendVerificationEmailTask.await()
    }

    private suspend fun createUserDataFiles(userId: String, name: String, email: String) {
        val userData = UserData(userId, name, name, email)
        val userFriendsData = mapOf(FRIENDS_DOC_UID.fieldName to userId)
        val database = Firebase.firestore
        val newUserDocument = database.collection(USERS_COLLECTION).document()
        val newUserFriendsDocument = database.collection(USER_FRIENDS_COLLECTION).document()

        return suspendCancellableCoroutine { continuation ->
            database.runTransaction { transaction ->
                transaction.set(newUserDocument, userData)
                transaction.set(newUserFriendsDocument, userFriendsData)
            }
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
    }

    override suspend fun updateUserData(userData: UserData) {
        val data = mapOf(
            USER_ID.fieldName to userData.userId,
            USERNAME.fieldName to userData.username,
            NAME.fieldName to userData.name,
            EMAIL.fieldName to userData.email
        )
        val database = Firebase.firestore
        return suspendCancellableCoroutine { continuation ->
            val getDocTask = database.collection(USERS_COLLECTION)
                .whereEqualTo(USER_ID.fieldName, userData.userId).get()
            getDocTask
                .addOnSuccessListener {
                    if (it.documents.size > 0) {
                        val docRef = it.documents[0].reference
                        database.runTransaction { transaction ->
                            transaction.update(docRef, data)
                        }
                            .addOnSuccessListener { continuation.resume(Unit) }
                            .addOnNoSuccessListeners(continuation, LOG_TAG)
                    } else continuation.resumeWithException(NoDataFoundException())
                }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
    }

    override suspend fun sendFriendRequestToUser(uid: String): Boolean {
        val currentUser = userAuthRepository.getCurrentUser() ?: throw NoUserSignedInException()

        if (currentUser.uid == uid) {
            // throw an exception when the user is trying to add themself to friends
            throw ForbiddenActionException()
        }
        // check if there is already an invite pending
        val thisRequestWasAlreadySent = doesFriendRequestExist(currentUser.uid, uid)
        if (thisRequestWasAlreadySent) throw FriendRequestExistsException()
        // check if user is already a friend
        val isUserInFriends = areUsersFriends(currentUser.uid, uid)
        if (isUserInFriends) throw UserAlreadyInFriendsException()

        return suspendCancellableCoroutine { continuation ->

            val database = Firebase.firestore
            val newDoc = database.collection(FRIEND_REQUEST_COLLECTION)
                .document()
            val data = mapOf(FROM_USER.fieldName to currentUser.uid, TO_USER.fieldName to uid)
            newDoc.set(data)
                .addOnSuccessListener { continuation.resume(true) }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
    }

    private suspend fun areUsersFriends(currentUserId: String, otherUserId: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val database = Firebase.firestore
            val queryTask = database.collection(USER_FRIENDS_COLLECTION)
                .whereEqualTo(FRIENDS_DOC_UID.fieldName, currentUserId)
                .whereArrayContains(FRIENDS_IDS.fieldName, otherUserId)
                .get()
            queryTask.addOnSuccessListener {
                if (it.documents.isNotEmpty()) continuation.resume(true)
                else continuation.resume(false)
            }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
    }

    override suspend fun acceptOrRejectFriendRequest(
        fromUserId: String,
        isRequestAccepted: Boolean
    ) {
        val database = Firebase.firestore
        val currentUserId =
            userAuthRepository.getCurrentUser()?.uid ?: throw NoUserSignedInException()

        // get references to docs for transaction
        val getRequestDocRefTask = database.collection(FRIEND_REQUEST_COLLECTION)
            .whereEqualTo(TO_USER.fieldName, currentUserId)
            .whereEqualTo(FROM_USER.fieldName, fromUserId)
            .get()
        val friendRequestDocRef =
            getDocRef(getRequestDocRefTask) ?: throw DataUnavailableException()

        val getUserFriendsDocTask = database.collection(USER_FRIENDS_COLLECTION)
            .whereEqualTo(FRIENDS_DOC_UID.fieldName, currentUserId).get()
        val userFriendsDocRef = getDocRef(getUserFriendsDocTask) ?: throw DataUnavailableException()

        val getOtherUserFriendsDocTask = database.collection(USER_FRIENDS_COLLECTION)
            .whereEqualTo(FRIENDS_DOC_UID.fieldName, fromUserId).get()
        val otherUserFriendsDocRef =
            getDocRef(getOtherUserFriendsDocTask) ?: throw DataUnavailableException()

        return suspendCancellableCoroutine { continuation ->
            database.runTransaction { transaction ->
                if (isRequestAccepted) {
                    // prepare data to update
                    val userFriendsDoc = transaction.get(userFriendsDocRef)
                    var friends = userFriendsDoc.get(FRIENDS_IDS.fieldName) as List<String>?
                    if (friends != null) {
                        friends = friends.plus(fromUserId)
                    } else friends = listOf(fromUserId)
                    val otherUserFriendsDoc = transaction.get(otherUserFriendsDocRef)
                    var friends2 = otherUserFriendsDoc.get(FRIENDS_IDS.fieldName) as List<String>?
                    if (friends2 != null) {
                        friends2 = friends2.plus(currentUserId)
                    } else friends2 = listOf(currentUserId)

                    // add friends
                    transaction.update(userFriendsDocRef, FRIENDS_IDS.fieldName, friends)
                    transaction.update(otherUserFriendsDocRef, FRIENDS_IDS.fieldName, friends2)
                }
                transaction.delete(friendRequestDocRef)
            }
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
    }

    private suspend fun doesFriendRequestExist(fromUserId: String, toUserId: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            val database = Firebase.firestore
            val queryTask = database.collection(FRIEND_REQUEST_COLLECTION)
                .whereEqualTo(TO_USER.fieldName, toUserId)
                .whereEqualTo(FROM_USER.fieldName, fromUserId)
                .get()
            queryTask.addOnSuccessListener {
                if (it.documents.isEmpty()) continuation.resume(false)
                else continuation.resume(true)
            }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }

    private suspend fun getDocRef(queryTask: Task<QuerySnapshot>): DocumentReference? =
        suspendCancellableCoroutine { continuation ->
            queryTask
                .addOnSuccessListener {
                    if (it.documents.size > 0) {
                        val docRef = it.documents[0].reference
                        continuation.resume(docRef)
                    } else continuation.resume(null)
                }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
}
