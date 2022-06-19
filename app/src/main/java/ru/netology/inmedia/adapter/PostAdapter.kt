package ru.netology.inmedia.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import ru.netology.inmedia.R
import ru.netology.inmedia.databinding.FragmentCardPostBinding
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.enumeration.AttachmentType
import ru.netology.inmedia.service.PostService
import com.google.android.exoplayer2.MediaItem
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import ru.netology.inmedia.util.MediaUtils

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onDisLike(post: Post)
    fun onSinglePost(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
    fun onFullScreenImage(post: Post)
    fun onPlayAudio(post: Post)
    fun onLink(url: String)
}

class PostAdapter
    (private val onInteractionListener: OnInteractionListener) :
    ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            FragmentCardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

}

class PostViewHolder(
    private val binding: FragmentCardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    val parentView = binding.root
    val videoThumbnail = binding.videoContainer
    val videoContainer = binding.groupForVideo
    val videoProgressBar = binding.videoProgress
    var videoPreview: MediaItem? = null
    val videoPlayIcon: MaterialButton = binding.playVideo

    fun bind(post: Post) {
        val url = "https://inmediadiploma.herokuapp.com/api"

        binding.apply {
            authorName.text = post.author
            date.text = post.published
            contentPost.text = post.content
            Linkify.addLinks(contentPost, Linkify.ALL)
            contentPost.movementMethod = BetterLinkMovementMethod.getInstance()
            BetterLinkMovementMethod.linkify(Linkify.WEB_URLS, contentPost)
                .setOnLinkClickListener { textView, url ->
                    onInteractionListener.onLink(url)
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
                        playAudio.visibility = View.GONE
                        imageContainer.visibility = View.GONE
                        videoPreview = MediaItem.fromUri(post.attachment.url)
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

            if (post.authorAvatar == null) {
                avatar.setImageResource(R.drawable.ic_baseline_person_pin_24)
            } else {
                MediaUtils.loadPostAvatar(avatar, url, post)
            }

            likes.setOnClickListener {
                if (!post.likedByMe) {
                    onInteractionListener.onLike(post)
                    ObjectAnimator.ofPropertyValuesHolder(
                        likes,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0F, 1.2F, 1.0F, 1.2F),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0F, 1.2F, 1.0F, 1.2F)
                    ).start()
                } else {
                    onInteractionListener.onDisLike(post)
                    ObjectAnimator.ofPropertyValuesHolder(
                        likes,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0F, 1.2F, 1.0F, 1.2F),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0F, 1.2F, 1.0F, 1.2F)
                    ).start()
                }

            }

            playAudio.setOnClickListener {
                onInteractionListener.onPlayAudio(post)
            }

            imageContainer.setOnClickListener {
                onInteractionListener.onFullScreenImage(post)
            }

            contentPost.setOnClickListener {
                onInteractionListener.onSinglePost(post)
            }

            menuButton.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

            menuButton.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.delete -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}