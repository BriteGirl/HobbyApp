package pl.com.britenet.hobbyapp.data.location

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class GeoDataResult() : Parcelable {
    @Parcelize
    class GeoDataResultSet(val data: GeoData) : GeoDataResult()

    @Parcelize
    class GeoDataResultDelete : GeoDataResult()

    @Parcelize
    class GeoDataResultCancelled : GeoDataResult()
}