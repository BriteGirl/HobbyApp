package pl.com.britenet.hobbyapp.data.firestore

enum class UserHobbyAssociationDocFields(val fieldName: String) {
    USER_ID("user_id"),
    HOBBY_ID("hobby_id")
}