package pl.com.britenet.hobbyapp.data.firestore

enum class UserFriendsDocFields(val fieldName: String) {
    USER_ID("user_id"),
    FRIENDS_IDS("friends_ids")
}
