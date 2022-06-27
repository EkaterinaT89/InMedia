package ru.netology.inmedia.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.enumeration.AttachmentType
import ru.netology.inmedia.util.MediaUtils
import ru.netology.inmedia.util.PermissionsManager
import java.io.File

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private var bindingPost: FragmentNewPostBinding? = null

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private var mediaPlayer: ExoPlayer? = null

    private val permissionsRequestCode = 963
    lateinit var permissionManager: PermissionsManager

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

        val permissions = listOf<String>(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )

        permissionManager =
            PermissionsManager(requireActivity(), permissions, permissionsRequestCode)

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
        mediaPlayer = ExoPlayer.Builder(requireContext())
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