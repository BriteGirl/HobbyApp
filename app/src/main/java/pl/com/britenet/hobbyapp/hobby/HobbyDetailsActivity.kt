package pl.com.britenet.hobbyapp.hobby

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import pl.com.britenet.hobbyapp.R
import pl.com.britenet.hobbyapp.chat.ChatActivity
import pl.com.britenet.hobbyapp.data.images.IImagesRepository
import pl.com.britenet.hobbyapp.data.user.UserData
import pl.com.britenet.hobbyapp.databinding.ActivityHobbyDetailsBinding
import javax.inject.Inject

@AndroidEntryPoint
class HobbyDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHobbyDetailsBinding
    private val viewModel: HobbyDetailsViewModel by viewModels()
    @Inject lateinit var imagesRepository: IImagesRepository
    private lateinit var userListAdapter: UserDataListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHobbyDetailsBinding.inflate(layoutInflater)

        binding.hobbyUsersRv.apply {
            userListAdapter =
                UserDataListAdapter(viewModel::onAddFriendClicked, ::onMessageUserClicked)
            adapter = userListAdapter
            layoutManager = LinearLayoutManager(context)
        }

        viewModel.exceptionToShow.observe(this) { showException(it) }
        viewModel.hobbyName.observe(this) { binding.hobbyDetailsName.text = it }
        viewModel.hobbyImageName.observe(this) { displayHobbyImage(it) }
        viewModel.isFavourite.observe(this) { toggleFavourite(it) }
        viewModel.users.observe(this) { updateUserList(it) }
        viewModel.isFriendRequestSent.observe(this) { displayFriendRequestSent(it) }
        viewModel.isUserLoggedIn.observe(this) { toggleFeatureAvailability(it) }

        binding.favouriteHobbyBtn.setOnClickListener { viewModel.onFavouriteBtnClicked() }

        setContentView(binding.root)
    }

    private fun onMessageUserClicked(userId: String, username: String?) {
        val intent = ChatActivity.createIntent(this, userId, username)
        startActivity(intent)
    }

    private fun toggleFeatureAvailability(isUserLoggedIn: Boolean) {
        val usersListRv = binding.hobbyUsersRv
        val usersListUnavailableTv = binding.hobbyUsersNotVisibleTv

        if (isUserLoggedIn && usersListRv.visibility == View.GONE) {
            usersListRv.visibility = View.VISIBLE
            usersListUnavailableTv.visibility = View.GONE
        } else if (!isUserLoggedIn && usersListRv.visibility == View.VISIBLE) {
            usersListRv.visibility = View.GONE
            usersListUnavailableTv.visibility = View.VISIBLE
        }
    }

    private fun displayFriendRequestSent(isRequestSent: Boolean?) {
        if (isRequestSent != null) {
            val toastMessage = if (isRequestSent) R.string.friend_request_sent
            else R.string.something_went_wrong
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
            viewModel.onRequestSentMessageShowed()
        }
    }

    private fun updateUserList(data: List<UserData>?) {
        if (data == null) userListAdapter.updateData(emptyList())
        else userListAdapter.updateData(data)
    }

    override fun onPause() {
        super.onPause()
        viewModel.onActivityPause()
    }

    private fun toggleFavourite(isFavourite: Boolean?) {
        if (isFavourite == null) return
        var favIcon = R.drawable.ic_baseline_favorite_border_36
        if (isFavourite) favIcon = R.drawable.ic_baseline_favorite_36

        binding.favouriteHobbyBtn.setImageResource(favIcon)
    }

    private fun displayHobbyImage(imageName: String?) {
        imagesRepository.loadHobbyImageInto(imageName, binding.hobbyDetailsImage)
    }

    private fun showException(exceptionMessage: String?) {
        if (exceptionMessage != null) {
            Toast.makeText(this, exceptionMessage, Toast.LENGTH_SHORT).show()
            viewModel.onExceptionShowed()
        }
    }
}
