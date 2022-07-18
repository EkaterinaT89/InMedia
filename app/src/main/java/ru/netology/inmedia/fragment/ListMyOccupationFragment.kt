package ru.netology.inmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.R
import ru.netology.inmedia.adapter.MyJobAdapter
import ru.netology.inmedia.adapter.MyJobListener
import ru.netology.inmedia.databinding.FragmentListMyOccupationBinding
import ru.netology.inmedia.dto.Job
import ru.netology.inmedia.fragment.EditMyJobFragment.Companion.jobArg
import ru.netology.inmedia.fragment.UserOccupationDetailsFragment.Companion.showOneJob
import ru.netology.inmedia.viewmodel.JobViewModel
import ru.netology.inmedia.viewmodel.UserViewModel

@AndroidEntryPoint
class ListMyOccupationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentListMyOccupationBinding.inflate(
            inflater,
            container,
            false
        )

        val jobViewModel: JobViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val userViewModel: UserViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        userViewModel.user.observe(viewLifecycleOwner) { user ->

            lifecycleScope.launchWhenCreated {
                jobViewModel.getAllJobs(user.id)
            }

            val jobAdapter = MyJobAdapter(object : MyJobListener {
                override fun onSingleJob(job: Job) {
                    findNavController().navigate(
                        R.id.cardMyOccupationFragment,
                        Bundle().apply
                        {
                            showOneJob = job
                        })
                }

                override fun onEdit(job: Job) {
                    jobViewModel.editJob(job)
                    findNavController().navigate(
                        R.id.editMyJobFragment,
                        Bundle().apply
                        {
                            jobArg = job

                        })
                }

                override fun onDelete(job: Job) {
                    jobViewModel.removeJobById(job.id)
                }

            })

            binding.jobsContainer.adapter = jobAdapter

            jobViewModel.data.observe(viewLifecycleOwner) { jobs ->
                jobAdapter.submitList(jobs)
            }

        }

        jobViewModel.dataState.observe(viewLifecycleOwner) { state ->
            with(binding) {
                progress.isVisible = state.loading
                swiperefresh.isRefreshing = state.refreshing
                if (state.error) {
                    error.visibility = View.VISIBLE
                    tryAgainButton.setOnClickListener {
                        jobViewModel.tryAgain()
                        error.visibility = View.GONE
                    }
                }
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            jobViewModel.retryGetAllJobs()
        }

        return binding.root
    }

}