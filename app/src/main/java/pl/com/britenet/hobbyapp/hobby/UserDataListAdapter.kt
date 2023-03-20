package pl.com.britenet.hobbyapp.hobby

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import pl.com.britenet.hobbyapp.R
import pl.com.britenet.hobbyapp.data.user.UserData
import pl.com.britenet.hobbyapp.utils.setOnSafeClickListener

class UserDataListAdapter(
    val onAddFriendClicked: (String) -> Unit,
    val onMessageUserClicked: (String, String?) -> Unit
) : RecyclerView.Adapter<UserListViewHolder>() {
    private var data: List<UserData> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_user, parent, false)
        return UserListViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        val userData = data[position]
        holder.usernameTv.text = userData.username
        holder.addToFriendsBtn.setOnSafeClickListener(5000) {
            onAddFriendClicked(userData.userId)
        }
        holder.sendMessageBtn.setOnSafeClickListener {
            onMessageUserClicked(userData.userId, userData.username)
        }
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: List<UserData>) {
        val diff = DiffUtil.calculateDiff(getDiffCallback(data, newData))
        data = newData
        diff.dispatchUpdatesTo(this)
    }

    private fun getDiffCallback(oldList: List<UserData>, newList: List<UserData>): DiffUtil.Callback =
        object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size

            override fun getNewListSize(): Int = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].userId == newList[newItemPosition].userId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] === newList[newItemPosition]
            }
        }
}
