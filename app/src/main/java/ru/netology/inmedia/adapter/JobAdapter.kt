package ru.netology.inmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.inmedia.databinding.FragmentCardUserOccupationBinding
import ru.netology.inmedia.dto.Job

interface JobListener {
    fun onSingleJob(job: Job)
}

class JobAdapter(private val jobListener: JobListener) :
    ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding =
            FragmentCardUserOccupationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, jobListener)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }
}

class JobViewHolder(
    private val binding: FragmentCardUserOccupationBinding,
    private val jobListener: JobListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(job: Job) {
        with(binding) {
            position.text = job.position
            company.text = job.name

            getJobDetails.setOnClickListener {
                jobListener.onSingleJob(job)
            }

        }

    }
}

class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}