package ru.netology.inmedia.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.netology.inmedia.fragment.*

class TabsAdapter(fm: FragmentManager, lifecycle: Lifecycle, private var numberOfTabs: Int) :
    FragmentStateAdapter(fm, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                ListPostFragment()
            }
            1 -> {
                ListUsersFragment()
            }
            2 -> {
                ListEventsFragment()
            }
            3 -> {
                MyPageFragment()
            }
            else -> TabsFragment()
        }

    }

    override fun getItemCount(): Int {
        return numberOfTabs
    }
}