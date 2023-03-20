package pl.com.britenet.hobbyapp.data.images

import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ImagesRepository(private val imageFolder: ImageFolder) : IImagesRepository {
    enum class ImageFolder(val path: String) {
        IMAGES_FOLDER("hobbies_images"),
        SUGGESTIONS_FOLDER("hobbies_images/suggestions")
    }

    override fun loadHobbyImageInto(fileName: String?, imageView: ImageView) {
        if (fileName.isNullOrEmpty()) {
            Glide.with(imageView.context).clear(imageView)
            return
        }

        val folderRef = Firebase.storage.reference.child(imageFolder.path)
        val imageRef = folderRef.child(fileName)

        Glide.with(imageView.context)
            .load(imageRef)
            .centerCrop()
            .into(imageView)
    }

    override fun loadHobbyImageInto(uri: Uri?, imageView: ImageView) {
        if (uri == null) return

        imageView.apply {
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(this)
        }
    }
}
