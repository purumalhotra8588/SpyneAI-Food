package com.spyneai.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.spyneai.R
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shootapp.repository.model.image.Image
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso


public class ShowReplacedImagesAdapter(
    val context: Context,
    val imagesList: ArrayList<Image>,
    val btnlistener: BtnClickListener
) : RecyclerView.Adapter<ShowReplacedImagesAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgBeforeReplaced: ImageView = view.findViewById(com.spyneai.R.id.imgBeforeReplaced)
        val imgAfterReplaced: ImageView = view.findViewById(com.spyneai.R.id.imgAfterReplaced)
        val ivQcStatus: ImageView = view.findViewById(com.spyneai.R.id.ivQcStatus)
        val llBeforeAfterReplaced: LinearLayout = view.findViewById(com.spyneai.R.id.llBeforeAfterReplaced)
        val llQcStatus: LinearLayout = view.findViewById(com.spyneai.R.id.llQcStatus)
        val llQcMessage: LinearLayout = view.findViewById(com.spyneai.R.id.llQcMessage)
        val ivArrow: ImageView = view.findViewById(com.spyneai.R.id.ivArrow)
        val tvQcStatus: TextView = view.findViewById(com.spyneai.R.id.tvQcStatus)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(com.spyneai.R.layout.row_app_replaced_images, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        if(Utilities.getBool(context, AppConstants.CHECK_QC,false)) {
            imagesList[position].qcStatus.let {
                viewHolder.llQcStatus.visibility = View.VISIBLE
                when (it) {
                    "approved"->{
                        viewHolder.ivQcStatus.setBackgroundResource(R.drawable.qcapproved)
                        viewHolder.tvQcStatus.text="Assured"
                    }
                    "in_progress"->{
                        viewHolder.ivQcStatus.setBackgroundResource(R.drawable.qcinprogress)
                        viewHolder.tvQcStatus.text="Under review"
                    }
                    "rejected"->{
                        viewHolder.ivQcStatus.setBackgroundResource(R.drawable.qcreshoot)
                        viewHolder.tvQcStatus.text="Reshoot required"
                    }
                    "qc_done"->{
                        viewHolder.ivQcStatus.setBackgroundResource(R.drawable.qcapproved)
                        viewHolder.tvQcStatus.text="Assured"
                    }
                    "reshoot"->{
                        viewHolder.ivQcStatus.setBackgroundResource(R.drawable.qcreshoot)
                        viewHolder.tvQcStatus.text="Reshoot required"
                    }
                    else->{
                        viewHolder.ivQcStatus.setBackgroundResource(R.drawable.qcinprogress)
                        viewHolder.tvQcStatus.text="Under review"
                    }
                }
            }
        }else{
            viewHolder.llQcStatus?.visibility = View.GONE
        }


        if (!imagesList[position].input_image_lres_url.isNullOrEmpty()){
            val picasso = Picasso.get()
            picasso.invalidate(imagesList[position].input_image_lres_url)
            picasso
                .load(imagesList[position].input_image_lres_url)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .into(viewHolder.imgBeforeReplaced)
        }

        if (!imagesList[position].output_image_lres_url.isNullOrEmpty()){
            val picasso = Picasso.get()

            picasso.invalidate(imagesList[position].output_image_lres_url)
            picasso
                .load(imagesList[position].output_image_lres_url)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .into(viewHolder.imgAfterReplaced)
        }

        mClickListener = btnlistener

        viewHolder.llBeforeAfterReplaced.setOnClickListener(View.OnClickListener {
            Log.e("ok", "Ok way$position")
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })


        viewHolder.llQcStatus.setOnClickListener{
            if(viewHolder.llQcMessage.visibility ==View.GONE){
                viewHolder.ivArrow.visibility =View.VISIBLE
                viewHolder.llQcMessage.visibility =View.VISIBLE
            }else{
                viewHolder.ivArrow.visibility =View.GONE
                viewHolder.llQcMessage.visibility =View.GONE
            }


        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = imagesList.size

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }
}
