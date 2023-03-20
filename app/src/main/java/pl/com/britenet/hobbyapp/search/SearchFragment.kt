package pl.com.britenet.hobbyapp.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import pl.com.britenet.hobbyapp.R
import pl.com.britenet.hobbyapp.data.Hobby
import pl.com.britenet.hobbyapp.data.location.UserGeoData
import pl.com.britenet.hobbyapp.databinding.FragmentSearchBinding
import pl.com.britenet.hobbyapp.utils.getNames

@AndroidEntryPoint
class SearchFragment : Fragment(), OnMapReadyCallback {
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var binding: FragmentSearchBinding
    private lateinit var googleMap: GoogleMap
    private val markers = mutableListOf<Marker>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // load the map
        val googleMapFragment = binding.searchMap.getFragment() as SupportMapFragment
        googleMapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // enable controls
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true

        // load & observe data
        viewModel.loadAllLocations()
        viewModel.geoData.observe(this) {
            clearMarkers()
            showMarkers(it)
        }

        // set up filtering
        setUpLocationFilter()
        setUpHobbyFilter()
    }

    private fun showMarkers(data: List<UserGeoData>?) {
        if (data != null) {
            for (geoData in data) {
                val markerOptions = MarkerOptions()
                    .position(LatLng(geoData.latitude, geoData.longitude))
                    .title(geoData.username)
                val marker = googleMap.addMarker(markerOptions)
                if (marker != null) {
                    markers.add(marker)
                }
            }
        }
    }

    private fun clearMarkers() {
        for (marker in markers) {
            marker.remove()
        }
        markers.clear()
    }

    private fun setUpLocationFilter() {
        binding.userSearchSv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val locationName = query?.trim()
                viewModel.filterByLocation(locationName)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    private fun setUpHobbyFilter() {
        binding.userSearchHobbyFilter.setOnClickListener {
            getHobbyChoiceDialog().show()
        }
        viewModel.hobbiesFilterDisplayString.observe(this) {
            binding.userSearchHobbyFiltersDisplay.text = it
        }
    }

    private fun getHobbyChoiceDialog(): AlertDialog {
        val hobbies: List<Hobby> = viewModel.hobbies.value ?: listOf()
        val hobbiesNames = hobbies.getNames()
        val checked = viewModel.checkedHobbies.value ?: BooleanArray(hobbies.size) { false }
        val db = AlertDialog.Builder(this.requireContext())
            .setCancelable(true)
            .setTitle(R.string.hobbies)
            .setMultiChoiceItems(hobbiesNames, checked) { _, index, isChecked ->
                if (isChecked) {
                    checked[index] = true
                } else if (checked[index]) {
                    checked[index] = false
                }
            }
            .setPositiveButton(R.string.ok_btn_label) { _, _ ->
                viewModel.checkedHobbies.value = checked
                viewModel.filterByHobby(hobbies.filterIndexed { index, _ -> checked[index] })
            }
            .setNeutralButton(R.string.clear_btn_label) { _, _ -> viewModel.filterByHobby(listOf()) }
        return db.create()
    }
}