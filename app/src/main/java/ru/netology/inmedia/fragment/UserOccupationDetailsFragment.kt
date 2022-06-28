package ru.netology.inmedia.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.databinding.FragmentUserOccupationDetailsBinding
import ru.netology.inmedia.dto.Job
import ru.netology.inmedia.service.JobArg

@AndroidEntryPoint
class UserOccupationDetailsFragment : Fragment() {

    companion object {
        var Bundle.showOneJob: Job? by JobArg
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentUserOccupationDetailsBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.showOneJob?.let { job: Job ->
            with(binding) {
                position.text = job.position
                company.text = job.name
                start.text = "С " + job.start.toString()

                if (job.finish != null) {
                    end.text = "По " + job.finish.toString()
                } else {
                    end.visibility = View.GONE
                }
            }
        }

        return binding.root
    }

}