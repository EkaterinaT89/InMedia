package ru.netology.inmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.R
import ru.netology.inmedia.databinding.FragmentCardEventBinding
import ru.netology.inmedia.dto.Event
import ru.netology.inmedia.enumeration.EventType
import ru.netology.inmedia.fragment.NewPostFragment.Companion.textArg
import ru.netology.inmedia.service.EventArg
import ru.netology.inmedia.service.PostService
import ru.netology.inmedia.viewmodel.EventViewModel

@AndroidEntryPoint
class CardEventFragment : Fragment() {

    companion object {
        var Bundle.showEvent: Event? by EventArg
    }

    private val eventViewModel: EventViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentCardEventBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.showEvent?.let { event ->
            with(binding) {
                val url = "https://inmediadiploma.herokuapp.com"

                eventTime.text = event.published
                eventAuthor.text = event.author
                eventContent.text = event.content
                likesButton.text = PostService.countPresents(event.likeOwnerIds)
                eventDate.text = event.datetime
                likesButton.isChecked = event.likedByMe

                if (event.attachment == null) {
                    imageContainer.visibility = View.GONE
                } else {
                    imageContainer.visibility = View.VISIBLE
                }

                if (event.type == EventType.ONLINE) {
                    eventType.text = getString(R.string.online)
                } else {
                    eventType.text = getString(R.string.offline)
                }

                Glide.with(imageContainer)
                    .load("$url/media/${event.attachment?.url}")
                    .error(R.drawable.ic_error)
                    .placeholder(R.drawable.ic_loading)
                    .timeout(10_000)
                    .into(imageContainer)

                likesButton.setOnClickListener {
                    if (!event.likedByMe) {
                        eventViewModel.likeById(event.id)
                    } else {
                        eventViewModel.disLikeById(event.id)
                    }
                }

                takeAPartButton.setOnClickListener {
                    eventViewModel.takePartEvent(event.id)
                    takeAPartButton.visibility = View.GONE
                    denyButton.visibility = View.VISIBLE
                }

                denyButton.setOnClickListener {
                    eventViewModel.unTakePartEvent(event.id)
                    takeAPartButton.visibility = View.VISIBLE
                    denyButton.visibility = View.GONE
                }

                menuButton.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.menu)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.delete -> {
                                    eventViewModel.removeEventById(event.id)
                                    true
                                }
                                R.id.edit -> {
                                    eventViewModel.editEvent(event)
                                    findNavController().navigate(
                                        R.id.editEventFragment,
                                        Bundle().apply {
                                            textArg = event.content
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