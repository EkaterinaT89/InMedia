package ru.netology.inmedia.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.netology.inmedia.fragment.*

class TabsAdapter(fm: FragmentManager, lifecycle: Lifecycle, private var numberOfTabs: Int) : FragmentStateAdapter(fm, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                // # Posts
                val bundle = Bundle()
                val listPostFragment = ListPostFragment()
                listPostFragment.arguments = bundle
                return listPostFragment
            }
            1 -> {
                // # People
                val bundle = Bundle()
                val usersFragment = ListUsersFragment()
                usersFragment.arguments = bundle
                return usersFragment
            }
            2 -> {
                // # Events
                val bundle = Bundle()
                val eventsFragment = ListEventsFragment()
                eventsFragment.arguments = bundle
                return eventsFragment
            }
            3 -> {
                // # MyPage
                val bundle = Bundle()
                val myPageFragment = MyPageFragment()
                myPageFragment.arguments = bundle
                return myPageFragment
            }
            else -> return TabsFragment()
        }

    }

    override fun getItemCount(): Int {
        return numberOfTabs
    }
}