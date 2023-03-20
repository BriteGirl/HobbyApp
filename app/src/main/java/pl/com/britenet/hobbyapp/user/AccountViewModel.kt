package pl.com.britenet.hobbyapp.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.com.britenet.hobbyapp.R
import pl.com.britenet.hobbyapp.data.location.*
import pl.com.britenet.hobbyapp.data.user.IUserAuthRepository
import pl.com.britenet.hobbyapp.data.user.IUserRepository
import pl.com.britenet.hobbyapp.data.user.UserData
import pl.com.britenet.hobbyapp.exceptions.NoUserSignedInException
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    val userRepo: IUserRepository,
    val userAuthRepository: IUserAuthRepository,
    val userLocationRepository: ILocationRepository
) : ViewModel() {
    private var _currentUser = MutableLiveData<UserData?>()
    val currentUser: LiveData<UserData?>
        get() = _currentUser

    private val _friendRequestingUsers = MutableLiveData<List<UserData>?>()
    val friendRequestingUsers: LiveData<List<UserData>?>
        get() = _friendRequestingUsers

    private val _isAdmin = MutableLiveData<Boolean>(false)
    val isAdmin: LiveData<Boolean>
        get() = _isAdmin

    private val _exceptionToShow = MutableLiveData<String?>()
    val exceptionToShow: LiveData<String?>
        get() = _exceptionToShow

    init {
        viewModelScope.launch {
            refreshUserData()
        }
    }

    private suspend fun refreshUserData() {
        val firebaseUser = userAuthRepository.getCurrentUser()
        if (firebaseUser == null) {
            _currentUser.value = null
        } else withContext(Dispatchers.IO) {
            val userData = userRepo.getUserData(firebaseUser.uid)
            if (userData == null) {
                userRepo.registerUser()
                refreshUserData()
            }
            _currentUser.postValue(userData)
            val friendRequestsUsers = userRepo.getFriendRequests(firebaseUser.uid)
            _friendRequestingUsers.postValue(friendRequestsUsers)

            val isUserAdmin = userRepo.isUserAdmin(userData!!.userId)
            _isAdmin.postValue(isUserAdmin)
        }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        _currentUser.value = null
    }

    fun onNewDataSubmitted(text: String, labelResId: Int) {
        val user = _currentUser.value
        if (user == null) {
            _exceptionToShow.value = NoUserSignedInException().message
            return
        }

        // change data depending on update requested
        when (labelResId) {
            R.string.enter_new_name -> user.name = text
            R.string.enter_new_username -> user.username = text
            R.string.enter_new_email -> user.email = text
        }
        updateUser(user)
    }

    fun onRequestAccepted(fromUserId: String, isRequestAccepted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userRepo.acceptOrRejectFriendRequest(fromUserId, isRequestAccepted)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _exceptionToShow.postValue(e.message)
                return@launch
            }

            // delete friend request from data because the database action was successful
            val data = _friendRequestingUsers.value!!
            val newData: List<UserData> = data.filter { userData -> userData.userId != fromUserId }
            _friendRequestingUsers.postValue(newData)
        }
    }

    fun onExceptionDisplayed() {
        _exceptionToShow.value = null
    }

    private fun updateUser(userData: UserData) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { userRepo.updateUserData(userData) }
            refreshUserData()
        }
    }

    fun saveLocation(geoData: GeoDataResult) {
        viewModelScope.launch(Dispatchers.IO) {
            when (geoData) {
                is GeoDataResult.GeoDataResultSet -> userLocationRepository.saveLocation(geoData.data)
                is GeoDataResult.GeoDataResultDelete -> userLocationRepository.deleteLocation()
                is GeoDataResult.GeoDataResultCancelled -> {}
            }
        }
    }
}
