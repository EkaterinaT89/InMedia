package ru.netology.inmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.inmedia.R
import ru.netology.inmedia.adapter.EventAdapter
import ru.netology.inmedia.adapter.OnEventListener
import ru.netology.inmedia.databinding.FragmentListEventsBinding
import ru.netology.inmedia.dto.Event
import ru.netology.inmedia.fragment.CardEventFragment.Companion.showEvent
import ru.netology.inmedia.fragment.NewPostFragment.Companion.textArg
import ru.netology.inmedia.viewmodel.AuthViewModel
import ru.netology.inmedia.viewmodel.EventViewModel

class ListEventsFragment : Fragment() {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentListEventsBinding.inflate(
            inflater,
            container,
            false
        )

        val eventViewModel: EventViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val authViewModel: AuthViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val eventAdapter = EventAdapter(object : OnEventListener {
            override fun onEdit(event: Event) {
                eventViewModel.editEvent(event)
                binding.addNewEvent.visibility = View.GONE
                findNavController().navigate(
                    R.id.editEventFragment,
                    Bundle().apply {
                        textArg = event.content
                    })
            }

            override fun onLike(event: Event) {
                eventViewModel.likeById(event.id)
            }

            override fun onDisLike(event: Event) {
                eventViewModel.disLikeById(event.id)
            }

            override fun onTakePart(event: Event) {
                eventViewModel.takePartEvent(event.id)
            }

            override fun onUnTakePart(event: Event) {
                eventViewModel.unTakePartEvent(event.id)
            }

            override fun onSingleEvent(event: Event) {
                findNavController().navigate(R.id.cardEventFragment,
                    Bundle().apply
                    {
                        showEvent = event
                    })
            }

            override fun onRemove(event: Event) {
                eventViewModel.removeEventById(event.id)
            }

            override fun onFullImage(event: Event) {
                findNavController().navigate(R.id.imageFragment)
            }

        })

        binding.eventsContainer.adapter = eventAdapter

        eventViewModel.data.observe(viewLifecycleOwner, { state ->
            eventAdapter.submitList(state.events)
        })

        eventViewModel.dataState.observe(viewLifecycleOwner, { state ->
            with(binding) {
                progress.isVisible = state.loading
                swiperefresh.isRefreshing = state.refreshing
                if (state.error) {
                    error.visibility = View.VISIBLE
                    tryAgainButton.setOnClickListener {
                        eventViewModel.tryAgain()
                        error.visibility = View.GONE
                    }
                }
            }
        })

        with(binding) {
            addNewEvent.setOnClickListener {
                if (authViewModel.authenticated) {
                    findNavController().navigate(R.id.newEventFragment)
                } else {
                    findNavController().navigate(R.id.authFragment)
                }
            }
        }

        return binding.root
    }

}