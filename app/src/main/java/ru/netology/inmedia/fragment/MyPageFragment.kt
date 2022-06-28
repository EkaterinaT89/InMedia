package ru.netology.inmedia.fragment

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.R
import ru.netology.inmedia.adapter.OnInteractionListener
import ru.netology.inmedia.adapter.PostAdapter
import ru.netology.inmedia.adapter.PostRecyclerView
import ru.netology.inmedia.auth.AppAuth
import ru.netology.inmedia.databinding.FragmentMyPageBinding
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.fragment.CardPostFragment.Companion.showPost
import ru.netology.inmedia.fragment.ListUserOccupationFragment.Companion.showUser
import ru.netology.inmedia.fragment.NewPostFragment.Companion.textArg
import ru.netology.inmedia.service.MediaLifecycleObserver
import ru.netology.inmedia.util.MediaUtils
import ru.netology.inmedia.viewmodel.PostViewModel
import ru.netology.inmedia.viewmodel.UserViewModel
import ru.netology.inmedia.viewmodel.WallViewModel
import javax.inject.Inject

private const val BASE_URL = "https://inmediadiploma.herokuapp.com/api/media"

@AndroidEntryPoint
class MyPageFragment : Fragment() {

    private lateinit var recyclerView: PostRecyclerView

    @Inject
    lateinit var auth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentMyPageBinding.inflate(
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

        val userViewModel: UserViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val mediaObserver = MediaLifecycleObserver()

        lifecycle.addObserver(mediaObserver)

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            with(binding) {
                val url = "https://inmediadiploma.herokuapp.com/api"

                userName.text = user.name

                if (user.avatar == null) {
                    avatarInput.setImageResource(R.drawable.ic_baseline_person_pin_24)
                } else {
                    MediaUtils.loadUserAvatar(avatarInput, url, user)
                }

                userName.text = user.name

                getOccupationList.setOnClickListener {
                    findNavController().navigate(R.id.listMyOccupationFragment,
                        Bundle().apply {
                            showUser = user
                        })
                }

                addNewOccupation.setOnClickListener {
                    findNavController().navigate(R.id.newJobFragment,
                        Bundle().apply {
                            showUser = user
                        })
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

                lifecycleScope.launchWhenCreated {
                    wallViewModel.getWall(user.id)
                }

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

                menuButton.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.my_page_menu)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.sign_out -> {
                                    AlertDialog.Builder(requireContext()).setMessage("Уверены?")
                                        .setPositiveButton("Выйти"
                                        ) { dialogInterface, i ->
                                            auth.removeAuth()
                                            findNavController().navigate(R.id.tabsFragment)
                                        }
                                        .setNegativeButton("Остаться"
                                        ) { dialogInterface, i ->
                                            return@setNegativeButton
                                        }
                                        .show()
                                    true
                                }
                                R.id.load_avatar -> {

                                    val pickPhotoLauncher =
                                        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                                            when (it.resultCode) {
                                                ImagePicker.RESULT_ERROR -> {
                                                    Snackbar.make(
                                                        binding.root,
                                                        ImagePicker.getError(it.data),
                                                        Snackbar.LENGTH_LONG
                                                    ).show()
                                                }
                                                Activity.RESULT_OK -> {
                                                    val uri: Uri? = it.data?.data
                                                    userViewModel.saveUserAvatar(user, uri, uri?.toFile())
                                                }
                                            }
                                        }

                                    parentFragment?.let { it1 ->
                                        ImagePicker.with(it1)
                                            .crop()
                                            .compress(2048)
                                            .provider(ImageProvider.GALLERY)
                                            .galleryMimeTypes(
                                                arrayOf(
                                                    "image/png",
                                                    "image/jpeg",
                                                )
                                            )
                                            .createIntent(pickPhotoLauncher::launch)
                                    }



                                    true
                                }
                                else -> false
                            }
                        }
                    }.show()
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