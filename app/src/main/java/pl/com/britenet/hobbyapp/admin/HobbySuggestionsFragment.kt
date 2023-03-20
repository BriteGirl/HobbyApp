package pl.com.britenet.hobbyapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import pl.com.britenet.hobbyapp.HobbiesListAdapter
import pl.com.britenet.hobbyapp.HobbiesListAdapterFactory
import pl.com.britenet.hobbyapp.data.images.ImagesRepository
import pl.com.britenet.hobbyapp.databinding.FragmentHobbySuggestionsBinding
import pl.com.britenet.hobbyapp.utils.setOnSafeClickListener
import javax.inject.Inject

@AndroidEntryPoint
class HobbySuggestionsFragment : Fragment() {
    private lateinit var binding: FragmentHobbySuggestionsBinding
    private val viewModel: HobbySuggestionsViewModel by viewModels()
    @Inject lateinit var adapterFactory: HobbiesListAdapterFactory
    lateinit var hobbiesRvAdapter: HobbiesListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHobbySuggestionsBinding.inflate(inflater, container, false)
        hobbiesRvAdapter = adapterFactory.createAdapter(ImagesRepository.ImageFolder.SUGGESTIONS_FOLDER)

        setUpRecyclerView()
        enableButtons()

        viewModel.hobbies.observe(viewLifecycleOwner) { hobbiesRvAdapter.updateData(it ?: emptyList()) }
        viewModel.exceptionToShow.observe(viewLifecycleOwner) { showException(it) }

        return binding.root
    }

    private fun showException(message: String?) {
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onExceptionShowed()
        }
    }

    private fun enableButtons() {
        binding.hobbySuggestionsAcceptBtn.setOnSafeClickListener {
            val checkedHobbies = hobbiesRvAdapter.getCheckedHobbies()
            viewModel.acceptOrRejectHobbySuggestions(true, checkedHobbies)
        }
        binding.hobbySuggestionsRejectBtn.setOnSafeClickListener {
            val checkedHobbies = hobbiesRvAdapter.getCheckedHobbies()
            viewModel.acceptOrRejectHobbySuggestions(false, checkedHobbies)
        }
    }

    private fun setUpRecyclerView() {
        binding.hobbiesSuggestionsRv.layoutManager = GridLayoutManager(this.context, 1)
        binding.hobbiesSuggestionsRv.adapter = hobbiesRvAdapter
        hobbiesRvAdapter.enableCheckboxes()
    }
}
