package pl.com.britenet.hobbyapp.exceptions

class DataUnavailableException : Exception() {
    override val message: String = ExceptionMessages.DATA_UNAVAILABLE.message
}
