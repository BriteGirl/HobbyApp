package pl.com.britenet.hobbyapp.addhobby

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.com.britenet.hobbyapp.data.IHobbiesRepository
import javax.inject.Inject

@HiltViewModel
class NewHobbyViewModel @Inject constructor(private val hobbiesRepository: IHobbiesRepository) : ViewModel() {

    private var _submitComplete = MutableLiveData<Boolean>(false)
    val submitComplete: LiveData<Boolean>
        get() = _submitComplete

    private var _imagePicked = MutableLiveData<Uri?>()
    val imagePicked: LiveData<Uri?>
        get() = _imagePicked
    private var fileType: String? = null

    private var _exceptionToShow = MutableLiveData<String?>()
    val exceptionToShow: LiveData<String?>
        get() = _exceptionToShow

    fun onImagePicked(uri: Uri?, fileType: String?) {
        _imagePicked.value = uri
        this.fileType = fileType?.substringAfter('/')
    }

    fun onNewHobbySubmit(hobbyName: String, imgBitmap: Bitmap? = null) {
        Log.d("NewHobbyViewModel", "onNewHobbySubmit")
        viewModelScope.launch {
            Log.d("NewHobbyViewModel", "launch")
            withContext(Dispatchers.IO) {
                try {
                    Log.d("NewHobbyViewModel", "coroutine")

                    val imageUploadTask = hobbiesRepository.saveNewHobby(
                        hobbyName,
                        imgBitmap,
                        fileType
                    )
                    Log.d("NewHobbyViewModel", "after save")
                    // imageUploadTask?.await()
                    _submitComplete.postValue(true)
                } catch (e: Exception) {
                    Log.d("NewHobbyViewModel", "exception")
                    if (e is CancellationException) throw e
                    _exceptionToShow.postValue(e.message)
                }
            }
        }
    }
}
