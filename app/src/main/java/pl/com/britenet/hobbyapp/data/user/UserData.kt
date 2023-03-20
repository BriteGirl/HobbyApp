package pl.com.britenet.hobbyapp.data.user

import pl.com.britenet.hobbyapp.data.Hobby

open class UserData(
    var userId: String,
    var name: String?,
    var username: String?,
    var email: String,
    var userHobbies: List<Hobby>? = listOf()
) {

    constructor() : this("", "", "", "")

    override fun equals(other: Any?): Boolean {
        if (other !is UserData) return false
        return userId == other.userId &&
                name == other.name &&
                username == other.username &&
                email == other.email
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (username?.hashCode() ?: 0)
        result = 31 * result + email.hashCode()
        return result
    }
}
