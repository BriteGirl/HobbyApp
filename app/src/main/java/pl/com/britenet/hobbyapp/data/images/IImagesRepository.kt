package pl.com.britenet.hobbyapp.data.images

import android.net.Uri
import android.widget.ImageView

interface IImagesRepository {
    fun loadHobbyImageInto(fileName: String?, imageView: ImageView)
    fun loadHobbyImageInto(uri: Uri?, imageView: ImageView)
}
