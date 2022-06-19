package ru.netology.inmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import ru.netology.inmedia.adapter.OnInteractionListener
import ru.netology.inmedia.adapter.PostAdapter
import ru.netology.inmedia.databinding.FragmentUserPageBinding
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.dto.User
import ru.netology.inmedia.service.UserArg
import ru.netology.inmedia.viewmodel.UserViewModel
import ru.netology.inmedia.viewmodel.WallViewModel

class UserPageFragment : Fragment() {

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

        arguments?.showUser?.let { user: User ->
            with(binding) {
                userName.text = user.name

                val wallAdapter = PostAdapter(object: OnInteractionListener {
                    override fun onLike(post: Post) {
                        wallViewModel.likePostsOnWall(user.id, post.id)
                    }

                    override fun onDisLike(post: Post) {

                    }

                    override fun onSinglePost(post: Post) {

                    }

                    override fun onEdit(post: Post) {

                    }

                    override fun onRemove(post: Post) {

                    }

                    override fun onFullScreenImage(post: Post) {

                    }

                    override fun onPlayAudio(post: Post) {

                    }

                    override fun onLink(url: String) {

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

            }

        }



        return binding.root
    }

}