package pl.com.britenet.hobbyapp.data

import android.graphics.Bitmap
import pl.com.britenet.hobbyapp.data.user.IUserAuthRepository
import pl.com.britenet.hobbyapp.exceptions.EmptyFormFieldException
import pl.com.britenet.hobbyapp.exceptions.NoUserSignedInException
import javax.inject.Inject

class HobbiesRepository @Inject constructor(
    private val userAuthRepository: IUserAuthRepository,
    private val hobbyDatabase: IHobbyDatabase
) : IHobbiesRepository {
    companion object {
        const val LOG_TAG = "HobbyApp.HobbiesRepository"
    }

    override suspend fun getAllHobbies(): List<Hobby> {
        return hobbyDatabase.getAllHobbies()
    }

    override suspend fun getUserHobbies(userId: String): List<Hobby> {
        val associativeData = hobbyDatabase.getUserHobbiesAssociations(userId)

        if (associativeData.isEmpty()) return emptyList()
        return hobbyDatabase.getUserHobbies(associativeData)
    }

    override suspend fun getUserIdsByHobby(hobbyId: String): List<String> {
        return hobbyDatabase.getUserIdsByHobbyId(hobbyId)
    }

    override suspend fun getHobbyDetails(hobbyId: String): Hobby? {
        return hobbyDatabase.getHobbyById(hobbyId)
    }

    override suspend fun saveNewHobby(name: String, imgBitmap: Bitmap?, fileType: String?): Boolean {
        // do not proceed if the hobby name is empty or if user is not logged in
        val currentUser = userAuthRepository.getCurrentUser() ?: throw NoUserSignedInException()
        if (name.isEmpty()) throw EmptyFormFieldException()

        val newSuggestedHobby = SuggestedHobby("", name, currentUser.uid)
        val isImagePicked = !(imgBitmap == null || fileType.isNullOrEmpty())

        // add image name depending on the image existence
        if (isImagePicked) {
            val imgFileName = name.lowercase().replace(' ', '_', true)
                .plus(".").plus(fileType)
            newSuggestedHobby.imgName = imgFileName
        }

        return hobbyDatabase.addHobbySuggestion(newSuggestedHobby, imgBitmap, fileType)
    }

    override suspend fun updateHobbyIsFavourite(hobbyId: String, isFavourite: Boolean) {
        val currentUserId = userAuthRepository.getCurrentUser()?.uid
            ?: throw NoUserSignedInException()
        return hobbyDatabase.updateHobbyIsFavourite(hobbyId, isFavourite, currentUserId)
    }

    override suspend fun checkIsHobbyFavourite(hobbyId: String): Boolean {
        val currentUser = userAuthRepository.getCurrentUser()?.uid ?: return false
        val associationDoc = hobbyDatabase.getUserHobbyAssociationDoc(currentUser, hobbyId)
        return associationDoc != null
    }
}
