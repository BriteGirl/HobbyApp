package pl.com.britenet.hobbyapp.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import pl.com.britenet.hobbyapp.R
import pl.com.britenet.hobbyapp.data.admin.AdminData
import pl.com.britenet.hobbyapp.utils.setOnSafeClickListener

class AdminListAdapter : RecyclerView.Adapter<AdminItemViewHolder>() {
    private lateinit var onMessageBtnClicked: (adminId: String) -> Unit
    private lateinit var onSettingsBtnClicked: (adminId: String) -> Unit
    private var admins = listOf<AdminData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_admin, parent, false)
        return AdminItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminItemViewHolder, position: Int) {
        val admin = admins[position]
        holder.nameTv.text = admin.name
        holder.usernameTv.text = admin.username
        holder.emailTv.text = admin.email
        holder.adminRoleTv.text = admin.adminRole.name
        if (this::onMessageBtnClicked.isInitialized)
            holder.messageBtn.setOnSafeClickListener { onMessageBtnClicked(admin.userId) }
        if (this::onSettingsBtnClicked.isInitialized)
            holder.settingsBtn.setOnSafeClickListener { onSettingsBtnClicked(admin.userId) }
    }

    override fun getItemCount(): Int = admins.size

    fun updateAdminsList(newList: List<AdminData>) {
        val diff = DiffUtil.calculateDiff(getDiffCallback(admins, newList))
        diff.dispatchUpdatesTo(this)
        admins = newList
    }

    fun setOnMessageBtnClicked(onMessageBtnClicked: (adminId: String) -> Unit) {
        this.onMessageBtnClicked = onMessageBtnClicked
    }

    fun setOnSettingsBtnClicked(onSettingsBtnClicked: (adminId: String) -> Unit) {
        this.onSettingsBtnClicked = onSettingsBtnClicked
    }

    private fun getDiffCallback(oldList: List<AdminData>, newList: List<AdminData>) =
        object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size

            override fun getNewListSize(): Int = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldList[oldItemPosition].userId == newList[newItemPosition].userId

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldList[oldItemPosition] == newList[newItemPosition]
        }
}
