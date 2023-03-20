package pl.com.britenet.hobbyapp.data

class SuggestedHobby(
    id: String,
    name: String,
    val creatorUid: String,
    imgName: String? = null,
    isChecked: Boolean = false
) : Hobby(id, name, imgName, isChecked) {
    // this constructor is a default for Firestore deserialization
    constructor() : this("", "", "")

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is SuggestedHobby) {
            return super.equals(other)
        }
        return super.equals(other) && creatorUid == other.creatorUid
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + creatorUid.hashCode()
        return result
    }
}
