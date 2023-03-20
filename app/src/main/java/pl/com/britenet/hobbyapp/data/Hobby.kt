package pl.com.britenet.hobbyapp.data

open class Hobby(
    var id: String,
    var name: String,
    var imgName: String? = null,
    var isChecked: Boolean = false
) {
    // this constructor is a default for Firestore deserialization
    constructor() : this("", "")

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Hobby) {
            return super.equals(other)
        }
        return id == other.id &&
                name == other.name &&
                imgName == other.imgName &&
                isChecked == other.isChecked
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (imgName?.hashCode() ?: 0)
        result = 31 * result + isChecked.hashCode()
        return result
    }
}
