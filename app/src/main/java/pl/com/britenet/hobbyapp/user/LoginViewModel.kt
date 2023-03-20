package pl.com.britenet.hobbyapp.user

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.com.britenet.hobbyapp.BuildConfig
import pl.com.britenet.hobbyapp.data.user.ILoginRepository
import pl.com.britenet.hobbyapp.data.user.IUserAuthRepository
import pl.com.britenet.hobbyapp.data.user.IUserRepository
import pl.com.britenet.hobbyapp.exceptions.EmptyLoginDataException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val userRepo: IUserRepository,
    private val userAuthRepository: IUserAuthRepository,
    private val loginRepository: ILoginRepository
) : AndroidViewModel(application) {
    companion object {
        private const val LOG_TAG = "HobbyApp.LoginViewModel"
        enum class Purpose { LOGIN, REGISTER }
        private val serverClientId: String = BuildConfig.SERVER_CLIENT_ID
        private val googleSignInOptions = GoogleSignInOptions.Builder()
            .requestIdToken(serverClientId)
            .requestEmail()
            .requestProfile()
            .build()
    }

    private val googleSingInClient =
        GoogleSignIn.getClient(application.applicationContext, googleSignInOptions)

    private var _purpose = MutableLiveData(Purpose.LOGIN)
    val purpose: LiveData<Purpose>
        get() = _purpose

    private var _loginException = MutableLiveData<Exception?>()
    val loginException: LiveData<Exception?>
        get() = _loginException

    private var _userLoggedIn = MutableLiveData<String?>()
    val userLoggedIn: LiveData<String?>
        get() = _userLoggedIn

    // if not null, tells the activity to start login or sign-up process with a provider
    private var _intentToStartForResult = MutableLiveData<Intent?>()
    val intentToStartForResult: LiveData<Intent?>
        get() = _intentToStartForResult

    init {
        // check for a signed-in user
        val currentUser = userAuthRepository.getCurrentUser()
        currentUser?.reload()?.addOnSuccessListener {
            _userLoggedIn.value = currentUser.displayName
        }
    }

    fun loginOrRegisterWithEmail(login: String, password: String) {
        if (login.isNotEmpty() && password.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                // register or log in
                val result: Task<AuthResult> = if (_purpose.value == Purpose.REGISTER) {
                    loginRepository.registerWithEmail(login, password)
                } else {
                    loginRepository.logInWithEmail(login, password)
                }
                // proceed when the action is complete
                onLoginOrRegisterComplete(result)
            }
        } else {
            // let the user know they didn't pass correct data
            _loginException.value = EmptyLoginDataException()
        }
    }

    fun loginOrRegisterWithGoogle() {
        _intentToStartForResult.value = googleSingInClient.signInIntent
    }

    private fun onLoginOrRegisterComplete(result: Task<AuthResult>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (result.isSuccessful) {
                val currentUser = userAuthRepository.getCurrentUser()
                var username: String? = null

                currentUser?.let {
                    username = currentUser.displayName
                    if (_purpose.value == Purpose.REGISTER) {
                        userRepo.verifyEmail(currentUser)
                        userRepo.registerUser()
                    }
                }
                _userLoggedIn.postValue(username)
            } else {
                _loginException.postValue(result.exception)
            }
        }
    }

    fun togglePurpose() {
        val currentPurpose = _purpose.value
        if (currentPurpose == Purpose.LOGIN) {
            _purpose.value = Purpose.REGISTER
        } else {
            _purpose.value = Purpose.LOGIN
        }
    }

    fun onErrorDisplayed() {
        _loginException.value = null
    }

    fun onLoginOrRegisterWithGoogleComplete(incomingIntent: Intent?) {
        _intentToStartForResult.value = null
        if (incomingIntent != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val loginResult = loginRepository.logInWithGoogle(incomingIntent)
                    onLoginOrRegisterComplete(loginResult)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e(LOG_TAG, e.toString())
                    _loginException.postValue(e)
                }
            }
        }
    }
}
