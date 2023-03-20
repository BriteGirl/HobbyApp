package pl.com.britenet.hobbyapp.exceptions

class NoDataFoundException : Exception() {
    override val message: String?
        get() = ExceptionMessages.NO_DATA_FOUND.message
}
