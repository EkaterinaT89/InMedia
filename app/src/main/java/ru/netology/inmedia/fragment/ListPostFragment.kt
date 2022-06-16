package ru.netology.inmedia.fragment

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide.init
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.R
import ru.netology.inmedia.adapter.OnInteractionListener
import ru.netology.inmedia.adapter.PostAdapter
import ru.netology.inmedia.databinding.FragmentListPostBinding
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.viewmodel.AuthViewModel
import ru.netology.inmedia.viewmodel.PostViewModel
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.inmedia.fragment.CardPostFragment.Companion.showPost
import ru.netology.inmedia.fragment.NewPostFragment.Companion.textArg
import ru.netology.inmedia.service.MediaLifecycleObserver

private const val BASE_URL = "https://inmediadiploma.herokuapp.com/api/media"

class ListPostFragment : Fragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalCoroutinesApi::class)
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

        val authViewModel: AuthViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val mediaObserver = MediaLifecycleObserver()

        lifecycle.addObserver(mediaObserver)

        val postAdapter = PostAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                postViewModel.likeById(post.id)
            }

            override fun onDisLike(post: Post) {
                postViewModel.disLikeById(post.id)
            }

            override fun onSinglePost(post: Post) {
                findNavController().navigate(R.id.cardPostFragment,
                    Bundle().apply {
                        showPost = post
                    })
            }

            override fun onEdit(post: Post) {
                postViewModel.edit(post)
                binding.addNewPost.visibility = View.GONE
                findNavController().navigate(
                    R.id.editPostFragment,
                    Bundle().apply {
                        textArg = post.content
                    })
            }

            override fun onRemove(post: Post) {
                postViewModel.removeById(post.id)
            }

            override fun onFullScreenImage(post: Post) {
                findNavController().navigate(R.id.imageFragment)
            }

            override fun onPlayAudio(post: Post) {
                mediaObserver.apply {
                    player?.setDataSource(
                        BASE_URL
                    )
                }.play()
            }

            override fun onPlayVideo(post: Post) {

            }

        })

        binding.postsContainer.adapter = postAdapter

        postViewModel.data.observe(viewLifecycleOwner, { state ->
            postAdapter.submitList(state.posts)
        })

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

        with(binding) {
            swiperefresh.setOnRefreshListener {
                postViewModel.loadPosts()
            }

            addNewPost.setOnClickListener {
                if (authViewModel.authenticated) {
                    findNavController().navigate(R.id.newPostFragment)
                } else {
                    findNavController().navigate(R.id.authFragment)
                }
            }
        }



        return binding.root
    }

}