package pl.com.britenet.hobbyapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import pl.com.britenet.hobbyapp.addhobby.NewHobbyActivity
import pl.com.britenet.hobbyapp.data.Hobby
import pl.com.britenet.hobbyapp.databinding.FragmentMainBinding
import pl.com.britenet.hobbyapp.hobby.HobbyDetailsActivity
import pl.com.britenet.hobbyapp.hobby.HobbyDetailsViewModel.Companion.HOBBY_ID_EXTRA
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding
    @Inject lateinit var adapterFactory: HobbiesListAdapterFactory
    lateinit var hobbiesRvAdapter: HobbiesListAdapter
    private val gridColumnNum = 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        hobbiesRvAdapter = adapterFactory.createAdapter()

        binding.hobbiesRv.layoutManager = GridLayoutManager(this.context, gridColumnNum)
        hobbiesRvAdapter.setItemClickListener(viewModel::onHobbyClicked)
        binding.hobbiesRv.adapter = hobbiesRvAdapter

        binding.addHobbyFab.setOnClickListener { onAddHobbyClicked() }
        viewModel.hobbies.observe(this.viewLifecycleOwner) { hobbiesList -> updateHobbies(hobbiesList) }
        viewModel.hobbyToShow.observe(this.viewLifecycleOwner) { hobbyId -> showHobbyDetails(hobbyId) }

        return binding.root
    }

    private fun showHobbyDetails(hobbyId: String?) {
        if (hobbyId != null) {
            val intent = Intent(requireContext(), HobbyDetailsActivity::class.java)
            intent.putExtra(HOBBY_ID_EXTRA, hobbyId)
            viewModel.onHobbyDetailsActivityStart()
            startActivity(intent)
        }
    }

    private fun updateHobbies(hobbiesList: List<Hobby>?) {
        if (hobbiesList != null) {
            hobbiesRvAdapter.updateData(hobbiesList)
        }
    }

    private fun onAddHobbyClicked() {
        val intent = Intent(context, NewHobbyActivity::class.java)
        startActivity(intent)
    }
}
