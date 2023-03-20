package pl.com.britenet.hobbyapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pl.com.britenet.hobbyapp.databinding.FragmentBugReportsBinding

class BugReportsFragment : Fragment() {
    private lateinit var binding: FragmentBugReportsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentBugReportsBinding.inflate(inflater, container, false)

        return binding.root
    }
}
