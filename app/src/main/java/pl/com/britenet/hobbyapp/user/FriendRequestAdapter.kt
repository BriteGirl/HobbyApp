package pl.com.britenet.hobbyapp.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import pl.com.britenet.hobbyapp.R
import pl.com.britenet.hobbyapp.data.user.UserData
import pl.com.britenet.hobbyapp.databinding.ItemUserRequestBinding

class FriendRequestAdapter(val onRequestAccepted: (String, Boolean) -> Unit) : RecyclerView.Adapter<FriendRequestViewHolder>() {
    private var data = listOf<UserData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val holderView = inflater.inflate(R.layout.item_user_request, parent, false)
        return FriendRequestViewHolder(holderView)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val userData = data[position]
        holder.usernameTv.text = userData.username
        holder.acceptBtn.setOnClickListener { onRequestAccepted(userData.userId, true) }
        holder.rejectBtn.setOnClickListener { onRequestAccepted(userData.userId, false) }
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: List<UserData>?) {
        if (newData == null || newData.isEmpty()) {
            data = mutableListOf()
            return
        }
        val diff = DiffUtil.calculateDiff(getDiffUtilCallback(data, newData))
        diff.dispatchUpdatesTo(this)
        data = newData
    }

    private fun getDiffUtilCallback(oldData: List<UserData>, newData: List<UserData>) =
        object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldData.size

            override fun getNewListSize(): Int = newData.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldData[oldItemPosition].userId == newData[newItemPosition].userId

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldData[oldItemPosition] === newData[newItemPosition]
        }
}

class FriendRequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemUserRequestBinding.bind(view)
    val usernameTv = binding.userRequestUsername
    val userPictureIV = binding.userFriendRequestMiniImg
    val rejectBtn = binding.userRequestRejectBtn
    val acceptBtn = binding.userFriendRequestAcceptBtn
}
