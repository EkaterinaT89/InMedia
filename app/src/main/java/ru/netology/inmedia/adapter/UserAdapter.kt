package ru.netology.inmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.inmedia.R
import ru.netology.inmedia.databinding.FragmentCardUserBinding
import ru.netology.inmedia.dto.User
import ru.netology.inmedia.util.MediaUtils

interface OnUserListener {
    fun onSingleUser(user: User)
}

class UserAdapter(private val onUserListener: OnUserListener) :
    ListAdapter<User, UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            FragmentCardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onUserListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }
}

class UserViewHolder(
    private val binding: FragmentCardUserBinding,
    private val onUserListener: OnUserListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
        with(binding) {
            userName.text = user.name

            val url = "https://inmediadiploma.herokuapp.com/api"

            if (user.avatar == null) {
                avatarInput.setImageResource(R.drawable.ic_baseline_person_pin_24)
            } else {
                MediaUtils.loadUserAvatar(avatarInput, url, user)
            }

            cardUser.setOnClickListener {
                onUserListener.onSingleUser(user)
            }
        }

    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}
