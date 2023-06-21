package com.spyneai.homev12.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.homev12.holder.HomeProjectsHolder
import com.spyneai.shootapp.repository.model.project.Project


class HomeProjectPagedAdapter(
    val context : Context
    ) :
    PagingDataAdapter<Project, RecyclerView.ViewHolder>(REPO_COMPARATOR) {

    object REPO_COMPARATOR : DiffUtil.ItemCallback<Project>() {
        override fun areItemsTheSame(
            oldItem: Project,
            newItem: Project
        ) =
            oldItem.projectId == newItem.projectId

        override fun areContentsTheSame(
            oldItem: Project,
            newItem: Project
        ) =
            oldItem == newItem
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        getItem(position)?.let {
            (holder as HomeProjectsHolder).bind(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return HomeProjectsHolder.getInstance(context, parent)
    }
}