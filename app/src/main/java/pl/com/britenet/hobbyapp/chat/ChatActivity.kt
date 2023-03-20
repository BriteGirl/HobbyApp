package pl.com.britenet.hobbyapp.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import pl.com.britenet.hobbyapp.databinding.ActivityChatBinding

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    companion object {
        private const val OTHER_USER_ID_EXTRA = "ChatActivity.UserId"
        private const val OTHER_USER_USERNAME = "ChatActivity.Username"
        private const val CHAT_ID = "ChatActivity.ChatId"

        fun createIntent(context: Context, otherUserId: String, otherUserUsername: String?) =
            Intent(context, ChatActivity::class.java)
                .putExtra(OTHER_USER_ID_EXTRA, otherUserId)
                .putExtra(OTHER_USER_USERNAME, otherUserUsername)

        fun createIntent(
            context: Context,
            chatId: String,
            otherUserId: String,
            otherUserUsername: String?
        ) = Intent(context, ChatActivity::class.java)
            .putExtra(CHAT_ID, chatId)
            .putExtra(OTHER_USER_ID_EXTRA, otherUserId)
            .putExtra(OTHER_USER_USERNAME, otherUserUsername)
    }

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var binding: ActivityChatBinding
    private val messagesAdapter = MessagesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        val userId = intent.getStringExtra(OTHER_USER_ID_EXTRA)
        val username = intent.getStringExtra(OTHER_USER_USERNAME)
        val chatId = intent.getStringExtra(CHAT_ID)

        viewModel.loadData(userId, username, chatId)

        binding.sendMessageBtn.setOnClickListener { sendMessage() }
        viewModel.messages.observe(this) {
            messagesAdapter.updateMessages(it)
            binding.chatRv.adapter = messagesAdapter
            binding.chatRv.scrollToPosition(it.lastIndex)
        }
        viewModel.secondUserId.observe(this) { messagesAdapter.updateOtherUserId(it) }
        viewModel.displayedUsername.observe(this) { updateUsername(it) }
        viewModel.errorToShow.observe(this) { showError(it) }

        binding.chatRv.apply {
            adapter = messagesAdapter
            layoutManager = LinearLayoutManager(context)
        }

        setContentView(binding.root)
    }

    private fun updateUsername(username: String) {
        binding.chatUsername.text = username
    }

    private fun sendMessage() {
        val message = binding.messageEditText.text
        if (message.isNotBlank()) {
            viewModel.sendMessage(message.toString())
            binding.messageEditText.setText("")
        }
    }

    private fun showError(message: String?) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            viewModel.onErrorShown()
        }
    }
}