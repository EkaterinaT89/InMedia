package ru.netology.inmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.databinding.FragmentImageBinding
import ru.netology.inmedia.fragment.NewPostFragment.Companion.textArg
import ru.netology.inmedia.service.imageLoad

private const val BASE_URL = "https://inmediadiploma.herokuapp.com/api/"

@AndroidEntryPoint
class ImageFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentImageBinding.inflate(
            inflater,
            container,
            false
        )

        with(binding) {
            arguments?.textArg.let {
                showImage.imageLoad("${BASE_URL}/media/$it")
            }

        }

        return binding.root
    }

}