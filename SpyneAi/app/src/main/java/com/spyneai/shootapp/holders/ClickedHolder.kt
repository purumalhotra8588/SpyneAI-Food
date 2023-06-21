package com.spyneai.shootapp.holders

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.app.BaseApplication
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.ItemClickedBinding
import com.spyneai.loadSmartly
import com.spyneai.shootapp.data.OnOverlaySelectionListener
import com.spyneai.shootapp.data.model.ShootData

class ClickedHolder(
    itemView: View,
    listener: OnItemClickListener?,
    overlaySelectionListener: OnOverlaySelectionListener?
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<ShootData> {

    var listener: OnItemClickListener? = null
    var overlaySelectionListener: OnOverlaySelectionListener? = null
    var binding: ItemClickedBinding? = null

    val TAG = "OverlaysHolder"

    init {
        binding = ItemClickedBinding.bind(itemView)
        this.listener = listener
        this.overlaySelectionListener = overlaySelectionListener
    }

    override fun bind(data: ShootData) {
        //set overlay id as per adapter position
        data.overlayId = adapterPosition

        binding?.tvAngle?.text = data.angle.toString() + BaseApplication.getContext().getString(R.string.angle_sign)

        when {
            data.isSelected -> {
                binding?.flOverlay?.background = ContextCompat.getDrawable(
                    BaseApplication.getContext(),
                    R.drawable.bg_overlay_selected
                )
                overlaySelectionListener?.onOverlaySelected(
                    binding?.flOverlay!!,
                    adapterPosition,
                    data
                )
            }

            data.imageClicked -> {
                binding?.flOverlay?.background = ContextCompat.getDrawable(
                    BaseApplication.getContext(),
                    R.drawable.bg_overlay_image_clicked
                )
            }

            else -> {
                binding?.flOverlay?.background = ContextCompat.getDrawable(
                    BaseApplication.getContext(),
                    R.drawable.bg_overlay
                )
            }
        }

        if (data.imageClicked) {
            itemView.loadSmartly(data.capturedImage,binding?.ivClicked!!)
        }

        binding?.flOverlay?.setOnClickListener {
            listener?.onItemClick(
                it,
                adapterPosition,
                data
            )
        }
    }
}