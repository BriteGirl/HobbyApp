package pl.com.britenet.hobbyapp.userhobbies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import pl.com.britenet.hobbyapp.HobbiesListAdapter
import pl.com.britenet.hobbyapp.HobbiesListAdapterFactory
import pl.com.britenet.hobbyapp.data.Hobby
import pl.com.britenet.hobbyapp.databinding.FragmentUserHobbiesBinding
import javax.inject.Inject

@AndroidEntryPoint
class UserHobbiesFragment : Fragment() {
    private val viewModel: UserHobbiesViewModel by viewModels()
    private lateinit var binding: FragmentUserHobbiesBinding
    @Inject lateinit var adapterFactory: HobbiesListAdapterFactory
    lateinit var rvAdapter: HobbiesListAdapter
    private val gridColumnNum = 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentUserHobbiesBinding.inflate(inflater, container, false)
        rvAdapter = adapterFactory.createAdapter()

        binding.userHobbiesRv.layoutManager = GridLayoutManager(context, gridColumnNum)
        binding.userHobbiesRv.adapter = rvAdapter

        viewModel.userHobbies.observe(viewLifecycleOwner) { updateHobbies(it) }
        viewModel.isUserSignedIn.observe(viewLifecycleOwner) { toggleView(it) }

        return binding.root
    }

    private fun toggleView(isUserSignedIn: Boolean) {
        if (isUserSignedIn)
            binding.signedInOnlyWarning.visibility = View.GONE
        else
            binding.signedInOnlyWarning.visibility = View.VISIBLE
    }

    private fun updateHobbies(newData: List<Hobby>?) {
        if (newData != null) {
            rvAdapter.updateData(newData)
        }
    }
}
