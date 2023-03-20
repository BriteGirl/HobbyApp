package pl.com.britenet.hobbyapp.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import pl.com.britenet.hobbyapp.R
import pl.com.britenet.hobbyapp.databinding.ActivityAdminPanelBinding

@AndroidEntryPoint
class AdminPanelActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminPanelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminPanelBinding.inflate(layoutInflater)

        // set up tabs
        binding.adminPanelViewPager.adapter = AdminPanelViewAdapter(supportFragmentManager, lifecycle)
        TabLayoutMediator(binding.adminPanelTabLayout, binding.adminPanelViewPager) {
            tab, position ->
            val titleResId = getTabTitleResId(position)
            tab.setText(titleResId)
        }.attach()

        setContentView(binding.root)
    }

    private fun getTabTitleResId(position: Int): Int {
        return when (position) {
            0 -> R.string.tab_admins_label
            1 -> R.string.tab_hobbies_suggestions_label
            else -> R.string.tab_bug_reports_label
        }
    }
}
