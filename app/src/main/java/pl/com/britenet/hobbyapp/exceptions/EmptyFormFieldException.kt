package pl.com.britenet.hobbyapp.exceptions

class EmptyFormFieldException : Exception() {
    override val message: String
        get() = ExceptionMessages.FORM_FIELD_EMPTY.message
}
