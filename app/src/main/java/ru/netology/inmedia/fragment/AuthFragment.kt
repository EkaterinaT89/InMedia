package ru.netology.inmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.R
import ru.netology.inmedia.databinding.FragmentAuthBinding
import ru.netology.inmedia.viewmodel.SignInViewModel

@AndroidEntryPoint
class AuthFragment : Fragment() {

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

        val signInViewModel: SignInViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        with(binding) {
            signInButton.setOnClickListener {
                val login = loginInput.text?.trim().toString()
                val password = passwordInput.text?.trim().toString()
                signInViewModel.signeIn(login, password)
                findNavController().navigate(R.id.tabsFragment)
            }

            signUpButton.setOnClickListener {
                findNavController().navigate(R.id.signUpFragment)
            }
        }

        return binding.root
    }

}