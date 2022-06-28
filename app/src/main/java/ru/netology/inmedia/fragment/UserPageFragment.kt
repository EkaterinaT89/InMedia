package ru.netology.inmedia.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.R
import ru.netology.inmedia.adapter.OnInteractionListener
import ru.netology.inmedia.adapter.PostAdapter
import ru.netology.inmedia.adapter.PostRecyclerView
import ru.netology.inmedia.databinding.FragmentUserPageBinding
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.dto.User
import ru.netology.inmedia.fragment.CardPostFragment.Companion.showPost
import ru.netology.inmedia.fragment.NewPostFragment.Companion.textArg
import ru.netology.inmedia.service.MediaLifecycleObserver
import ru.netology.inmedia.service.UserArg
import ru.netology.inmedia.util.MediaUtils
import ru.netology.inmedia.viewmodel.JobViewModel
import ru.netology.inmedia.viewmodel.PostViewModel
import ru.netology.inmedia.viewmodel.WallViewModel

private const val BASE_URL = "https://inmediadiploma.herokuapp.com/api/media"

@AndroidEntryPoint
class UserPageFragment : Fragment() {

    private lateinit var recyclerView: PostRecyclerView

    companion object {
        var Bundle.showUser: User? by UserArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentUserPageBinding.inflate(
            inflater,
            container,
            false
        )

        val wallViewModel: WallViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val postViewModel: PostViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val jobViewModel: JobViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val mediaObserver = MediaLifecycleObserver()

        lifecycle.addObserver(mediaObserver)

        arguments?.showUser?.let { user: User ->
            with(binding) {
                val url = "https://inmediadiploma.herokuapp.com/api"

                userName.text = user.name

                if (user.avatar == null) {
                    avatarInput.setImageResource(R.drawable.ic_baseline_person_pin_24)
                } else {
                    MediaUtils.loadUserAvatar(avatarInput, url, user)
                }

                val wallAdapter = PostAdapter(object : OnInteractionListener {
                    override fun onLike(post: Post) {
                        wallViewModel.likePostsOnWall(user.id, post.id)
                    }

                    override fun onDisLike(post: Post) {
                        wallViewModel.disLikePostsOnWall(user.id, post.id)
                    }

                    override fun onSinglePost(post: Post) {
                        findNavController().navigate(
                            R.id.cardPostFragment,
                            Bundle().apply {
                                showPost = post
                            })
                    }

                    override fun onEdit(post: Post) {
                        postViewModel.edit(post)
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

                    override fun onLink(url: String) {
                        CustomTabsIntent.Builder()
                            .setShowTitle(true)
                            .build()
                            .launchUrl(requireContext(), Uri.parse(url))
                    }

                })

                postsContainer.adapter = wallAdapter

                wallViewModel.data.observe(viewLifecycleOwner, { wall ->
                    wallAdapter.submitList(wall)
                })

                wallViewModel.dataState.observe(viewLifecycleOwner, { state ->
                    with(binding) {
                        progress.isVisible = state.loading
                        if (state.error) {
                            error.visibility = View.VISIBLE
                            tryAgainButton.setOnClickListener {
                                wallViewModel.tryAgain()
                                error.visibility = View.GONE
                            }
                        }
                    }
                })

                lifecycleScope.launchWhenCreated {
                    wallViewModel.getWall(user.id)
                }

                getOccupationList.setOnClickListener {
                    findNavController().navigate(R.id.listUserOccupationFragment,
                        Bundle().apply {
                            showUser = user
                        })
                }

            }

        }

        return binding.root
    }

    override fun onResume() {
        if (::recyclerView.isInitialized) recyclerView.createPlayer()
        super.onResume()
    }

    override fun onPause() {
        if (::recyclerView.isInitialized) recyclerView.releasePlayer()
        super.onPause()
    }


    override fun onStop() {
        if (::recyclerView.isInitialized) recyclerView.releasePlayer()
        super.onStop()
    }

}