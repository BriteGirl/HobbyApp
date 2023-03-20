package pl.com.britenet.hobbyapp.exceptions

class FriendRequestExistsException : Exception() {
    override val message: String
        get() = ExceptionMessages.FRIEND_REQUEST_ALREADY_PENDING.message
}
