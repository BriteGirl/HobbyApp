package pl.com.britenet.hobbyapp.data

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.suspendCancellableCoroutine
import pl.com.britenet.hobbyapp.data.firestore.UserHobbiesDocFields
import pl.com.britenet.hobbyapp.data.images.ImagesRepository
import pl.com.britenet.hobbyapp.utils.addOnNoSuccessListeners
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.coroutines.resume

class FirestoreHobbyDatabase @Inject constructor() : IHobbyDatabase {
    companion object {
        private const val LOG_TAG = "HobbyApp.DocumentDatabaseFirestore"
        const val HOBBIES_COLLECTION = "hobbies"
        const val HOBBIES_SUGGESTIONS_COLLECTION = "hobbies_suggestions"
        const val ASSOCIATIVE_COLLECTION = "user_hobbies"
    }

    override suspend fun addHobbySuggestion(
        hobbySuggestion: SuggestedHobby,
        imgBitmap: Bitmap?,
        fileType: String?
    ): Boolean =
        suspendCancellableCoroutine { continuation ->
            val isImagePicked = !(imgBitmap == null || fileType.isNullOrEmpty())
            val hobbiesDB = Firebase.firestore
            val newHobbyDoc = hobbiesDB.collection(HOBBIES_SUGGESTIONS_COLLECTION).document()
            hobbySuggestion.id = newHobbyDoc.id

            newHobbyDoc.set(hobbySuggestion)
                .addOnSuccessListener {
                    if (isImagePicked) {
                        val uploadTask = saveNewHobbyImage(imgBitmap!!, fileType!!, hobbySuggestion.imgName!!)
                        uploadTask.addOnSuccessListener {
                            continuation.resume(true)
                        }
                            .addOnNoSuccessListeners(continuation, LOG_TAG)
                    } else continuation.resume(true)
                }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }

    override suspend fun getAllHobbies(): List<Hobby> {
        val hobbiesDB = Firebase.firestore
        val getTask = hobbiesDB.collection(HOBBIES_COLLECTION).get()
        return createCoroutineHobbyReader(getTask, ::queryDocumentToHobby)
    }

    override suspend fun getUserHobbiesAssociations(userId: String): List<UserHobbyAssociation> {
        val hobbiesDB = Firebase.firestore
        val task = hobbiesDB.collection(ASSOCIATIVE_COLLECTION)
            .whereEqualTo(UserHobbiesDocFields.USER_ID.fieldName, userId).get()
        return createCoroutineAssociationReader(task, ::documentToUserHobbyAssociation)
    }

    override suspend fun getUserHobbies(associativeData: List<UserHobbyAssociation>): List<Hobby> {
        val hobbiesDB = Firebase.firestore
        val getHobbiesTask = hobbiesDB.collection(HOBBIES_COLLECTION)
            .whereIn(
                FieldPath.documentId(),
                associativeData.map { association -> association.hobbyId }
            )
            .get()
        return createCoroutineHobbyReader(getHobbiesTask, ::queryDocumentToHobby)
    }

    override suspend fun getUserIdsByHobbyId(hobbyId: String): List<String> {
        val database = Firebase.firestore
        val queryTask = database.collection(ASSOCIATIVE_COLLECTION)
            .whereEqualTo(UserHobbiesDocFields.HOBBY_ID.fieldName, hobbyId).get()
        return suspendCancellableCoroutine { continuation ->
            queryTask
                .addOnSuccessListener {
                    val userIds = mutableListOf<String>()
                    for (docSnapshot in it) {
                        val uid = queryDocToUid(docSnapshot)
                        if (uid != null) userIds.add(uid)
                    }
                    continuation.resume(userIds)
                }
                .addOnNoSuccessListeners(continuation, HobbiesRepository.LOG_TAG)
        }
    }

    override suspend fun getHobbyById(hobbyId: String): Hobby? {
        val hobbiesDB = Firebase.firestore
        val getTask = hobbiesDB.collection(HOBBIES_COLLECTION)
            .document(hobbyId).get()
        return createCoroutineHobbyDocReader(getTask, ::documentToHobby)
    }

    private fun saveNewHobbyImage(bitmap: Bitmap, fileType: String, imgFileName: String): UploadTask {
        val imgRef = Firebase.storage.reference
            .child(ImagesRepository.ImageFolder.SUGGESTIONS_FOLDER.path).child(imgFileName)

        // convert bitmap to byte array
        val bitmapCompressFormat = Bitmap.CompressFormat.valueOf(fileType.uppercase())
        val baos = ByteArrayOutputStream()
        bitmap.compress(bitmapCompressFormat, 100, baos)

        // save the image to Firebase Storage
        return imgRef.putStream(baos.toByteArray().inputStream())
    }

    override suspend fun getUserHobbyAssociationDoc(currentUserId: String, hobbyId: String): UserHobbyAssociation? {
        val hobbiesDB = Firebase.firestore
        val getAssociationDoc = hobbiesDB.collection(ASSOCIATIVE_COLLECTION)
            .whereEqualTo(UserHobbiesDocFields.USER_ID.fieldName, currentUserId)
            .whereEqualTo(UserHobbiesDocFields.HOBBY_ID.fieldName, hobbyId).get()
        val associationDocs =
            createCoroutineAssociationReader(getAssociationDoc, ::documentToUserHobbyAssociation)
        return if (associationDocs.isNotEmpty()) associationDocs[0] else null
    }

    override suspend fun updateHobbyIsFavourite(
        hobbyId: String,
        isFavourite: Boolean,
        currentUserId: String
    ) {
        val hobbiesDB = Firebase.firestore
        val associationDoc = getUserHobbyAssociationDoc(currentUserId, hobbyId)

        // if data is correct, no need to update it
        if ((associationDoc == null && !isFavourite) || (associationDoc != null && isFavourite)) return

        return suspendCancellableCoroutine { continuation ->
            hobbiesDB.runTransaction { transaction ->
                if (associationDoc != null) {
                    transaction.delete(
                        hobbiesDB.collection(ASSOCIATIVE_COLLECTION).document(associationDoc.docId)
                    )
                } else {
                    // create new association
                    val associationData = mapOf(
                        UserHobbiesDocFields.USER_ID.fieldName to currentUserId,
                        UserHobbiesDocFields.HOBBY_ID.fieldName to hobbyId
                    )
                    val newDocRef = hobbiesDB.collection(ASSOCIATIVE_COLLECTION).document()
                    transaction.set(newDocRef, associationData)
                }
            }
                .addOnCompleteListener { continuation.resume(Unit) }
                .addOnNoSuccessListeners(continuation, HobbiesRepository.LOG_TAG)
        }
    }

    private suspend fun createCoroutineHobbyReader(
        getTask: Task<QuerySnapshot>,
        docTransform: (QueryDocumentSnapshot) -> Hobby
    ): List<Hobby> =
        suspendCancellableCoroutine { continuation ->
            getTask
                .addOnSuccessListener { querySnapshot ->
                    val result: MutableList<Hobby> = mutableListOf()
                    for (document in querySnapshot) {
                        val hobby = docTransform(document)
                        result.add(hobby)
                    }
                    continuation.resume(result)
                }
                .addOnNoSuccessListeners(continuation, HobbiesRepository.LOG_TAG)
        }

    private suspend fun createCoroutineHobbyDocReader(
        getTask: Task<DocumentSnapshot>,
        docTransform: (DocumentSnapshot) -> Hobby?
    ): Hobby? {
        return suspendCancellableCoroutine { continuation ->
            getTask
                .addOnSuccessListener { document ->
                    val hobby = docTransform(document)
                    continuation.resume(hobby)
                }
                .addOnNoSuccessListeners(continuation, HobbiesRepository.LOG_TAG)
        }
    }

    private suspend fun createCoroutineAssociationReader(
        getTask: Task<QuerySnapshot>,
        docTransform: (QueryDocumentSnapshot) -> UserHobbyAssociation
    ): List<UserHobbyAssociation> {
        return suspendCancellableCoroutine { continuation ->
            getTask
                .addOnSuccessListener { querySnapshot ->
                    val result: MutableList<UserHobbyAssociation> = mutableListOf()
                    for (document in querySnapshot) {
                        val element = docTransform(document)
                        result.add(element)
                    }
                    continuation.resume(result)
                }
                .addOnNoSuccessListeners(continuation, HobbiesRepository.LOG_TAG)
        }
    }
}
