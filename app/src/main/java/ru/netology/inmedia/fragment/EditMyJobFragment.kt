package ru.netology.inmedia.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.R
import ru.netology.inmedia.databinding.FragmentEditJobBinding
import ru.netology.inmedia.dto.Job
import ru.netology.inmedia.dto.User
import ru.netology.inmedia.enumeration.EventType
import ru.netology.inmedia.service.AndroidUtils
import ru.netology.inmedia.service.JobArg
import ru.netology.inmedia.service.UserArg
import ru.netology.inmedia.viewmodel.JobViewModel

@AndroidEntryPoint
class EditMyJobFragment : Fragment() {

    companion object {
        var Bundle.jobArg: Job? by JobArg
        var Bundle.userArg: User? by UserArg
    }

    private val viewModel: JobViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    var eventType: EventType? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentEditJobBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.jobArg?.let { job ->
            arguments?.userArg?.let { user ->

                with(binding) {

                    saveButton.setOnClickListener {
                        viewModel.editJobContent(
                            companyName = companyNameInput.text.toString(),
                            position = positionInput.text.toString(),
                            start = startInput.text.toString(),
                            end = endInput.text.toString()
                        )

                        viewModel.createNewJob(user.id)
                        AndroidUtils.hideKeyboard(requireView())

                        findNavController().navigate(R.id.tabsFragment)
                    }

                    cancelButton.setOnClickListener {
                        AndroidUtils.hideKeyboard(it)
                        findNavController().navigate(R.id.tabsFragment)

                    }

                }

            }
        }

        return binding.root
    }

}