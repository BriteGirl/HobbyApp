package pl.com.britenet.hobbyapp.data.firestore

enum class UserDocFields(val fieldName: String) {
    USER_ID("userId"),
    NAME("name"),
    USERNAME("username"),
    EMAIL("email"),
    USER_HOBBIES("userHobbies")
}
