package pl.com.britenet.hobbyapp.data.location

interface ILocationRepository {
    suspend fun saveLocation(geoData: GeoData)
    suspend fun deleteLocation()
    suspend fun getAllLocations(): List<UserGeoData>
}
