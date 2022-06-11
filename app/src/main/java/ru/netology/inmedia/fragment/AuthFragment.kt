package ru.netology.inmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.R
import ru.netology.inmedia.databinding.FragmentAuthBinding

@AndroidEntryPoint
class AuthFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentAuthBinding.inflate(
            inflater,
            container,
            false
        )

        with(binding) {
            signUpButton.setOnClickListener {
                findNavController().navigate(R.id.signUpFragment)
            }
        }

        return binding.root
    }

}