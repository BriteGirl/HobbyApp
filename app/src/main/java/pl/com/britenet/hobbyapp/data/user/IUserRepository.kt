package pl.com.britenet.hobbyapp.data.user

import com.google.firebase.auth.FirebaseUser

interface IUserRepository {
    suspend fun getUserData(uid: String): UserData?
    suspend fun getUserData(uids: List<String>): List<UserData>
    suspend fun getFriendRequests(uid: String): List<UserData>
    suspend fun isUserAdmin(uid: String): Boolean
    suspend fun registerUser()
    suspend fun verifyEmail(user: FirebaseUser)
    suspend fun updateUserData(userData: UserData)
    suspend fun sendFriendRequestToUser(uid: String): Boolean
    suspend fun acceptOrRejectFriendRequest(fromUserId: String, isRequestAccepted: Boolean)
}
