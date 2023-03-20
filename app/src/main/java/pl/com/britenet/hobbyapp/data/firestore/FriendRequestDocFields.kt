package pl.com.britenet.hobbyapp.data.firestore

enum class FriendRequestDocFields(val fieldName: String) {
    FROM_USER("from_user_id"),
    TO_USER("to_user_id")
}
