package ru.netology.inmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.netology.inmedia.R
import ru.netology.inmedia.databinding.FragmentUserOccupationDetailsBinding
import ru.netology.inmedia.dto.Job
import ru.netology.inmedia.service.JobArg
import ru.netology.inmedia.util.DateFormatter

class UserOccupationDetailsFragment : Fragment() {

    companion object {
        var Bundle.showOneJob: Job? by JobArg
    }

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
                start.text = DateFormatter.getCurrentDate(start, job.start)

                if (job.finish == null) {
                    end.text = getString(R.string.till_now)
                } else {
                    end.text = DateFormatter.getCurrentDate(end, job.finish)
                }
            }
        }

        return binding.root
    }

}