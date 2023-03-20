package pl.com.britenet.hobbyapp.data

import android.graphics.Bitmap

interface IHobbiesRepository {
    suspend fun getAllHobbies(): List<Hobby>
    suspend fun getUserHobbies(userId: String): List<Hobby>
    suspend fun getUserIdsByHobby(hobbyId: String): List<String>
    suspend fun getHobbyDetails(hobbyId: String): Hobby?
    suspend fun saveNewHobby(name: String, imgBitmap: Bitmap? = null, fileType: String? = null): Boolean
    suspend fun updateHobbyIsFavourite(hobbyId: String, isFavourite: Boolean)
    suspend fun checkIsHobbyFavourite(hobbyId: String): Boolean
}
