package pl.com.britenet.hobbyapp.data.admin

import pl.com.britenet.hobbyapp.data.user.UserData

class AdminData(userId: String, var adminRole: AdminRole, name: String?, username: String?, email: String) :
    UserData(userId, name, username, email) {
    override fun equals(other: Any?): Boolean {
        return if (other !is AdminData) super.equals(other)
        else super.equals(other) && adminRole == other.adminRole
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + adminRole.hashCode()
        return result
    }
}
