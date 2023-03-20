package pl.com.britenet.hobbyapp.user

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import pl.com.britenet.hobbyapp.databinding.ActivityUserLocationBinding

@AndroidEntryPoint
class UserLocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private lateinit var binding: ActivityUserLocationBinding
    private val viewModel: UserLocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserLocationBinding.inflate(layoutInflater)
        binding.userLocationSaveBtn.setOnClickListener { onSave() }
        viewModel.resultIntent.observe(this) { onResultIntent(it) }
        viewModel.exceptionToShow.observe(this) { showException(it) }

        // load the map asynchronously
        binding.userLocationMap.getFragment<SupportMapFragment>().getMapAsync(this)

        setContentView(binding.root)
    }

    private fun onResultIntent(intent: Intent?) {
        if (intent != null) {
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        setUpMap()
    }

    private fun setUpMap() {
        // enable controls
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true

        // set marker on long click
        googleMap.setOnMapLongClickListener { latLong ->
            viewModel.removeMarker()
            val markerOptions = MarkerOptions()
                .position(latLong)
                .title("Dropped pin")
            val newMarker = googleMap.addMarker(markerOptions)
            viewModel.updateMarker(newMarker)
        }
    }

    private fun onSave() {
        val latLong: LatLng? = viewModel.currentMarker.value?.position
        if (latLong == null) {
            viewModel.onSaveNull()
            return
        }
        val geo = Geocoder(this)
        // TODO: this is deprecated in Android API 33
        val addresses = geo.getFromLocation(latLong.latitude, latLong.longitude, 1)!!
        viewModel.onSave(latLong, addresses)
    }

    private fun showException(exception: Exception?) {
        if (exception != null)
            Toast
                .makeText(this, exception.message ?: exception.toString(), Toast.LENGTH_SHORT)
                .show()
    }
}