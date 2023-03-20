package pl.com.britenet.hobbyapp.admin

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import pl.com.britenet.hobbyapp.databinding.ItemAdminBinding

class AdminItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemAdminBinding.bind(view)
    val pictureIv = binding.adminMiniImg
    val usernameTv = binding.adminUsername
    val nameTv = binding.adminName
    val emailTv = binding.adminEmail
    val adminRoleTv = binding.adminRole
    val messageBtn = binding.adminMessageUserBtn
    val settingsBtn = binding.adminSettingsBtn
}
