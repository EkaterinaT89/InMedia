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
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import ru.netology.inmedia.R
import ru.netology.inmedia.databinding.FragmentCardEventBinding
import ru.netology.inmedia.dto.Event
import ru.netology.inmedia.enumeration.EventType
import ru.netology.inmedia.service.PostService
import ru.netology.inmedia.util.MediaUtils

interface OnEventListener {
    fun onEdit(event: Event)
    fun onLike(event: Event)
    fun onDisLike(event: Event)
    fun onSingleEvent(event: Event)
    fun onRemove(event: Event)
    fun onFullImage(event: Event)
    fun onLink(url: String)
}

class EventAdapter(private val onEventListener: OnEventListener) :
    ListAdapter<Event, EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding =
            FragmentCardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onEventListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
    }
}

class EventViewHolder(
    private val binding: FragmentCardEventBinding,
    private val onEventListener: OnEventListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: Event) {

        val url = "https://inmediadiploma.herokuapp.com"

        binding.apply {
            eventTime.text = event.published
            eventDate.text = event.datetime
            eventAuthor.text = event.author
            eventContent.text = event.content
            Linkify.addLinks(eventContent, Linkify.ALL)
            eventContent.movementMethod = BetterLinkMovementMethod.getInstance()
            BetterLinkMovementMethod.linkify(Linkify.WEB_URLS, eventContent)
                .setOnLinkClickListener { _, url ->
                    onEventListener.onLink(url)
                    true
                }

            likesButton.text = PostService.countPresents(event.likeOwnerIds)

            likesButton.isChecked = event.likedByMe

            if (event.attachment == null) {
                imageContainer.visibility = View.GONE
            } else {
                imageContainer.visibility = View.VISIBLE
                MediaUtils.loadEventImage(imageContainer, url, event)
            }

            if (event.type == EventType.ONLINE) {
                eventType.text = itemView.context.getString(R.string.online)
            } else {
                eventType.text = itemView.context.getString(R.string.offline)
            }

            imageContainer.setOnClickListener {
                onEventListener.onFullImage(event)
            }

            likesButton.setOnClickListener {
                if (!event.likedByMe) {
                    onEventListener.onLike(event)
                    ObjectAnimator.ofPropertyValuesHolder(
                        likesButton,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0F, 1.2F, 1.0F, 1.2F),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0F, 1.2F, 1.0F, 1.2F)
                    ).start()
                } else {
                    onEventListener.onDisLike(event)
                    ObjectAnimator.ofPropertyValuesHolder(
                        likesButton,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0F, 1.2F, 1.0F, 1.2F),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0F, 1.2F, 1.0F, 1.2F)
                    ).start()
                }

            }

            eventContent.setOnClickListener {
                onEventListener.onSingleEvent(event)
            }

            takeAPartButton.setOnClickListener {
                takeAPartButton.visibility = View.GONE
                denyButton.visibility = View.VISIBLE
            }

            denyButton.setOnClickListener {
                takeAPartButton.visibility = View.VISIBLE
                denyButton.visibility = View.GONE
            }

            menuButton.visibility = if (event.ownedByMe) View.VISIBLE else View.INVISIBLE

            menuButton.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.delete -> {
                                onEventListener.onRemove(event)
                                true
                            }
                            R.id.edit -> {
                                onEventListener.onEdit(event)
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

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}