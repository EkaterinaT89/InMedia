package ru.netology.inmedia.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import ru.netology.inmedia.R
import ru.netology.inmedia.service.AndroidUtils
import ru.netology.inmedia.service.StringArg
import androidx.navigation.fragment.findNavController
import ru.netology.inmedia.databinding.FragmentNewEventBinding
import ru.netology.inmedia.enumeration.AttachmentType
import ru.netology.inmedia.enumeration.EventType
import ru.netology.inmedia.viewmodel.EventViewModel

class NewEventFragment : Fragment() {

    private val viewModel: EventViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private var bindingEvent: FragmentNewEventBinding? = null

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    var eventType: EventType? = null

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentNewEventBinding.inflate(
            inflater,
            container,
            false
        )

        bindingEvent = binding

        setHasOptionsMenu(true)

        arguments?.textArg
            ?.let(binding.edit::setText)

        binding.edit.requestFocus()

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
                        viewModel.changeAttachment(uri, uri?.toFile(), AttachmentType.IMAGE)
                    }
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
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

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }

        viewModel.attachment.observe(viewLifecycleOwner) { model ->
            if (model.uri == null) {
                binding.photoContainer.visibility = View.GONE

            }
            when (model.type) {
                AttachmentType.IMAGE -> {
                    binding.photoContainer.visibility = View.VISIBLE
                    binding.photo.setImageURI(model.uri)
                }
                else -> binding.photoContainer.visibility = View.GONE
            }
        }

        with(binding) {
            onlineNotSelected.setOnClickListener {
                onlineNotSelected.visibility = View.VISIBLE
                onlineNotSelected.visibility = View.GONE
                eventType = EventType.ONLINE
            }

            onlineSelected.setOnClickListener {
                onlineNotSelected.visibility = View.GONE
                onlineNotSelected.visibility = View.VISIBLE
                eventType = EventType.ONLINE
            }

            offlineNotSelected.setOnClickListener {
                offlineSelected.visibility = View.VISIBLE
                offlineNotSelected.visibility = View.GONE
                eventType = EventType.OFFLINE
            }

            offlineSelected.setOnClickListener {
                offlineSelected.visibility = View.GONE
                offlineNotSelected.visibility = View.VISIBLE
                eventType = EventType.ONLINE
            }

            removeAttachment.setOnClickListener {
                viewModel.changeAttachment(null, null, null)
            }

        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.new_post_menu, menu)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                bindingEvent?.let {
                    viewModel.editEventContent(it.edit.text.toString())
                    if(eventType == null) {
                        viewModel.createNewEvent(eventType = EventType.OFFLINE)
                    } else {
                        viewModel.createNewEvent(eventType!!)
                    }
                    AndroidUtils.hideKeyboard(requireView())
                    findNavController().navigate(R.id.tabsFragment)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}