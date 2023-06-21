package com.spyneai.orders.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R
import com.spyneai.databinding.ItemGridSkuV2Binding
import com.spyneai.databinding.ItemLinearSkuV2Binding
import com.spyneai.getFormattedDate
import com.spyneai.loadSmartlyWithCache
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.ui.activity.ShowRawImagesActivity
import com.spyneai.shootapp.repository.model.sku.Sku


class OngoingSkuPagedHolder(
    val context: Context,
    val view: View
) : RecyclerView.ViewHolder(view) {

    var bindingGrid: ItemGridSkuV2Binding? = null
    var bindingLinear: ItemLinearSkuV2Binding? = null

    init {
        if (!Utilities.getBool(context, "viewTypeGrid", false)) {
            bindingLinear = ItemLinearSkuV2Binding.bind(itemView)
        } else {
            bindingGrid = ItemGridSkuV2Binding.bind(itemView)
        }
    }

    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(
            context: Context,
            parent: ViewGroup
        ): OngoingSkuPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = if (!Utilities.getBool(context, "viewTypeGrid", false)) {
                inflater.inflate(R.layout.item_linear_sku_v2, parent, false)
            } else {
                inflater.inflate(R.layout.item_grid_sku_v2, parent, false)
            }
            return OngoingSkuPagedHolder(context, view)
        }
    }

    fun bind(item: Sku?) {
        item?.let { showData(it) }
    }

    private fun showData(item: Sku) {
        if (!Utilities.getBool(context, "viewTypeGrid", false)) {
            bindingLinear?.let {
                it.ivSkuStatus.setImageResource(R.drawable.ongoing_prj)
                it.tvStatus.text =
                    "Processing" + " (" + item.processedImages + " of " + item.imagesCount + ")"
                it.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.ongoing_text_colour
                    )
                )

                it.tvImages.text = "Images: ${item.imagesCount}"
                it.ivDownload.visibility = View.GONE
                it.tvCategory.text =
                    if (item.subcategoryName.isNullOrEmpty()) item.categoryName else item.subcategoryName
                if (item.isThreeSixty) {
                    if (item.subcategoryName.isNullOrEmpty() && item.categoryName.isNullOrEmpty()) {
                        it.tvCategory.text = "Automobile"
                    }
                    if (item.imagesCount == 0)
                        it.tvImages.text = "Images: ${item.totalFrames}"
                }


                it.tvDate.text = getFormattedDate(item.createdAt)

                if (item.thumbnail.isNullOrEmpty()) {
                    Glide.with(context)
                        .load(R.drawable.spyne_thumbnail)
                        .into(it.skuThumbnail)
                } else {
                    if (item.categoryId == AppConstants.CARS_CATEGORY_ID || item.categoryId == AppConstants.BIKES_CATEGORY_ID) {
                        Glide.with(context)
                            .load(item.thumbnail)
                            .thumbnail(Glide.with(context).load(R.drawable.placeholder_gif))
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .skipMemoryCache(false)
                            .into(it.skuThumbnail)
                    } else {
                        context.loadSmartlyWithCache(
                            item.thumbnail,
                            it.skuThumbnail
                        )
                    }
                }


//            it.tvPaid.isVisible = false
                it.tvSkuNameValue.text = item.skuName

                it.clSku.setOnClickListener {
                    if(item.categoryId != item.subcategoryId) {
                        Intent(context, ShowRawImagesActivity::class.java)
                            .apply {
                                putExtra(AppConstants.SKU_UUID, item.uuid)
                                putExtra(AppConstants.SKU_NAME, item.skuName)
                                putExtra(AppConstants.SKU_ID, item.skuId)
                                putExtra(AppConstants.PROJECT_ID, item.projectUuid)
                                putExtra(AppConstants.PROJECT_ID, item.projectId)
                                context.startActivity(this)
                            }
                    }
                }
            }
        } else {
            bindingGrid?.let {
                it.ivSkuStatus.setImageResource(R.drawable.ongoing_prj)
                it.tvStatus.text =
                    "Processing" + " (" + item.processedImages + " of " + item.imagesCount + ")"
                it.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.ongoing_text_colour
                    )
                )
                it.ivDownload.visibility = View.GONE
                it.tvCategory.text =
                    if (item.subcategoryName.isNullOrEmpty()) item.categoryName else item.subcategoryName
                it.tvImages.text = "Images: ${item.imagesCount}"
                it.ivDownload.visibility = View.GONE
                it.tvCategory.text =
                    if (item.subcategoryName.isNullOrEmpty()) item.categoryName else item.subcategoryName
                if (item.isThreeSixty) {
                    if (item.subcategoryName.isNullOrEmpty() && item.categoryName.isNullOrEmpty()) {
                        it.tvCategory.text = "Automobile"
                    }
                    it.tvImages.text = "Images: ${item.totalFrames}"

                }



                it.tvDate.text = getFormattedDate(item.createdAt)

                if (item.thumbnail.isNullOrEmpty()) {
                    Glide.with(context)
                        .load(R.drawable.spyne_thumbnail)
                        .into(it.skuThumbnail)
                } else {
                    if (item.categoryId == AppConstants.CARS_CATEGORY_ID || item.categoryId == AppConstants.BIKES_CATEGORY_ID) {
                        Glide.with(context)
                            .load(item.thumbnail)
                            .thumbnail(Glide.with(context).load(R.drawable.placeholder_gif))
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .skipMemoryCache(false)
                            .into(it.skuThumbnail)
                    } else {
                        context.loadSmartlyWithCache(
                            item.thumbnail,
                            it.skuThumbnail
                        )
                    }
                }


//            it.tvPaid.isVisible = false
                it.tvSkuNameValue.text = item.skuName

                it.clSku.setOnClickListener {
                    if (item.categoryId != item.subcategoryId) {
                        Intent(context, ShowRawImagesActivity::class.java)
                            .apply {
                                putExtra(AppConstants.SKU_UUID, item.uuid)
                                putExtra(AppConstants.SKU_NAME, item.skuName)
                                putExtra(AppConstants.SKU_ID, item.skuId)
                                putExtra(AppConstants.PROJECT_ID, item.projectUuid)
                                putExtra(AppConstants.PROJECT_ID, item.projectId)
                                context.startActivity(this)
                            }
                    }
                }
            }

        }
    }
}