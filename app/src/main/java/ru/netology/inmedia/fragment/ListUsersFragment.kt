package ru.netology.inmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.R
import ru.netology.inmedia.adapter.OnUserListener
import ru.netology.inmedia.adapter.UserAdapter
import ru.netology.inmedia.dto.User
import ru.netology.inmedia.fragment.UserPageFragment.Companion.showUser
import ru.netology.inmedia.viewmodel.UserViewModel

@AndroidEntryPoint
class ListUsersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = ru.netology.inmedia.databinding.FragmentListUsersBinding.inflate(
            inflater,
            container,
            false
        )

        val viewModel: UserViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val userAdapter = UserAdapter(object : OnUserListener {
            override fun onSingleUser(user: User) {
                findNavController().navigate(R.id.userPageFragment,
                    Bundle().apply {
                        showUser = user
                    })
            }

        })

        binding.usersContainer.adapter = userAdapter

        viewModel.data.observe(viewLifecycleOwner, { users ->
            userAdapter.submitList(users)
        })

        viewModel.dataState.observe(viewLifecycleOwner, { state ->
            with(binding) {
                progress.isVisible = state.loading
                if (state.error) {
                    error.visibility = View.VISIBLE
                    tryAgainButton.setOnClickListener {
                        viewModel.tryAgain()
                        error.visibility = View.GONE
                    }
                }
            }
        })

        return binding.root
    }

}