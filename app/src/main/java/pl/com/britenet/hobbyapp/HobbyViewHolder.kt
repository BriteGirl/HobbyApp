package pl.com.britenet.hobbyapp

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pl.com.britenet.hobbyapp.databinding.ItemHobbyBinding

class HobbyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val binding = ItemHobbyBinding.bind(itemView)
    val hobbyNameTv: TextView = binding.itemHobbyName
    val hobbyImageView: ImageView = binding.hobbyImg
    val checkBox: CheckBox = binding.itemHobbyCheckbox
}
