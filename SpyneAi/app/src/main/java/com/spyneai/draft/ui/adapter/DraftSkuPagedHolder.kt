package com.spyneai.draft.ui.adapter

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
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.ItemGridSkuV2Binding
import com.spyneai.databinding.ItemLinearSkuV2Binding
import com.spyneai.getFormattedDate
import com.spyneai.loadSmartlyWithCache
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shootapp.repository.model.sku.Sku
import com.spyneai.shootapp.ui.base.ProjectDetailsActivity
import com.spyneai.shootapp.ui.base.ShootPortraitActivity

class DraftSkuPagedHolder(
    val context: Context,
    val view: View,
    var listener: OnItemClickListener
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
            parent: ViewGroup,
            listener: OnItemClickListener
        ): DraftSkuPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = if (!Utilities.getBool(context, "viewTypeGrid", false)) {
                inflater.inflate(R.layout.item_linear_sku_v2, parent, false)
            } else {
                inflater.inflate(R.layout.item_grid_sku_v2, parent, false)
            }
            return DraftSkuPagedHolder(context, view, listener)
        }
    }

    fun bind(item: Sku?) {
        item?.let { showData(it) }
    }

    private fun showData(item: Sku) {
        if (!Utilities.getBool(context, "viewTypeGrid", false)) {
            bindingLinear?.let {
                it.ivSkuStatus.setImageResource(R.drawable.draft_prj)
                it.tvStatus.text = if (item.status == "Draft" || item.status == "draft") "Draft" else item.status
                it.ivDownload.visibility = View.INVISIBLE
                it.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.draft_text_colour))
                it.tvCategory.text =
                    if (item.subcategoryName.isNullOrEmpty()) item.categoryName else item.subcategoryName
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
                it.tvImages.text = "Images: ${item.imagesCount}"

                it.clSku.setOnClickListener {
                    Utilities.savePrefrence(
                        context,
                        AppConstants.SKU_ID,
                        item.skuId
                    )

                    if (item.status == "draft" || item.status == "Draft"){
                        var intent: Intent? = null
                        if (item.imagesCount > 0) {
                            intent = Intent(
                                context,
                                ShootPortraitActivity::class.java
                            )
                        } else {
                            intent = Intent(
                                context,
                                ShootPortraitActivity::class.java
                            )
                        }

                        intent?.apply {
                            putExtra(AppConstants.FROM_LOCAL_DB, true)
                            putExtra(AppConstants.FROM_DRAFTS, true)
                            putExtra(AppConstants.PROJECT_ID, item.projectId)
                            putExtra(AppConstants.PROJECT_UUIID, item.projectUuid)
                            putExtra(AppConstants.CATEGORY_NAME, item.categoryName)
                            putExtra(AppConstants.CATEGORY_ID, item.categoryId)
                            putExtra(AppConstants.SUB_CAT_ID, item.subcategoryId)
                            putExtra(AppConstants.SUB_CAT_NAME, item.subcategoryName)
                            putExtra(AppConstants.SKU_NAME, item.skuName)
                            putExtra(AppConstants.PROJECT_NAME, item.skuName)
                            putExtra(AppConstants.IMAGE_COUNT, item.imagesCount)
                            putExtra(AppConstants.SKU_CREATED, true)
                            putExtra(AppConstants.SKU_ID, item.skuId)
                            putExtra(AppConstants.SKU_UUID, item.uuid)
                            putExtra(AppConstants.EXTERIOR_ANGLES, item.initialFrames)
                            putExtra(AppConstants.EXTERIOR_SIZE, if (item.imagesCount < item.totalFrames ?: 0) item.imagesCount else item.totalFrames)
                            putExtra("is_paid", item.isPaid)
                            //putExtra(AppConstants.IMAGE_TYPE,item.category)
                            putExtra(AppConstants.IS_360, item.isThreeSixty)
                            putExtra(AppConstants.RESUME_EXTERIOR, true)
                            putExtra(AppConstants.RESUME_INTERIOR, false)
                            putExtra(AppConstants.RESUME_MISC, false)
                        }

                        if (!item.videoId.isNullOrEmpty()) {
                            intent?.apply {
                                putExtra(AppConstants.FROM_VIDEO, true)
                                putExtra(AppConstants.TOTAL_FRAME, item.imagesCount)
                            }
                        }

                        intent?.let {
                            context.startActivity(intent)
                        }
                    }else {
                        val projectDetailsIntent = Intent(context, ProjectDetailsActivity::class.java)
                        projectDetailsIntent.apply {
                            putExtra(AppConstants.CATEGORY_ID, item.categoryId)
                            putExtra(AppConstants.PROJECT_UUIID, item.projectUuid)
                            putExtra(AppConstants.SKU_UUID,item.uuid)
                            context.startActivity(this)
                        }
                    }
                }
            }
        } else {
            bindingGrid?.let {
                it.ivSkuStatus.setImageResource(R.drawable.draft_prj)
                it.tvStatus.text = "Draft"
                it.ivDownload.visibility = View.GONE
                it.ivDownload.visibility = View.INVISIBLE
                it.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.draft_text_colour))
                it.tvCategory.text =
                    if (item.subcategoryName.isNullOrEmpty()) item.categoryName else item.subcategoryName
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
                it.tvImages.text = "Images: ${item.imagesCount}"

                it.clSku.setOnClickListener {
                    Utilities.savePrefrence(
                        context,
                        AppConstants.SKU_ID,
                        item.skuId
                    )

                    if (item.status == "draft" || item.status == "Draft"){
                        var intent: Intent? = null
                        if (item.imagesCount > 0) {
                            intent = Intent(
                                context,
                                ShootPortraitActivity::class.java
                            )
                        } else {
                            intent = Intent(
                                context,
                                ShootPortraitActivity::class.java
                            )
                        }

                        intent?.apply {
                            putExtra(AppConstants.FROM_LOCAL_DB, true)
                            putExtra(AppConstants.FROM_DRAFTS, true)
                            putExtra(AppConstants.PROJECT_ID, item.projectId)
                            putExtra(AppConstants.PROJECT_UUIID, item.projectUuid)
                            putExtra(AppConstants.CATEGORY_NAME, item.categoryName)
                            putExtra(AppConstants.CATEGORY_ID, item.categoryId)
                            putExtra(AppConstants.SUB_CAT_ID, item.subcategoryId)
                            putExtra(AppConstants.SUB_CAT_NAME, item.subcategoryName)
                            putExtra(AppConstants.SKU_NAME, item.skuName)
                            putExtra(AppConstants.PROJECT_NAME, item.skuName)
                            putExtra(AppConstants.IMAGE_COUNT, item.imagesCount)
                            putExtra(AppConstants.SKU_CREATED, true)
                            putExtra(AppConstants.SKU_ID, item.skuId)
                            putExtra(AppConstants.SKU_UUID, item.uuid)
                            putExtra(AppConstants.EXTERIOR_ANGLES, item.initialFrames)
                            putExtra(AppConstants.EXTERIOR_SIZE, item.totalFrames)
                            putExtra("is_paid", item.isPaid)
                            //putExtra(AppConstants.IMAGE_TYPE,item.category)
                            putExtra(AppConstants.IS_360, item.isThreeSixty)
                            putExtra(AppConstants.RESUME_EXTERIOR, true)
                            putExtra(AppConstants.RESUME_INTERIOR, false)
                            putExtra(AppConstants.RESUME_MISC, false)
                        }

                        if (!item.videoId.isNullOrEmpty()) {
                            intent?.apply {
                                putExtra(AppConstants.FROM_VIDEO, true)
                                putExtra(AppConstants.TOTAL_FRAME, item.imagesCount)
                            }
                        }

                        intent?.let {
                            context.startActivity(intent)
                        }
                    }else {
                        val projectDetailsIntent = Intent(context, ProjectDetailsActivity::class.java)
                        projectDetailsIntent.apply {
                            putExtra(AppConstants.CATEGORY_ID, item.categoryId)
                            putExtra(AppConstants.PROJECT_UUIID, item.projectUuid)
                            putExtra(AppConstants.SKU_UUID,item.uuid)
                            context.startActivity(this)
                        }
                    }
                }
            }

        }
    }

}