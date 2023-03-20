package pl.com.britenet.hobbyapp.exceptions

class UserAlreadyInFriendsException : Exception() {
    override val message: String
        get() = ExceptionMessages.USER_ALREADY_IS_FRIEND.message
}
