package pl.com.britenet.hobbyapp.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import pl.com.britenet.hobbyapp.data.firestore.HobbyDocFields
import pl.com.britenet.hobbyapp.data.firestore.UserHobbyAssociationDocFields

fun queryDocumentToHobby(document: QueryDocumentSnapshot): Hobby {
    val data = document.data
    val hobbyId = document.id
    return getHobbyFromData(hobbyId, data)
}

fun documentToHobby(document: DocumentSnapshot): Hobby? {
    if (document.exists()) {
        val data = document.data
        if (data != null) {
            return getHobbyFromData(document.id, data)
        }
    }
    return null
}

fun documentToUserHobbyAssociation(document: QueryDocumentSnapshot): UserHobbyAssociation {
    val data = document.data
    val docId = document.id
    val userId = data[UserHobbyAssociationDocFields.USER_ID.fieldName] as String
    val hobbyId = data[UserHobbyAssociationDocFields.HOBBY_ID.fieldName] as String

    return UserHobbyAssociation(docId, userId, hobbyId)
}

fun queryDocToUid(document: QueryDocumentSnapshot): String? {
    val data = document.data
    if (data.isNotEmpty()) return data[UserHobbyAssociationDocFields.USER_ID.fieldName] as String?
    return null
}

private fun getHobbyFromData(
    hobbyId: String,
    data: Map<String, Any>
): Hobby {
    val hobbyName: String = data[HobbyDocFields.NAME.fieldName] as String
    val imgName: String? = data[HobbyDocFields.IMAGE_NAME.fieldName] as String?

    return Hobby(hobbyId, hobbyName, imgName)
}