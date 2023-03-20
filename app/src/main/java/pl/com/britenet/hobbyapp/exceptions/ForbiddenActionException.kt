package pl.com.britenet.hobbyapp.exceptions

class ForbiddenActionException : Exception() {
    override val message: String
        get() = ExceptionMessages.FORBIDDEN_ACTION.message
}
