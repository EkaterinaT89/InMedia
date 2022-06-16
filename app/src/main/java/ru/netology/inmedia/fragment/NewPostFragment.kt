package ru.netology.inmedia.fragment

import android.app.Activity
import android.content.Intent
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
import ru.netology.inmedia.databinding.FragmentNewPostBinding
import ru.netology.inmedia.service.AndroidUtils
import ru.netology.inmedia.service.StringArg
import ru.netology.inmedia.viewmodel.PostViewModel
import androidx.navigation.fragment.findNavController
import ru.netology.inmedia.enumeration.AttachmentType
import ru.netology.inmedia.repository.PostRepositoryImpl

class NewPostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private var bindingPost: FragmentNewPostBinding? = null

    companion object {
        var Bundle.textArg: String? by StringArg

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )

        bindingPost = binding

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
                        viewModel.changeAttachment(uri, uri?.toFile())
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

        binding.removeAttachment.setOnClickListener {
            viewModel.changeAttachment(null, null)
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }
            binding.photoContainer.visibility = View.VISIBLE
            binding.removeAttachment.visibility = View.VISIBLE
            binding.photo.setImageURI(it.uri)
        }

        binding.downloadMp3.setOnClickListener {
            binding.audioContainer.visibility = View.VISIBLE
            binding.removeAttachment.visibility = View.VISIBLE
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/*"
            }
        }

        binding.downloadMp4.setOnClickListener {
            binding.videoContainer.visibility = View.VISIBLE
            binding.removeAttachment.visibility = View.VISIBLE
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "video/*"
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
                bindingPost?.let {
                    viewModel.editContent(it.edit.text.toString())
                    viewModel.save()
                    AndroidUtils.hideKeyboard(requireView())
                    findNavController().navigate(R.id.tabsFragment)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}