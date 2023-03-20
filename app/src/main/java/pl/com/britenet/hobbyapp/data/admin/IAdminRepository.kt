package pl.com.britenet.hobbyapp.data.admin

import pl.com.britenet.hobbyapp.data.SuggestedHobby

interface IAdminRepository {
    suspend fun getHobbySuggestions(): List<SuggestedHobby>
    suspend fun acceptOrRejectHobbySuggestions(areItemsAccepted: Boolean, hobbies: List<SuggestedHobby>)
    suspend fun getAdmins(): List<AdminData>
}
