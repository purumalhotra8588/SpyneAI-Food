package com.spyneai.draft.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.base.OnItemClickListener
import com.spyneai.orders.ui.adapter.CompletedSkuPagedHolder
import com.spyneai.orders.ui.adapter.OngoingSkuPagedHolder
import com.spyneai.shootapp.repository.model.sku.Sku


class SkuPagedAdapter(
    val context: Context,
    val status: String,
    var listener: OnItemClickListener
) : PagingDataAdapter<Sku, RecyclerView.ViewHolder>(REPO_COMPARATOR) {

    object REPO_COMPARATOR : DiffUtil.ItemCallback<Sku>() {
        override fun areItemsTheSame(
            oldItem: Sku,
            newItem: Sku
        ) =
            oldItem.projectUuid == newItem.projectUuid

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: Sku,
            newItem: Sku
        ) =
            oldItem == newItem
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        getItem(position)?.let {
            when (status) {
                "draft" -> (holder as DraftSkuPagedHolder).bind(it)
                "ongoing" -> (holder as OngoingSkuPagedHolder).bind(it)
                "completed" -> (holder as CompletedSkuPagedHolder).bind(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (status) {
            "draft" -> DraftSkuPagedHolder.getInstance(context, parent,listener)
            "ongoing" -> OngoingSkuPagedHolder.getInstance(context, parent)
            else -> CompletedSkuPagedHolder.getInstance(context, parent)
        }
    }
}