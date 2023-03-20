package pl.com.britenet.hobbyapp.exceptions

class IneligibleMarkerException : Exception() {
    override val message: String
        get() = ExceptionMessages.MARKER_NOT_ELIGIBLE.message
}
