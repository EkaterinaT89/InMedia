package ru.netology.inmedia.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.inmedia.BuildConfig
import ru.netology.inmedia.R
import ru.netology.inmedia.databinding.FragmentCardPostBinding
import ru.netology.inmedia.dto.Post
import ru.netology.inmedia.enumeration.AttachmentType
import ru.netology.inmedia.service.PostService

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onDisLike(post: Post)
    fun onSinglePost(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)

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

    fun bind(post: Post) {
        val url = "https://inmediadiploma.herokuapp.com"

        binding.apply {
            authorName.text = post.author
            date.text = post.published.toString()
            contentPost.text = post.content
            likes.text = PostService.countPresents(post.likeOwnerIds.size.toLong())

            likes.isChecked = post.likedByMe

            if(post.attachment == null) {
                groupForVideo.visibility = View.GONE
                playAudio.visibility = View.GONE
                imageContainer.visibility = View.GONE
            } else {
                when(post.attachment.type) {
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
                if(post.likedByMe) {
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

//            playVideo.setOnClickListener {
//                onInteractionListener.onPlayVideo(post)
//            }
//
//            playAudio.setOnClickListener {
//                onInteractionListener.onPlayAudio(post)
//            }
//
//            imageContainer.setOnClickListener{
//                onInteractionListener.onFullImage(post)
//            }

            contentPost.setOnClickListener {
                onInteractionListener.onSinglePost(post)
            }

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