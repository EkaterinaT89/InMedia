package ru.netology.inmedia.fragment

import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import dagger.hilt.android.AndroidEntryPoint
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import ru.netology.inmedia.R
import ru.netology.inmedia.databinding.FragmentCardPostBinding
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.enumeration.AttachmentType
import ru.netology.inmedia.fragment.NewPostFragment.Companion.textArg
import ru.netology.inmedia.service.MediaLifecycleObserver
import ru.netology.inmedia.service.PostArg
import ru.netology.inmedia.service.PostService
import ru.netology.inmedia.util.MediaUtils
import ru.netology.inmedia.viewmodel.AuthViewModel
import ru.netology.inmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class CardPostFragment : Fragment() {

    companion object {
        var Bundle.showPost: Post? by PostArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    val authViewModel: AuthViewModel by viewModels(
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

                val parentView = binding.root
                val videoThumbnail = videoContainer

                val url = "https://inmediadiploma.herokuapp.com/api/"

                authorName.text = post.author
                date.text = post.published
                contentPost.text = post.content
                Linkify.addLinks(contentPost, Linkify.ALL)
                contentPost.movementMethod = BetterLinkMovementMethod.getInstance()
                BetterLinkMovementMethod.linkify(Linkify.WEB_URLS, contentPost)
                    .setOnLinkClickListener { textView, url ->
                        CustomTabsIntent.Builder()
                            .setShowTitle(true)
                            .build()
                            .launchUrl(requireContext(), Uri.parse(url))
                        true
                    }

                likes.text = PostService.countPresents(post.likeOwnerIds)

                likes.isChecked = post.likedByMe

                if (post.attachment == null) {
                    groupForVideo.visibility = View.GONE
                    playAudio.visibility = View.GONE
                    imageContainer.visibility = View.GONE
                } else {
                    when (post.attachment.type) {
                        AttachmentType.VIDEO -> {
                            groupForVideo.visibility = View.VISIBLE
                            groupForVideo.visibility = View.VISIBLE
                            playAudio.visibility = View.GONE
                            imageContainer.visibility = View.GONE
                            MediaItem.fromUri(post.attachment.url)
                            Glide.with(parentView).load(post.attachment.url).into(videoThumbnail)
                        }
                        AttachmentType.AUDIO -> {
                            playAudio.visibility = View.VISIBLE
                            groupForVideo.visibility = View.GONE
                            imageContainer.visibility = View.GONE
                        }
                        AttachmentType.IMAGE -> {
                            imageContainer.visibility = View.VISIBLE
                            groupForVideo.visibility = View.GONE
                            playAudio.visibility = View.GONE
                            MediaUtils.loadPostImage(imageContainer, url, post)
                        }
                    }
                }

                likes.setOnClickListener {
                    if (authViewModel.authenticated) {
                        if (!post.likedByMe) {
                            viewModel.likeById(post.id)
                        } else {
                            viewModel.disLikeById(post.id)
                        }
                    }
                }

                playAudio.setOnClickListener {
                    mediaObserver.apply {
                        player?.setDataSource(
                            "$url/media/${post.attachment?.url}"
                        )
                    }.play()
                }

                if (post.authorAvatar == null) {
                    avatar.setImageResource(R.drawable.ic_baseline_person_pin_24)
                } else {
                    MediaUtils.loadPostAvatar(avatar, url, post)
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