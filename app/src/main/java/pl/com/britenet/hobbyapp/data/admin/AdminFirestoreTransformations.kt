package pl.com.britenet.hobbyapp.data.admin

import com.google.firebase.firestore.DocumentSnapshot
import pl.com.britenet.hobbyapp.data.firestore.UserDocFields

fun rolesDocumentToAdminData(doc: DocumentSnapshot): List<AdminData> {
    val data = doc.data ?: return emptyList()
    val adminRoles = data.keys
    val result = mutableListOf<AdminData>()
    for (roleKey in adminRoles) {
        val adminRole = AdminRole.roleOf(roleKey)
        if (adminRole != null) {
            val adminUids = data[roleKey] as List<String>
            for (uid in adminUids) {
                val adminData = AdminData(uid, adminRole, null, null, "")
                result.add(adminData)
            }
        }
    }
    return result
}

fun userDataDocToAdminData(document: DocumentSnapshot, adminData: AdminData): AdminData {
    val data = document.data ?: return adminData
    val name = data[UserDocFields.NAME.fieldName] as String?
    val username = data[UserDocFields.USERNAME.fieldName] as String?
    val email = data[UserDocFields.EMAIL.fieldName] as String

    adminData.username = username
    adminData.name = name
    adminData.email = email

    return adminData
}
