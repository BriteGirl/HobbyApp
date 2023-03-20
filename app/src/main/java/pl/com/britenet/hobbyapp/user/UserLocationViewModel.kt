package pl.com.britenet.hobbyapp.user

import android.content.Intent
import android.location.Address
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.com.britenet.hobbyapp.data.location.GeoData
import pl.com.britenet.hobbyapp.data.location.GeoDataResult
import pl.com.britenet.hobbyapp.data.location.ILocationRepository
import pl.com.britenet.hobbyapp.exceptions.IneligibleMarkerException
import java.io.IOException
import javax.inject.Inject

private const val LOG_TAG = "HobbyApp.UserLocationViewModel"

@HiltViewModel
class UserLocationViewModel @Inject constructor(private val locationRepo: ILocationRepository) :
    ViewModel() {
    private val _currentMarker = MutableLiveData<Marker?>(null)
    val currentMarker: LiveData<Marker?>
        get() = _currentMarker
    private val _resultIntent = MutableLiveData<Intent?>(null)
    val resultIntent: LiveData<Intent?>
        get() = _resultIntent
    private val _exceptionToShow = MutableLiveData<Exception>(null)
    val exceptionToShow: LiveData<Exception?>
        get() = _exceptionToShow

    fun removeMarker() {
        _currentMarker.value?.remove()
    }

    fun updateMarker(newMarker: Marker?) {
        _currentMarker.value = newMarker
    }

    fun onSave(latLong: LatLng, addresses: List<Address>) {
        val location: Address
        try {
            location = addresses[0]
        } catch (e: IndexOutOfBoundsException) {
            // if the marker is not on land, throw exception
            val exception = IneligibleMarkerException()
            Log.i(LOG_TAG, exception.message)
            _exceptionToShow.value = exception
            return
        } catch (e: IOException) {
            Log.i(LOG_TAG, e.message ?: e.toString())
            return
        }

        val locationName = location.locality ?: location.subAdminArea ?: location.adminArea ?: ""
        val countryName = location.countryName ?: ""

        saveLocation(latLong.latitude, latLong.longitude, locationName, countryName)
    }

    fun saveLocation(
        latitude: Double,
        longitude: Double,
        locationName: String,
        countryName: String
    ) {
        val intent = Intent()
        val result = GeoDataResult
            .GeoDataResultSet(GeoData(latitude, longitude, locationName, countryName))
        intent.putExtra(GetLocation.RESULT_DATA_EXTRA, result)
        _resultIntent.value = intent
    }

    fun onSaveNull() {
        val intent = Intent()
        intent.putExtra(GetLocation.RESULT_DATA_EXTRA, GeoDataResult.GeoDataResultDelete())
        _resultIntent.value = intent
    }
}