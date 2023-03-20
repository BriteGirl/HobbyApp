package pl.com.britenet.hobbyapp.data.location

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class GeoData(
    open val latitude: Double,
    open val longitude: Double,
    open val cityName: String,
    open val countryName: String
) : Parcelable