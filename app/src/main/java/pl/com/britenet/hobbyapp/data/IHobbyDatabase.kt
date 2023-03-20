package pl.com.britenet.hobbyapp.data

import android.graphics.Bitmap

interface IHobbyDatabase {
    suspend fun addHobbySuggestion(
        hobbySuggestion: SuggestedHobby,
        imgBitmap: Bitmap?,
        fileType: String?
    ): Boolean

    suspend fun getAllHobbies(): List<Hobby>
    suspend fun getUserHobbiesAssociations(userId: String): List<UserHobbyAssociation>
    suspend fun getUserHobbies(associativeData: List<UserHobbyAssociation>): List<Hobby>
    suspend fun getUserIdsByHobbyId(hobbyId: String): List<String>
    suspend fun getHobbyById(hobbyId: String): Hobby?
    suspend fun getUserHobbyAssociationDoc(currentUserId: String, hobbyId: String): UserHobbyAssociation?
    suspend fun updateHobbyIsFavourite(hobbyId: String, isFavourite: Boolean, currentUserId: String)
}
