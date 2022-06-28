package ru.netology.inmedia.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.R
import ru.netology.inmedia.databinding.FragmentEditPostBinding
import ru.netology.inmedia.databinding.FragmentNewPostBinding
import ru.netology.inmedia.enumeration.AttachmentType
import ru.netology.inmedia.service.AndroidUtils
import ru.netology.inmedia.service.StringArg
import ru.netology.inmedia.util.MediaUtils
import ru.netology.inmedia.util.PermissionsManager
import ru.netology.inmedia.viewmodel.PostViewModel
import java.io.File

@AndroidEntryPoint
class EditPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private var mediaPlayer: SimpleExoPlayer? = null

    private val permissionsRequestCode = 963
    lateinit var permissionManager: PermissionsManager

    private var bindingPost: FragmentEditPostBinding? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentEditPostBinding.inflate(
            inflater,
            container,
            false
        )

        bindingPost = binding

        arguments?.textArg.let(binding.editContent::setText)

        val permissions = listOf<String>(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )

        permissionManager =
            PermissionsManager(requireActivity(), permissions, permissionsRequestCode)

        viewModel.edited.observe(viewLifecycleOwner) { post ->
            post?.let {
                binding.editContent.requestFocus()
                it.attachment?.let { attachment ->
                    val type = attachment.type
                    val attachmentUrl = attachment.url
                    viewModel.changeAttachment(attachmentUrl.toUri(), null, type)

                    if (type == AttachmentType.IMAGE) {
                        val url = "https://inmediadiploma.herokuapp.com/api/"

                        with(binding) {
                            photoContainer.visibility = View.VISIBLE
                            MediaUtils.loadPostImage(photo, url, post)
                        }
                    }
                }
            }
        }

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

        val pickVideoResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val data = it.data
                when (it.resultCode) {
                    Activity.RESULT_OK -> {
                        val videoUri = data?.data!!
                        val videoPath = MediaUtils.getRealPathFromUri(videoUri, requireActivity())

                        if (videoPath != null) {
                            viewModel.changeAttachment(
                                videoUri,
                                File(videoPath),
                                AttachmentType.VIDEO
                            )
                        }
                    }
                }
            }

        val pickAudioResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val data = it.data
                when (it.resultCode) {
                    Activity.RESULT_OK -> {
                        val audioUri = data?.data!!
                        val audioPath = MediaUtils.getRealPathFromUri(audioUri, requireActivity())

                        if (audioPath != null) {
                            viewModel.changeAttachment(
                                audioUri,
                                File(audioPath),
                                AttachmentType.AUDIO
                            )
                        }
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

        with(binding) {
            saveButton.setOnClickListener {
                viewModel.editContent(editContent.text.toString())
                viewModel.save()
                AndroidUtils.hideKeyboard(requireView())
                findNavController().navigate(R.id.tabsFragment)
            }

            cancelEditButton.setOnClickListener {
                with(binding.editContent) {
                    AndroidUtils.hideKeyboard(this)
                    binding.group.visibility = android.view.View.GONE
                    findNavController().navigate(ru.netology.inmedia.R.id.tabsFragment)
                }
            }

            removeAttachment.setOnClickListener {
                viewModel.changeAttachment(null, null, null)
                removeAttachment.visibility = View.GONE
            }

            downloadMp3.setOnClickListener {
                audioContainer.visibility = View.VISIBLE
                removeAttachment.visibility = View.VISIBLE

                if (!permissionManager.checkPermissions()) {
                    permissionManager.requestPermissions()
                    Snackbar.make(
                        binding.root,
                        getString(R.string.grant_storage_permissions_dialog_message),
                        Snackbar.LENGTH_LONG
                    )
                        .setAnchorView(binding.audio)
                        .setAction(getString(R.string.everything_fine), {})
                        .show()
                    return@setOnClickListener
                }
                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                )
                pickAudioResult.launch(intent)

            }

            downloadMp4.setOnClickListener {
                binding.videoContainer.visibility = View.VISIBLE
                binding.removeAttachment.visibility = View.VISIBLE

                if (!permissionManager.checkPermissions()) {
                    permissionManager.requestPermissions()
                    Snackbar.make(
                        binding.root,
                        getString(R.string.grant_storage_permissions_dialog_message),
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(getString(R.string.everything_fine), {})
                        .show()
                    return@setOnClickListener
                }
                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                )
                pickVideoResult.launch(intent)
            }

            viewModel.attachment.observe(viewLifecycleOwner) { model ->
                if (model.uri == null) {
                    photoContainer.visibility = View.GONE
                    videoContainer.visibility = View.GONE
                    audioContainer.visibility = View.GONE
                }
                when (model.type) {
                    AttachmentType.IMAGE -> {
                        photoContainer.visibility = View.VISIBLE
                        photo.setImageURI(model.uri)
                    }
                    AttachmentType.AUDIO -> {
                        audioContainer.visibility = View.VISIBLE

                        val mediaItem = model.uri?.let { MediaItem.fromUri(it) }
                        if (mediaItem != null) {
                            mediaPlayer?.setMediaItem(mediaItem)
                        }
                    }
                    AttachmentType.VIDEO -> {
                        videoContainer.visibility = View.VISIBLE

                        val mediaItem = model.uri?.let { MediaItem.fromUri(it) }
                        if (mediaItem != null) {
                            mediaPlayer?.setMediaItem(mediaItem)
                        }
                    }
                }

            }

        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if (com.google.android.exoplayer2.util.Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (com.google.android.exoplayer2.util.Util.SDK_INT < 24 || mediaPlayer == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (com.google.android.exoplayer2.util.Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (com.google.android.exoplayer2.util.Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        mediaPlayer = SimpleExoPlayer.Builder(requireContext())
            .build()
            .also {
                bindingPost?.videoPlayerView?.player = it
            }
    }

    private fun releasePlayer() {
        mediaPlayer?.run {
            release()
        }
        mediaPlayer = null
    }

}