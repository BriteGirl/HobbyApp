package pl.com.britenet.hobbyapp.exceptions

class UnidentifiedException : Exception() {
    override val message: String = ExceptionMessages.SOMETHING_WENT_WRONG.message
}
