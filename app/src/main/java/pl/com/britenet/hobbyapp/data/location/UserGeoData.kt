package pl.com.britenet.hobbyapp.data.location

import pl.com.britenet.hobbyapp.data.Hobby

data class UserGeoData(
    val username: String,
    val userId: String,
    override val latitude: Double,
    override val longitude: Double,
    override val cityName: String,
    override val countryName: String,
    val hobbies: List<Hobby> = listOf()
) : GeoData(latitude, longitude, cityName, countryName) {
    // this constructor is a default for Firestore deserialization
    constructor() : this("", "", 0.0, 0.0, "", "")
}