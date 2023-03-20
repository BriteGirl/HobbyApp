package pl.com.britenet.hobbyapp.userhobbies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.com.britenet.hobbyapp.data.Hobby
import pl.com.britenet.hobbyapp.data.IHobbiesRepository
import pl.com.britenet.hobbyapp.data.user.IUserAuthRepository
import javax.inject.Inject

@HiltViewModel
class UserHobbiesViewModel @Inject constructor(
    private val hobbiesDB: IHobbiesRepository,
    private val userAuthRepository: IUserAuthRepository
) : ViewModel() {

    private val _userHobbies = MutableLiveData<List<Hobby>>()
    val userHobbies: LiveData<List<Hobby>>
        get() = _userHobbies

    private val _isUserSignedIn = MutableLiveData<Boolean>(false)
    val isUserSignedIn: LiveData<Boolean>
        get() = _isUserSignedIn

    init {
        checkForUser()
        updateHobbies()
    }

    private fun checkForUser() {
        _isUserSignedIn.value = userAuthRepository.getCurrentUser() != null
    }

    private fun updateHobbies() {
        val currentUser = userAuthRepository.getCurrentUser()
        if (currentUser == null) {
            _isUserSignedIn.value = false
            return
        }
        viewModelScope.launch {
            val userHobbies = withContext(Dispatchers.IO) {
                hobbiesDB.getUserHobbies(currentUser.uid)
            }
            _userHobbies.value = userHobbies
        }
    }
}
