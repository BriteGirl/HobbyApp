package pl.com.britenet.hobbyapp.exceptions

class EmptyLoginDataException : Exception() {
    override val message: String
        get() = ExceptionMessages.LOGIN_DATA_EMPTY.message
}
