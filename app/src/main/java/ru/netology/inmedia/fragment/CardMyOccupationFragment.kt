package ru.netology.inmedia.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.R
import ru.netology.inmedia.databinding.FragmentCardMyOccupationBinding
import ru.netology.inmedia.dto.Job
import ru.netology.inmedia.fragment.EditMyJobFragment.Companion.jobArg
import ru.netology.inmedia.service.JobArg
import ru.netology.inmedia.viewmodel.JobViewModel

@AndroidEntryPoint
class CardMyOccupationFragment: Fragment() {

    companion object {
        var Bundle.showOneJob: Job? by JobArg
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentCardMyOccupationBinding.inflate(
            inflater,
            container,
            false
        )

        val jobViewModel: JobViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        arguments?.showOneJob?.let { job: Job ->
            with(binding) {
                position.text = job.position
                company.text = job.name
                start.text = "С " + job.start.toString()

                if(job.finish != null) {
                    end.text = "По " + job.finish.toString()
                } else {
                    end.visibility = View.GONE
                }

                menuButton.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.menu)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.delete -> {
                                    jobViewModel.removeJobById(job.id)
                                    true
                                }
                                R.id.edit -> {
                                    findNavController().navigate(
                                        R.id.editMyJobFragment,
                                        Bundle().apply
                                        {
                                            jobArg = job
                                        })
                                    true
                                }
                                else -> false
                            }
                        }
                    }.show()
                }
            }

        }

        return binding.root
    }

}