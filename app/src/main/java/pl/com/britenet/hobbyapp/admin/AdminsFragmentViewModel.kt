package pl.com.britenet.hobbyapp.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.com.britenet.hobbyapp.data.admin.AdminData
import pl.com.britenet.hobbyapp.data.admin.IAdminRepository
import javax.inject.Inject

@HiltViewModel
class AdminsFragmentViewModel @Inject constructor(private val adminRepo: IAdminRepository) : ViewModel() {
    private val _admins = MutableLiveData<List<AdminData>>()
    val admins: LiveData<List<AdminData>>
        get() = _admins

    private val _exceptionToShow = MutableLiveData<String?>()
    val exceptionToShow: LiveData<String?>
        get() = _exceptionToShow

    init {
        viewModelScope.launch(Dispatchers.IO) {
            updateData()
        }
    }

    private suspend fun updateData() {
        try {
            val admins = adminRepo.getAdmins()
            _admins.postValue(admins)
        } catch (e: FirebaseFirestoreException) {
            _exceptionToShow.postValue(e.message)
        }
    }

    fun onExceptionDisplayed() {
        _exceptionToShow.value = null
    }
}
