package pl.com.britenet.hobbyapp.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.com.britenet.hobbyapp.data.Hobby
import pl.com.britenet.hobbyapp.data.IHobbiesRepository
import pl.com.britenet.hobbyapp.data.location.ILocationRepository
import pl.com.britenet.hobbyapp.data.location.UserGeoData
import pl.com.britenet.hobbyapp.utils.getNamesString
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val locationRepo: ILocationRepository,
    private val hobbiesRepo: IHobbiesRepository
) :
    ViewModel() {
    private var data = listOf<UserGeoData>()
    private var locationFilter: String? = null

    // stores a list of hobby ids
    private var hobbiesFilter = listOf<String>()
    val checkedHobbies = MutableLiveData<BooleanArray?>()

    private val _geoData = MutableLiveData<List<UserGeoData>>(listOf())
    val geoData: LiveData<List<UserGeoData>>
        get() = _geoData
    private val _hobbies = MutableLiveData<List<Hobby>>(listOf())
    val hobbies: LiveData<List<Hobby>>
        get() = _hobbies
    private val _hobbiesFilterDisplayString = MutableLiveData("")
    val hobbiesFilterDisplayString: LiveData<String>
        get() = _hobbiesFilterDisplayString

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _hobbies.postValue(hobbiesRepo.getAllHobbies())
        }
    }

    fun loadAllLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            val locations = locationRepo.getAllLocations()
            data = locations
            _geoData.postValue(locations)
        }
    }

    fun filterByLocation(locationName: String?) {
        locationFilter = locationName
        updateFilters()
    }

    private fun updateFilters() {
        val filtered = data.filter { userData ->
            val loc = locationFilter
            val hobbiesIds = hobbiesFilter
            var inLocation = true
            var hasHobbies = true

            if (loc != null) {
                inLocation = userData.cityName.contains(loc, true)
                        || userData.countryName.contains(loc, true)
            }
            if (inLocation) {
                // filter by hobby - user must have all checked hobbies
                val userHobbiesIds = userData.hobbies.map { it.id }
                for (checkedHobbyId in hobbiesIds) {
                    val inChecked = userHobbiesIds.contains(checkedHobbyId)
                    if (!inChecked) {
                        hasHobbies = false
                        break
                    }
                }
            }
            return@filter inLocation && hasHobbies
        }
        _geoData.value = filtered
    }

    fun filterByHobby(checkedHobbies: List<Hobby>) {
        hobbiesFilter = checkedHobbies.map { it.id }
        updateFilters()
        _hobbiesFilterDisplayString.value = checkedHobbies.getNamesString()
        if (checkedHobbies.isEmpty()) {
            // uncheck all hobbies in UI
            this.checkedHobbies.value = null
        }
    }
}
