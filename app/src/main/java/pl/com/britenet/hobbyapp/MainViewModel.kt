package pl.com.britenet.hobbyapp

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
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val hobbiesRepo: IHobbiesRepository) : ViewModel() {

    private val _hobbies = MutableLiveData<List<Hobby>>()
    val hobbies: LiveData<List<Hobby>>
        get() = _hobbies

    // stores id of the hobby picked to display
    private val _hobbyToShow = MutableLiveData<String?>()
    val hobbyToShow: LiveData<String?>
        get() = _hobbyToShow

    init {
        updateHobbies()
    }

    fun updateHobbies() {
        viewModelScope.launch {
            val res: List<Hobby> = withContext(Dispatchers.IO) {
                hobbiesRepo.getAllHobbies()
            }
            _hobbies.postValue(res)
        }
    }

    fun onHobbyClicked(hobbyId: String) {
        _hobbyToShow.value = hobbyId
    }

    fun onHobbyDetailsActivityStart() {
        _hobbyToShow.value = null
    }
}
