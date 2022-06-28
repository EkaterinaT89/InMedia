package ru.netology.inmedia.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.R
import ru.netology.inmedia.adapter.TabsAdapter
import ru.netology.inmedia.databinding.FragmentTabsBinding

@AndroidEntryPoint
class TabsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentTabsBinding.inflate(
            inflater,
            container,
            false
        )

        with(binding) {
            tabLayout.setSelectedTabIndicatorColor(Color.WHITE)

            val numberOfTabs = 4

            tabLayout.tabMode = TabLayout.MODE_FIXED

            val adapter = TabsAdapter(childFragmentManager, lifecycle, numberOfTabs)
            tabsViewpager.adapter = adapter

            tabsViewpager.isUserInputEnabled = true

            TabLayoutMediator(tabLayout, tabsViewpager) { tab, position ->
                when (position) {
                    0 -> {
                        tab.setIcon(R.drawable.ic_outline_home_24)
                    }
                    1 -> {
                        tab.setIcon(R.drawable.ic_baseline_people_outline_24)
                    }
                    2 -> {
                        tab.setIcon(R.drawable.ic_baseline_event_note_24)
                    }
                    3 -> {
                        tab.setIcon(R.drawable.ic_outline_person_pin_24)
                    }

                }

            }.attach()

        }

        return binding.root
    }

}