package ru.netology.inmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.databinding.FragmentOneUserOccupationBinding

class OneUserOccupationFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentOneUserOccupationBinding.inflate(
            inflater,
            container,
            false
        )

        with(binding) {

        }

        return binding.root
    }

}