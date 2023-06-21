package com.spyneai.registration.view.ui.activity

import android.media.Image
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.PreferenceListInviteBinding


class PreferenceListHolder(
        itemView: View,
        listener: OnItemClickListener?)
    : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<Image>{

        var listener: OnItemClickListener? = null
        var binding : PreferenceListInviteBinding? = null
        val TAG = "PreferenceListHolder"

        init {
            binding = PreferenceListInviteBinding.bind(itemView)
            this.listener = listener
        }

    override fun bind(data: Image) {


    }
}