package pl.com.britenet.hobbyapp.exceptions

class NoUserSignedInException : Exception() {
    override val message: String
        get() = ExceptionMessages.NO_USER_SIGNED_IN.message
}
