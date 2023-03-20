package pl.com.britenet.hobbyapp.data.location

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import pl.com.britenet.hobbyapp.data.IHobbiesRepository
import pl.com.britenet.hobbyapp.data.firestore.UserDocFields
import pl.com.britenet.hobbyapp.exceptions.DataUnavailableException
import pl.com.britenet.hobbyapp.exceptions.NoUserSignedInException
import pl.com.britenet.hobbyapp.utils.addOnNoSuccessListeners
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationRepository @Inject constructor(private val hobbiesRepo: IHobbiesRepository) :
    ILocationRepository {
    companion object {
        const val LOG_TAG = "HobbyApp.LocationRepository"
        const val LOCATION_COLLECTION = "locations"
        const val USER_COLLECTION = "users"
    }

    override suspend fun saveLocation(
        geoData: GeoData
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: throw NoUserSignedInException()
        val database = Firebase.firestore

        // get username document from userId
        val userDocQuery = database.collection(USER_COLLECTION)
            .whereEqualTo(UserDocFields.USER_ID.fieldName, userId).get().await()
        if (userDocQuery.isEmpty) throw DataUnavailableException()
        val userDocRef = userDocQuery.documents[0].reference

        // get document with user's uid or create a new one
        val locationDocQuery = database.collection(LOCATION_COLLECTION)
            .whereEqualTo("userId", userId).get().await()
        val newLocDoc: DocumentReference
        if (locationDocQuery.isEmpty) newLocDoc =
            database.collection(LOCATION_COLLECTION).document()
        else newLocDoc = locationDocQuery.documents[0].reference

        // get user's hobbies
        val hobbies = hobbiesRepo.getUserHobbies(userId)

        return suspendCancellableCoroutine { continuation ->
            val transaction = database.runTransaction {
                val username = it.get(userDocRef).get(UserDocFields.USERNAME.fieldName) as String
                val data = UserGeoData(
                    username,
                    userId,
                    geoData.latitude,
                    geoData.longitude,
                    geoData.cityName,
                    geoData.countryName,
                    hobbies
                )
                it.set(newLocDoc, data)
            }
            transaction
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
    }

    override suspend fun deleteLocation() {
        val database = Firebase.firestore
        val userId = Firebase.auth.uid
        val query = database.collection(LOCATION_COLLECTION)
            .whereEqualTo("userId", userId).get().await()
        val doc = if (query.isEmpty) null else query.documents[0]
        val docRef = doc?.reference
        docRef?.delete()?.await()
    }

    override suspend fun getAllLocations(): List<UserGeoData> {
        val database = Firebase.firestore
        val locations = mutableListOf<UserGeoData>()
        return suspendCancellableCoroutine { continuation ->
            val task = database.collection(LOCATION_COLLECTION).get()
            task.addOnSuccessListener {
                for (doc in it.documents) {
                    val data = doc.toObject(UserGeoData::class.java)
                    if (data != null) {
                        locations.add(data)
                    }
                }

                Log.i("HobbyApp", locations.toString())
                continuation.resume(locations)
            }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
    }
}