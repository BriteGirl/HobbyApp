package pl.com.britenet.hobbyapp.data.admin

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import pl.com.britenet.hobbyapp.data.FirestoreHobbyDatabase.Companion.HOBBIES_COLLECTION
import pl.com.britenet.hobbyapp.data.FirestoreHobbyDatabase.Companion.HOBBIES_SUGGESTIONS_COLLECTION
import pl.com.britenet.hobbyapp.data.SuggestedHobby
import pl.com.britenet.hobbyapp.data.firestore.HobbyDocFields
import pl.com.britenet.hobbyapp.data.firestore.HobbySuggestionDocFields
import pl.com.britenet.hobbyapp.data.firestore.UserDocFields
import pl.com.britenet.hobbyapp.data.images.ImagesRepository
import pl.com.britenet.hobbyapp.data.user.UserRepository.Companion.USERS_COLLECTION
import pl.com.britenet.hobbyapp.utils.addOnNoSuccessListeners
import javax.inject.Inject
import kotlin.coroutines.resume

class AdminRepository @Inject constructor() : IAdminRepository {
    companion object {
        const val ADMINS_COLLECTION = "admins"
        const val ADMINS_ROLES_FILE = "roles"
        private const val LOG_TAG = "HobbyApp.AdminRepository"
    }

    override suspend fun getHobbySuggestions(): List<SuggestedHobby> {
        val getTask = Firebase.firestore.collection(HOBBIES_SUGGESTIONS_COLLECTION).get()
        return suspendCancellableCoroutine { continuation ->
            getTask.addOnSuccessListener {
                val list = it.toObjects(SuggestedHobby::class.java)
                continuation.resume(list)
            }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
    }

    override suspend fun acceptOrRejectHobbySuggestions(
        areItemsAccepted: Boolean,
        hobbies: List<SuggestedHobby>
    ) {
        val database = Firebase.firestore
        // get references to old files to use in transactions
        val getTask = database.collection(HOBBIES_SUGGESTIONS_COLLECTION)
            .whereIn(FieldPath.documentId(), hobbies.map { it.id }).get()
        val oldDocRefs: List<DocumentReference> = getDocReferences(getTask)

        if (!areItemsAccepted) {
            deleteSuggestedHobbies(hobbies, oldDocRefs)
            return
        }

        // move images
        for (hobby in hobbies) {
            acceptSuggestedHobbyImage(hobby)
        }

        // create multiple documents to store data in
        val newDocRefs = mutableListOf<DocumentReference>()
        for (num in 1..hobbies.size) {
            val docRef = database.collection(HOBBIES_COLLECTION).document()
            newDocRefs.add(docRef)
        }

        // move data from suggestions collection to hobbies collection
        database.runTransaction { transaction ->
            for (hobbyNum in 1..hobbies.size) {
                val hobby = hobbies[hobbyNum - 1]
                val data = mutableMapOf(HobbyDocFields.NAME.fieldName to hobby.name)
                if (hobby.imgName != null)
                    data[HobbyDocFields.IMAGE_NAME.fieldName] = hobby.imgName!!
                data[HobbySuggestionDocFields.USER_ID.fieldName] = hobby.creatorUid
                transaction.set(newDocRefs[hobbyNum - 1], data)
                transaction.delete(oldDocRefs[hobbyNum - 1])
            }
        }.await()
    }

    override suspend fun getAdmins(): List<AdminData> {
        // get admins' user ids
        val database = Firebase.firestore
        val adminRolesDoc = database.collection(ADMINS_COLLECTION)
            .document(ADMINS_ROLES_FILE).get().await()
        val admins = rolesDocumentToAdminData(adminRolesDoc)

        // get UserData for admins
        val adminUids = admins.map { adminData -> adminData.userId }
        val getTask = database
            .collection(USERS_COLLECTION).whereIn(UserDocFields.USER_ID.fieldName, adminUids).get()
        return suspendCancellableCoroutine { continuation ->
            getTask.addOnSuccessListener {
                val resultList = mutableListOf<AdminData>()
                val docs = it.documents
                for (doc in it) {
                    val docData = doc.data
                    val uid = docData.get(UserDocFields.USER_ID.fieldName) as String
                    val admin = admins.first { adminData -> adminData.userId == uid }
                    val adminData = userDataDocToAdminData(doc, admin)
                    resultList.add(adminData)
                }
                continuation.resume(resultList)
            }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
    }

    private suspend fun deleteSuggestedHobbies(
        hobbies: List<SuggestedHobby>,
        oldDocRefs: List<DocumentReference>
    ) {
        val database = Firebase.firestore
        // delete all documents with images if they're rejected
        for (suggestion in hobbies) {
            val imageName = suggestion.imgName
            if (imageName != null) {
                val imgRef = Firebase.storage.reference
                    .child(ImagesRepository.ImageFolder.SUGGESTIONS_FOLDER.path)
                    .child(imageName)
                deleteImage(imgRef)
            }
        }
        database.runTransaction { transaction ->
            for (doc in oldDocRefs) transaction.delete(doc)
        }.await()
    }

    private suspend fun acceptSuggestedHobbyImage(hobby: SuggestedHobby) {
        val imageName = hobby.imgName
        if (imageName != null) {
            val imgRef = Firebase.storage.reference
                .child(ImagesRepository.ImageFolder.SUGGESTIONS_FOLDER.path)
                .child(imageName)
            val newImgRef = Firebase.storage.reference
                .child(ImagesRepository.ImageFolder.IMAGES_FOLDER.path).child(imageName)
            val hobbyImgStreamTask = imgRef.stream.await()
            newImgRef.putStream(hobbyImgStreamTask.stream).await()
            // update image metadata with creator's user id
            val metaData = storageMetadata {
                setCustomMetadata(HobbySuggestionDocFields.USER_ID.fieldName, hobby.creatorUid)
            }
            newImgRef.updateMetadata(metaData).await()
            deleteImage(imgRef)
        }
    }

    private suspend fun deleteImage(imgReference: StorageReference) = imgReference.delete().await()

    private suspend fun getDocReferences(task: Task<QuerySnapshot>): List<DocumentReference> =
        suspendCancellableCoroutine { continuation ->
            task.addOnSuccessListener {
                val refs = mutableListOf<DocumentReference>()
                for (doc in it) {
                    refs.add(doc.reference)
                }
                continuation.resume(refs)
            }
                .addOnNoSuccessListeners(continuation, LOG_TAG)
        }
}
