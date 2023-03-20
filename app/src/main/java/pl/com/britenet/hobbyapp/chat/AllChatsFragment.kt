package pl.com.britenet.hobbyapp.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import pl.com.britenet.hobbyapp.R
import pl.com.britenet.hobbyapp.databinding.FragmentAllChatsBinding
import pl.com.britenet.hobbyapp.utils.hide
import pl.com.britenet.hobbyapp.utils.show

@AndroidEntryPoint
class AllChatsFragment : Fragment() {

    private lateinit var binding: FragmentAllChatsBinding
    private val viewModel: AllChatsViewModel by viewModels()
    private val chatListAdapter = ChatListAdapter(::goToChatWith)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllChatsBinding.inflate(layoutInflater)
        setUpRv()

        viewModel.chats.observe(viewLifecycleOwner) {
            chatListAdapter.updateList(it)
            updateView(it.isEmpty())
        }

        viewModel.errorToShow.observe(viewLifecycleOwner) {
            showError(it)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkForUser()
        val chatsList = viewModel.chats.value ?: listOf()
        updateView(chatsList.isEmpty())
    }

    private fun updateView(isChatListEmpty: Boolean) {
        val isUserSignedIn: Boolean? = viewModel.isUserLoggedIn.value
        val chatRv = binding.chatListRv
        val infoContainer = binding.chatsNotVisibleTv

        if (isUserSignedIn == null) {
            chatRv.hide()
            infoContainer.text = getString(R.string.loading)
            infoContainer.show()
        } else {
            if (isUserSignedIn) {
                if (isChatListEmpty) {
                    chatRv.hide()
                    infoContainer.text = getString(R.string.no_chats_yet)
                    infoContainer.show()
                } else {
                    chatRv.show()
                    infoContainer.hide()
                }
            } else {
                chatRv.hide()
                infoContainer.text = getString(R.string.signed_in_only)
                infoContainer.show()
            }
        }
    }

    private fun setUpRv() {
        binding.chatListRv.apply {
            adapter = chatListAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun goToChatWith(chatId: String, otherUserId: String, otherUserUsername: String?) {
        val intent = ChatActivity.createIntent(requireContext(), chatId, otherUserId, otherUserUsername)
        startActivity(intent)
    }

    private fun showError(message: String?) {
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.onErrorShown()
        }
    }
}