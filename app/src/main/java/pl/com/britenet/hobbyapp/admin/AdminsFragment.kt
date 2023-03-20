package pl.com.britenet.hobbyapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import pl.com.britenet.hobbyapp.databinding.FragmentAdminsBinding

@AndroidEntryPoint
class AdminsFragment : Fragment() {
    private val viewModel: AdminsFragmentViewModel by viewModels()
    private lateinit var binding: FragmentAdminsBinding
    private lateinit var rvAdapter: AdminListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminsBinding.inflate(inflater, container, false)
        rvAdapter = AdminListAdapter()
        binding.adminListRv.layoutManager = LinearLayoutManager(context)
        binding.adminListRv.adapter = rvAdapter

        viewModel.exceptionToShow.observe(viewLifecycleOwner) { showException(it) }
        viewModel.admins.observe(viewLifecycleOwner) { rvAdapter.updateAdminsList(it) }

        return binding.root
    }

    private fun showException(message: String?) {
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onExceptionDisplayed()
        }
    }
}
