package pl.com.britenet.hobbyapp.data.admin

import pl.com.britenet.hobbyapp.data.firestore.AdminRolesDocFields

enum class AdminRole(val firestoreKey: String) {
    ADMIN(AdminRolesDocFields.ADMIN.fieldName);

    override fun toString(): String {
        val chars: MutableList<Char> = mutableListOf()
        for (letter in this.name.withIndex()) {
            var formattedLetter = letter.value
            if (letter.index == 0) {
                chars.add(formattedLetter)
                continue
            }
            if (!letter.value.isLetterOrDigit()) formattedLetter = ' '
            else formattedLetter = letter.value.lowercaseChar()
            chars.add(formattedLetter)
        }
        return chars.joinToString("")
    }

    companion object {
        fun roleOf(firestoreKey: String): AdminRole? {
            for (value in values()) {
                if (value.firestoreKey == firestoreKey)
                    return value
            }
            return null
        }
    }
}
