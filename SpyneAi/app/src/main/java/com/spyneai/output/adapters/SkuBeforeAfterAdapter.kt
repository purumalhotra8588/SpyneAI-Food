package com.spyneai.output.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.spyneai.loadSmartlyThumbnail
import com.spyneai.loadSmartlyWithCache
import com.spyneai.needs.AppConstants
import com.spyneai.shootapp.repository.model.image.Image


class SkuBeforeAfterAdapter(
    val context: Context,
    val imageList: ArrayList<Image>,
    val btnlistener: BtnClickListener,
) : RecyclerView.Adapter<SkuBeforeAfterAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivBefore: ImageView = view.findViewById(com.spyneai.R.id.ivBefore)
        val ivBlurThumbnail: ImageView = view.findViewById(com.spyneai.R.id.ivBlurThumbnail)
        val ivCategoryThumbnail: ImageView = view.findViewById(com.spyneai.R.id.ivCategoryThumbnail)
        val ivAfter: ImageView = view.findViewById(com.spyneai.R.id.ivAfter)
        val clMain: ConstraintLayout = view.findViewById(com.spyneai.R.id.clMain)
        val lottieCircularAnimation: LottieAnimationView =
            view.findViewById(com.spyneai.R.id.lottieCircularAnimation)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        if (com.spyneai.needs.Utilities.getPreference(
                context,
                AppConstants.SELECTED_CATEGORY_ID
            ) == AppConstants.CARS_CATEGORY_ID
        ){
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(com.spyneai.R.layout.item_output_before_after, viewGroup, false)
            return ViewHolder(view)
        }else{
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(com.spyneai.R.layout.item_output_before_after_portrait, viewGroup, false)
            return ViewHolder(view)
        }

    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
//        context.loadSmartly(rawImageList[position], viewHolder.ivBefore)
        context.loadSmartlyThumbnail(imageList[position].path, viewHolder.ivBlurThumbnail)

        context.loadSmartlyWithCache(imageList[position].path, viewHolder.ivBefore)


        if (imageList[position].output_image_lres_url.isNullOrEmpty()) {

            viewHolder.ivAfter.visibility = View.GONE
            viewHolder.lottieCircularAnimation.visibility = View.VISIBLE
            viewHolder.ivBlurThumbnail.visibility = View.VISIBLE
        } else {
            viewHolder.ivAfter.visibility = View.VISIBLE
            viewHolder.lottieCircularAnimation.visibility = View.GONE
            viewHolder.ivBlurThumbnail.visibility = View.GONE
            Glide.with(context)
                .load(imageList[position].output_image_lres_url)
                .apply(
                    RequestOptions()
                        .error(com.spyneai.R.mipmap.defaults)
                )
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(viewHolder.ivAfter)
        }


        mClickListener = btnlistener

        viewHolder.clMain.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = imageList.size

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }
}
