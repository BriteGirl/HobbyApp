package pl.com.britenet.hobbyapp.hobby

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import pl.com.britenet.hobbyapp.data.IHobbiesRepository
import pl.com.britenet.hobbyapp.data.user.IUserAuthRepository
import pl.com.britenet.hobbyapp.data.user.IUserRepository
import pl.com.britenet.hobbyapp.data.user.UserData
import pl.com.britenet.hobbyapp.exceptions.DataUnavailableException
import pl.com.britenet.hobbyapp.exceptions.NoUserSignedInException
import javax.inject.Inject

@HiltViewModel
class HobbyDetailsViewModel @Inject constructor(
    stateHandle: SavedStateHandle,
    private val hobbyRepository: IHobbiesRepository,
    private val userRepository: IUserRepository,
    userAuthRepository: IUserAuthRepository
) : ViewModel() {

    companion object {
        const val HOBBY_ID_EXTRA = "HOBBY_ID"
        private const val LOG_TAG = "HobbyDetailsViewModel"
    }

    private lateinit var hobbyId: String

    private val _hobbyName = MutableLiveData<String>()
    val hobbyName: LiveData<String>
        get() = _hobbyName

    private val _hobbyImageName = MutableLiveData<String?>()
    val hobbyImageName: LiveData<String?>
        get() = _hobbyImageName

    private val _isFavourite = MutableLiveData<Boolean?>()
    val isFavourite: LiveData<Boolean?>
        get() = _isFavourite

    private val _users = MutableLiveData<List<UserData>?>()
    val users: LiveData<List<UserData>?>
        get() = _users

    private val _exceptionToShow = MutableLiveData<String?>()
    val exceptionToShow: LiveData<String?>
        get() = _exceptionToShow

    private val _isFriendRequestSent = MutableLiveData<Boolean?>()
    val isFriendRequestSent: LiveData<Boolean?>
        get() = _isFriendRequestSent

    private val _isUserLoggedIn = MutableLiveData<Boolean>(false)
    val isUserLoggedIn: LiveData<Boolean>
        get() = _isUserLoggedIn

    init {
        _isUserLoggedIn.value = userAuthRepository.getCurrentUser() != null

        val chosenHobbyId = stateHandle.get<String>(HOBBY_ID_EXTRA)
        if (chosenHobbyId == null) onHobbyUnavailable()
        else {
            hobbyId = chosenHobbyId
            updateData()
        }
    }

    private fun updateData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // get hobby details
                val hobby = hobbyRepository.getHobbyDetails(hobbyId)
                if (hobby != null) {
                    _hobbyName.postValue(hobby.name)
                    _hobbyImageName.postValue(hobby.imgName)
                } else _exceptionToShow.postValue(DataUnavailableException().message)

                // is hobby favourite?
                val isFavourite = hobbyRepository.checkIsHobbyFavourite(hobbyId)
                _isFavourite.postValue(isFavourite)

                if (_isUserLoggedIn.value != false) {
                    // get users interested in this hobby (feature for logged in users only)
                    try {
                        val userIds = hobbyRepository.getUserIdsByHobby(hobbyId)
                        val users = userRepository.getUserData(userIds)
                        _users.postValue(users)
                    } catch (e: FirebaseFirestoreException) {
                        _exceptionToShow.postValue(DataUnavailableException().message)
                    }
                }
            }
        }
    }

    fun onExceptionShowed() {
        _exceptionToShow.value = null
    }

    private fun onHobbyUnavailable() {
        val exception = DataUnavailableException()
        _exceptionToShow.value = exception.message
        Log.e(LOG_TAG, exception.message, exception)
    }

    fun onFavouriteBtnClicked() {
        val isFav = _isFavourite.value
        if (isFav != null) {
            _isFavourite.value = !isFav
        } else _isFavourite.value = true
    }

    fun onActivityPause() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // update user's hobby status
                isFavourite.value?.let { hobbyRepository.updateHobbyIsFavourite(hobbyId, it) }
            } catch (e: NoUserSignedInException) {
                // do nothing if there is no user signed in
                return@launch
            }
        }
    }

    fun onAddFriendClicked(requestedUserId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isRequestSent = userRepository.sendFriendRequestToUser(requestedUserId)
                _isFriendRequestSent.postValue(isRequestSent)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _exceptionToShow.postValue(e.message)
            }
        }
    }

    fun onRequestSentMessageShowed() {
        _isFriendRequestSent.value = null
    }
}
