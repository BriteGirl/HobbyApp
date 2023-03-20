package pl.com.britenet.hobbyapp.data.user

import com.google.firebase.firestore.DocumentSnapshot
import pl.com.britenet.hobbyapp.data.firestore.UserDocFields.*

fun documentToUserData(doc: DocumentSnapshot): UserData? {
    val data = doc.data
    return if (data != null) {
        val userId = data[USER_ID.fieldName] as String
        val name = data[NAME.fieldName] as String?
        val username = data[USERNAME.fieldName] as String?
        val email = data[EMAIL.fieldName] as String

        UserData(userId, name, username, email)
    } else null
}
