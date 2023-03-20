package pl.com.britenet.hobbyapp.addhobby

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dagger.hilt.android.AndroidEntryPoint
import pl.com.britenet.hobbyapp.data.images.IImagesRepository
import pl.com.britenet.hobbyapp.databinding.ActivityNewHobbyBinding
import javax.inject.Inject

@AndroidEntryPoint
class NewHobbyActivity : AppCompatActivity() {
    private val viewModel: NewHobbyViewModel by viewModels()
    private lateinit var binding: ActivityNewHobbyBinding
    @Inject lateinit var imagesRepository: IImagesRepository
    val pickImageLauncher = registerForActivityResult(GetContent()) { uri ->
        val fileType = uri?.let { contentResolver.getType(it) }
        viewModel.onImagePicked(uri, fileType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewHobbyBinding.inflate(layoutInflater)

        binding.submitNewHobbyBtn.setOnClickListener { onSubmitClicked() }
        binding.newHobbyImage.setOnClickListener { onImageClicked() }

        viewModel.submitComplete.observe(this) { onSubmitComplete(it) }
        viewModel.imagePicked.observe(this) { onImagePicked(it) }
        viewModel.exceptionToShow.observe(this) { showException(it) }

        setContentView(binding.root)
    }

    private fun showException(exceptionMessage: String?) {
        if (exceptionMessage != null) {
            Toast.makeText(this, exceptionMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onSubmitComplete(isSubmitComplete: Boolean) {
        if (isSubmitComplete)
            finish()
    }

    private fun onSubmitClicked() {
        val hobbyName = binding.newHobbyName.text.toString()
        if (viewModel.imagePicked.value != null)
        // get image as bitmap and save the new hobby with its image
            Glide.with(this).asBitmap().load(viewModel.imagePicked.value).into(object :
                    CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        viewModel.onNewHobbySubmit(hobbyName, resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        // save hobby without an image
        else viewModel.onNewHobbySubmit(hobbyName)
    }

    private fun onImageClicked() {
        val intent = Intent(Intent.ACTION_PICK)
        val intentType = "image/*"
        intent.type = intentType
        val resolvedActivity = intent.resolveActivity(packageManager)

        if (resolvedActivity != null) {
            pickImageLauncher.launch(intentType)
        }
    }

    private fun onImagePicked(uri: Uri?) {
        imagesRepository.loadHobbyImageInto(uri, binding.newHobbyImage)
    }
}
