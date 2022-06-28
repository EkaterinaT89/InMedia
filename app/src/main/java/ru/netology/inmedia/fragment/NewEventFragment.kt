package ru.netology.inmedia.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import java.util.*
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
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.databinding.FragmentNewEventBinding
import ru.netology.inmedia.enumeration.AttachmentType
import ru.netology.inmedia.util.DateFormatter
import ru.netology.inmedia.viewmodel.EventViewModel

@AndroidEntryPoint
class NewEventFragment : Fragment() {

    private val viewModel: EventViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private var bindingEvent: FragmentNewEventBinding? = null

    companion object {
        var Bundle.textArg: String? by StringArg
    }

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

        viewModel.eventDateTime.observe(viewLifecycleOwner) { dateTime ->
            dateTime?.let {
                binding.dateTimePick.text = it
            }
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
            removeAttachment.setOnClickListener {
                viewModel.changeAttachment(null, null, null)
            }

            groupPickEventDate.setOnClickListener {
                showDateTimePicker()
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

                    when (it.typeOptions.checkedRadioButtonId) {
                        R.id.option_online -> viewModel.createNewEventOnLine()
                        R.id.option_offline -> viewModel.createNewEventOffLine()
                        else -> viewModel.createNewEventOnLine()
                    }

                    viewModel.createNewEventOnLine()

                    AndroidUtils.hideKeyboard(requireView())
                    findNavController().navigate(R.id.tabsFragment)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        DatePickerFragment(calendar) { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            TimePickerFragment(calendar) { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                viewModel.setEventDateTime(
                    DateFormatter.formatDateToDateTimeString(calendar.time)
                )
            }.show(childFragmentManager, "timePicker")
        }.show(childFragmentManager, "datePicker")
    }

}