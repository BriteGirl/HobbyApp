package pl.com.britenet.hobbyapp.hobby

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import pl.com.britenet.hobbyapp.databinding.ItemUserBinding

class UserListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemUserBinding.bind(view)
    val userImage = binding.userMiniImg
    val usernameTv = binding.userListUsername
    val addToFriendsBtn = binding.userListAddFriendBtn
    val sendMessageBtn = binding.userListMessageUserBtn
}
