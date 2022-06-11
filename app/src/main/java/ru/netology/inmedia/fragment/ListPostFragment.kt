package ru.netology.inmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide.init
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.adapter.OnInteractionListener
import ru.netology.inmedia.adapter.PostAdapter
import ru.netology.inmedia.databinding.FragmentListPostBinding
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class ListPostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentListPostBinding.inflate(
            inflater,
            container,
            false
        )

        val postViewModel: PostViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val postAdapter = PostAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                postViewModel.likeById(post.id)
            }

            override fun onDisLike(post: Post) {
                postViewModel.disLikeById(post.id)
            }

            override fun onSinglePost(post: Post) {

            }

            override fun onEdit(post: Post) {
                postViewModel.edit(post)
            }

            override fun onRemove(post: Post) {
                postViewModel.removeById(post.id)
            }

        })

        binding.postsContainer.adapter = postAdapter

        postViewModel.dataState.observe(viewLifecycleOwner, { state ->
            with(binding) {
                progress.isVisible = state.loading
                swiperefresh.isRefreshing = state.refreshing
                if (state.error) {
                    error.visibility = View.VISIBLE
                    tryAgainButton.setOnClickListener {
                        postViewModel.tryAgain()
                        error.visibility = View.GONE
                    }
                }
            }
        })

        return binding.root
    }

}