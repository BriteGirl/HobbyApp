package pl.com.britenet.hobbyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import pl.com.britenet.hobbyapp.data.Hobby
import pl.com.britenet.hobbyapp.data.images.IImagesRepository
import pl.com.britenet.hobbyapp.data.images.ImagesRepository

class HobbiesListAdapter @AssistedInject constructor(@Assisted imageFolder: ImagesRepository.ImageFolder) : RecyclerView.Adapter<HobbyViewHolder>() {
    private val imagesRepository: IImagesRepository = ImagesRepository(imageFolder)
    private var hobbies: List<Hobby> = emptyList()
    private lateinit var onItemClicked: (itemId: String) -> Unit
    private var checkboxVisible: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HobbyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_hobby, parent, false)

        return HobbyViewHolder(view)
    }

    override fun onBindViewHolder(holder: HobbyViewHolder, position: Int) {
        val hobby = hobbies[position]
        holder.hobbyNameTv.text = hobby.name
        if (this::onItemClicked.isInitialized) {
            holder.itemView.setOnClickListener { onItemClicked(hobby.id) }
        }
        if (checkboxVisible) {
            holder.checkBox.visibility = View.VISIBLE
            holder.checkBox.isChecked = hobby.isChecked
            holder.checkBox.setOnCheckedChangeListener { _, isChecked -> hobby.isChecked = isChecked }
        }
        imagesRepository.loadHobbyImageInto(hobby.imgName, holder.hobbyImageView)
    }

    override fun getItemCount(): Int {
        return hobbies.size
    }

    fun updateData(newData: List<Hobby>) {
        val diff = DiffUtil.calculateDiff(getCallback(hobbies, newData), true)
        hobbies = newData
        diff.dispatchUpdatesTo(this)
    }

    private fun getCallback(oldList: List<Hobby>, newList: List<Hobby>) =
        object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return oldList.size
            }

            override fun getNewListSize(): Int {
                return newList.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].id == newList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
        }

    fun setItemClickListener(listener: (hobbyId: String) -> Unit) {
        onItemClicked = listener
    }

    fun enableCheckboxes() {
        checkboxVisible = true
    }

    fun getCheckedHobbies(): List<Hobby> {
        return hobbies.filter { hobby -> hobby.isChecked }
    }
}

@AssistedFactory
interface HobbiesListAdapterFactory {
    fun createAdapter(imageFolder: ImagesRepository.ImageFolder = ImagesRepository.ImageFolder.IMAGES_FOLDER): HobbiesListAdapter
}
