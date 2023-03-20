package pl.com.britenet.hobbyapp.exceptions

enum class ExceptionMessages(val message: String) {
    LOGIN_DATA_EMPTY("Login or password is empty!"),
    NO_USER_SIGNED_IN("No user is signed in."),
    DATA_UNAVAILABLE("Data is unavailable."),
    NO_DATA_FOUND("Data couldn't be found."),
    FORBIDDEN_ACTION("Sorry, can't do that!"),
    FRIEND_REQUEST_ALREADY_PENDING("This friend request is already pending."),
    USER_ALREADY_IS_FRIEND("This user is already your friend!"),
    SOMETHING_WENT_WRONG("We are sorry, something went wrong."),
    FORM_FIELD_EMPTY("Please fill out all necessary fields."),
    MARKER_NOT_ELIGIBLE("You cannot place your location here!"),
    NETWORK_ERROR("There is a problem with your network. Try again later.")
}
