package pl.com.britenet.hobbyapp.data.firestore

enum class HobbySuggestionDocFields(val fieldName: String) {
    NAME("name"),
    USER_ID("creatorUid"),
    IMAGE_NAME("imgName")
}
