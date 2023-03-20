package pl.com.britenet.hobbyapp.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import pl.com.britenet.hobbyapp.R
import pl.com.britenet.hobbyapp.admin.AdminPanelActivity
import pl.com.britenet.hobbyapp.data.user.UserData
import pl.com.britenet.hobbyapp.databinding.DialogFriendRequestsBinding
import pl.com.britenet.hobbyapp.databinding.DialogOneTextInputBinding
import pl.com.britenet.hobbyapp.databinding.FragmentAccountBinding

@AndroidEntryPoint
class AccountFragment : Fragment() {
    private val viewModel: AccountViewModel by viewModels()
    private lateinit var binding: FragmentAccountBinding
    private val getLocationActivity = registerForActivityResult(GetLocation()) { data -> viewModel.saveLocation(data) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAccountBinding.inflate(inflater)

        // observe current user from the ViewModel
        viewModel.currentUser.observe(viewLifecycleOwner) {
            setView(it)
            setCurrentUser(it)
        }
        viewModel.exceptionToShow.observe(viewLifecycleOwner) { showException(it) }
        viewModel.isAdmin.observe(viewLifecycleOwner) { activateAdminFunction(it) }

        activateButtons()

        return binding.root
    }

    private fun showException(exceptionMessage: String?) {
        if (exceptionMessage != null) {
            Toast.makeText(context, exceptionMessage, Toast.LENGTH_SHORT).show()
            viewModel.onExceptionDisplayed()
        }
    }

    private fun setView(user: UserData?) {
        val userViewActive = (binding.userLoggedLayout.visibility == View.VISIBLE)
        if (user == null && userViewActive) {
            binding.userLoggedLayout.visibility = View.GONE
            binding.noUserLayout.visibility = View.VISIBLE
        } else {
            binding.userLoggedLayout.visibility = View.VISIBLE
            binding.noUserLayout.visibility = View.GONE
        }
    }

    private fun activateButtons() {
        binding.signOutBtn.setOnClickListener { viewModel.signOut() }
        binding.logInBtn.setOnClickListener { goToLoginActivity() }
        binding.userGreeting.setOnClickListener {
            getChangeDataDialog(R.string.enter_new_name)?.show()
        }
        binding.userUsername.setOnClickListener {
            getChangeDataDialog(R.string.enter_new_username)?.show()
        }
        binding.userEmail.setOnClickListener {
            getChangeDataDialog(R.string.enter_new_email)?.show()
        }
        binding.accountActionsLayout.friendRequestsBtn.setOnClickListener {
            getFriendRequestsDialog()?.show()
        }
        binding.accountActionsLayout.adminPanelBtn.setOnClickListener {
            startActivity(Intent(context, AdminPanelActivity::class.java))
        }
        binding.accountActionsLayout.userLocationBtn.setOnClickListener {
            getLocationActivity.launch(Unit)
        }
    }

    private fun activateAdminFunction(isAdmin: Boolean) {
        val adminPanelBtn = binding.accountActionsLayout.adminPanelBtn
        if (isAdmin && adminPanelBtn.visibility == View.GONE)
            adminPanelBtn.visibility = View.VISIBLE
        else if (!isAdmin && adminPanelBtn.visibility == View.VISIBLE)
            adminPanelBtn.visibility = View.GONE
    }

    private fun getChangeDataDialog(labelResId: Int): AlertDialog? {
        val dialogBinding = DialogOneTextInputBinding.inflate(layoutInflater)
        return context?.let {
            AlertDialog.Builder(it)
                .setTitle(labelResId)
                .setView(dialogBinding.root)
                .setNegativeButton(R.string.cancel) { dialog, btnId -> dialog.cancel() }
                .setPositiveButton(R.string.submit) { dialog, btnId ->
                    val inputEditText = dialogBinding.changeDataInput
                    val text = inputEditText.text.toString()
                    viewModel.onNewDataSubmitted(text, labelResId)
                    dialog.dismiss()
                }
                .create()
        }
    }

    private fun getFriendRequestsDialog(): AlertDialog? {
        val dialogBinding = DialogFriendRequestsBinding.inflate(layoutInflater)
        val dataAdapter = FriendRequestAdapter(viewModel::onRequestAccepted)
        dialogBinding.friendRequestsRv.apply {
            adapter = dataAdapter
            layoutManager = LinearLayoutManager(context)
        }
        viewModel.friendRequestingUsers.observe(viewLifecycleOwner) { dataAdapter.updateData(it) }

        return context?.let {
            AlertDialog.Builder(it)
                .setView(dialogBinding.root)
                .setPositiveButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .create()
        }
    }

    private fun setCurrentUser(user: UserData?) {
        user?.let {
            val displayName = if (!it.name.isNullOrEmpty()) it.name else it.email
            binding.userGreeting.text = getString(R.string.username_greeting, displayName)
            binding.userEmail.text = it.email
            binding.userUsername.text = it.username
        }
    }

    private fun goToLoginActivity() {
        val intent = Intent(this.context, LoginActivity::class.java)
        startActivity(intent)
    }
}
