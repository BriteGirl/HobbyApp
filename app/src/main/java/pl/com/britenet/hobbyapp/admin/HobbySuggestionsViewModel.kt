package pl.com.britenet.hobbyapp.admin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.com.britenet.hobbyapp.data.Hobby
import pl.com.britenet.hobbyapp.data.SuggestedHobby
import pl.com.britenet.hobbyapp.data.admin.IAdminRepository
import javax.inject.Inject

@HiltViewModel
class HobbySuggestionsViewModel @Inject constructor(private val hobbiesRepo: IAdminRepository) : ViewModel() {
    private companion object {
        val LOG_TAG = "HobbyApp.HobbySuggestionsViewModel"
    }

    private val _hobbies = MutableLiveData<List<SuggestedHobby>?>()
    val hobbies: LiveData<List<SuggestedHobby>?>
        get() = _hobbies

    private val _exceptionToShow = MutableLiveData<String?>()
    val exceptionToShow: LiveData<String?>
        get() = _exceptionToShow

    init {
        viewModelScope.launch(Dispatchers.IO) { updateData() }
    }

    private suspend fun updateData() {
        try {
            val hobbies = hobbiesRepo.getHobbySuggestions()
            _hobbies.postValue(hobbies)
        } catch (e: FirebaseFirestoreException) {
            _exceptionToShow.postValue(e.message)
        }
    }

    fun acceptOrRejectHobbySuggestions(areItemsAccepted: Boolean, hobbies: List<Hobby>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                hobbiesRepo.acceptOrRejectHobbySuggestions(
                    areItemsAccepted,
                    hobbies as List<SuggestedHobby>
                )
                updateData()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(LOG_TAG, e.message.orEmpty())
                _exceptionToShow.postValue(e.message)
            }
        }
    }

    fun onExceptionShowed() {
        _exceptionToShow.value = null
    }
}
