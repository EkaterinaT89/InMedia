package ru.netology.inmedia.fragment

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.R
import ru.netology.inmedia.databinding.FragmentCardPostBinding
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.enumeration.AttachmentType
import ru.netology.inmedia.fragment.NewPostFragment.Companion.textArg
import ru.netology.inmedia.service.MediaLifecycleObserver
import ru.netology.inmedia.service.PostArg
import ru.netology.inmedia.service.PostService
import ru.netology.inmedia.viewmodel.PostViewModel

private const val BASE_URL = "https://inmediadiploma.herokuapp.com/api/media"

class CardPostFragment : Fragment() {

    companion object {
        var Bundle.showPost: Post? by PostArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentCardPostBinding.inflate(
            inflater,
            container,
            false
        )

        val mediaObserver = MediaLifecycleObserver()

        lifecycle.addObserver(mediaObserver)

        arguments?.showPost?.let { post: Post ->
            with(binding) {
                val url = "https://inmediadiploma.herokuapp.com/api/"

                authorName.text = post.author
                date.text = post.published
                contentPost.text = post.content
                likes.text = PostService.countPresents(post.likeOwnerIds)

                likes.isChecked = post.likedByMe

                if (post.attachment == null) {
                    groupForVideo.visibility = View.GONE
                    playAudio.visibility = View.GONE
                    imageContainer.visibility = View.GONE
                } else {
                    when (post.attachment.type) {
                        AttachmentType.VIDEO -> groupForVideo.visibility = View.VISIBLE
                        AttachmentType.AUDIO -> playAudio.visibility = View.VISIBLE
                        AttachmentType.IMAGE -> imageContainer.visibility = View.VISIBLE
                    }
                }

                Glide.with(imageContainer)
                    .load("$url/media/${post.attachment?.url}")
                    .error(R.drawable.ic_error)
                    .placeholder(R.drawable.ic_loading)
                    .timeout(10_000)
                    .into(imageContainer)

                Glide.with(avatar)
                    .load("$url/avatars/${post.authorAvatar}")
                    .error(R.drawable.ic_error)
                    .placeholder(R.drawable.ic_loading)
                    .circleCrop()
                    .timeout(10_000)
                    .into(avatar)

                likes.setOnClickListener {
                    if (!post.likedByMe) {
                        viewModel.likeById(post.id)
                    } else {
                        viewModel.disLikeById(post.id)
                    }

                }

                playAudio.setOnClickListener {
                    mediaObserver.apply {
                        player?.setDataSource(
                            "$url/media/${post.attachment?.url}"
                        )
                    }.play()
                }

                videoContainer.apply {
                    setMediaController(MediaController(context))
                    setVideoURI(
                        Uri.parse("$url/media/${post.attachment?.url}")
                    )
                    setOnPreparedListener {
                        start()
                    }
                    setOnCompletionListener {
                        stopPlayback()
                    }
                }

                imageContainer.setOnClickListener {
                    findNavController().navigate(R.id.imageFragment)
                }

                menuButton.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

                menuButton.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.menu)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.delete -> {
                                    viewModel.removeById(post.id)
                                    true
                                }
                                R.id.edit -> {
                                    viewModel.edit(post)
                                    findNavController().navigate(
                                        R.id.editPostFragment,
                                        Bundle().apply {
                                            textArg = post.content
                                        })
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

}