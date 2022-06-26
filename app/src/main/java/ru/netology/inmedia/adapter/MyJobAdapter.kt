package ru.netology.inmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.inmedia.R
import ru.netology.inmedia.databinding.FragmentCardMyOccupationBinding
import ru.netology.inmedia.databinding.FragmentCardUserOccupationBinding
import ru.netology.inmedia.dto.Job

interface MyJobListener {
    fun onSingleJob(job: Job)
    fun onEdit(job: Job)
    fun onDelete(job: Job)
}

class MyJobAdapter(private val jobListener: MyJobListener) :
    ListAdapter<Job, MyJobViewHolder>(MyJobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyJobViewHolder {
        val binding =
            FragmentCardMyOccupationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyJobViewHolder(binding, jobListener)
    }

    override fun onBindViewHolder(holder: MyJobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }
}

class MyJobViewHolder(
    private val binding: FragmentCardMyOccupationBinding,
    private val jobListener: MyJobListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(job: Job) {
        with(binding) {
            position.text = job.position
            company.text = job.name

            getJobDetails.setOnClickListener {
                jobListener.onSingleJob(job)
            }

            menuButton.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.delete -> {
                                jobListener.onDelete(job)
                                true
                            }
                            R.id.edit -> {
                                jobListener.onEdit(job)
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

class MyJobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}