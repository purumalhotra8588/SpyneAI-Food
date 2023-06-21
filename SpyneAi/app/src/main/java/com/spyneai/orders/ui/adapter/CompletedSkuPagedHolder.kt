package com.spyneai.orders.ui.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
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
import com.spyneai.processedimages.ui.ProcessedImageActivity
import com.spyneai.shootapp.repository.model.sku.Sku


class CompletedSkuPagedHolder(
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
        ): CompletedSkuPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = if (!Utilities.getBool(context, "viewTypeGrid", false)) {
                inflater.inflate(R.layout.item_linear_sku_v2, parent, false)
            } else {
                inflater.inflate(R.layout.item_grid_sku_v2, parent, false)
            }
            return CompletedSkuPagedHolder(context, view)
        }
    }

    fun bind(item: Sku?) {
        item?.let { showData(it) }
    }

    private fun showData(item: Sku) {
        if (!Utilities.getBool(context, "viewTypeGrid", false)) {
            bindingLinear?.let {
                it.ivSkuStatus.setImageResource(R.drawable.prjcomplete)
                it.tvStatus.text = "Complete"
                it.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.complete_prj))
                it.tvCategory.text =
                    if (item.subcategoryName.isNullOrEmpty()) item.categoryName else item.subcategoryName
                it.tvDate.text = getFormattedDate(item.createdAt)

                if (item.categoryName.isNullOrEmpty() && item.subcategoryName.isNullOrEmpty()) {
                    it.tvCategory.text = when (item.categoryId) {
                        "cat_d8R14zUNx" -> "Bikes"
                        "cat_d8R14zUNE" -> "Automobile"
                        "cat_Ujt0kuFxY" -> "Ecom"
                        "cat_Ujt0kuFxF" -> "Food & Beverages"
                        "cat_P4t6BRVCxx" -> "Health & Beauty"
                        "cat_P4t6BRVAyy" -> "Accessories"
                        "cat_P4t6BRVART" -> "Womens Fashion"
                        "cat_P4t6BRVAMN" -> "Mens Fashion"
                        "cat_Ujt0kuFxX" -> "Footwear"
                        "cat_P4t6BRVCAP" -> "Caps"
                        "cat_skJ7HIvnc" -> "Fashion"
                        else -> {
                            ""
                        }
                    }
                }

                try {
                    if (item.thumbnail.isNullOrEmpty()) {
                        Glide.with(context)
                            .load(R.drawable.spyne_thumbnail)
                            .into(it.skuThumbnail)
                    } else {
                        if (item.categoryId == AppConstants.CARS_CATEGORY_ID || item.categoryId == AppConstants.BIKES_CATEGORY_ID) {
                            Glide.with(context)
                                .load(item.thumbnail)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .skipMemoryCache(false)
                                .thumbnail(Glide.with(context).load(R.drawable.placeholder_gif))
                                .into(it.skuThumbnail)
                        } else {
                            context.loadSmartlyWithCache(
                                item.thumbnail,
                                it.skuThumbnail
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }

                if (context.getString(R.string.app_name) == AppConstants.SWEEP) {
                    apply {
                        it.tvCategory.visibility = View.INVISIBLE
                        it.tvCategories.visibility = View.INVISIBLE
                    }
                } else {
                }
                it.tvSkuNameValue.text = item.skuName
                it.tvImages.text = "Images: ${item.imagesCount}"
                it.tvDate.text = getFormattedDate(item.createdAt)


                if (Utilities.getPreference(context, AppConstants.SHOOT_TYPE) == "Inspection") {
                    it.tvImages.text = "Download Report"
                }

                if (Utilities.getBool(
                        context,
                        AppConstants.CHECK_QC,
                        false
                    ) && Utilities.getPreference(context, AppConstants.SHOOT_TYPE) != "Inspection"
                ) {
                    item.qcStatus.let {
                        bindingLinear?.llQcStatus?.visibility = View.VISIBLE
                        when (it) {
                            "approved" -> {
                                bindingLinear?.ivQcStatus?.setBackgroundResource(R.drawable.qcapproved)
                                bindingLinear?.tvQcStatus?.text = "Assured"

                            }
                            "in_progress" -> {
                                bindingLinear?.ivQcStatus?.setBackgroundResource(R.drawable.qcinprogress)
                                bindingLinear?.tvQcStatus?.text = "Under review"
                            }
                            "rejected" -> {
                                bindingLinear?.ivQcStatus?.setBackgroundResource(R.drawable.qcreshoot)
                                bindingLinear?.tvQcStatus?.text = "Reshoot required"
                            }
                            "qc_done" -> {
                                bindingLinear?.ivQcStatus?.setBackgroundResource(R.drawable.qcapproved)
                                bindingLinear?.tvQcStatus?.text = "Assured"
                            }
                            "reshoot" -> {
                                bindingLinear?.ivQcStatus?.setBackgroundResource(R.drawable.qcreshoot)
                                bindingLinear?.tvQcStatus?.text = "Reshoot required"
                            }
                            else -> {
                                bindingLinear?.ivQcStatus?.setBackgroundResource(R.drawable.qcinprogress)
                                bindingLinear?.tvQcStatus?.text = "Under review"
                            }
                        }
                    }
                } else {
                    bindingLinear?.llQcStatus?.visibility = View.GONE
                    bindingLinear?.ivArrow?.visibility = View.GONE
                    bindingLinear?.llQcMessage?.visibility = View.GONE
                }

                it.clSku.setOnClickListener {
                        if (Utilities.getPreference(
                            context,
                            AppConstants.SHOOT_TYPE
                        ) == "Inspection"
                    ) {

                        val url = if (AppConstants.BASE_URL.contains("beta")) {
                            "https://beta-web.spyne.xyz/pdf/inspection?" + "sku_id=" + item.skuId +
                                    "&auth_key=" + Utilities.getPreference(
                                context,
                                AppConstants.AUTH_KEY
                            )
                        } else {
                            "https://www.spyne.ai/pdf/inspection?" + "sku_id=" + item.skuId +
                                    "&auth_key=" + Utilities.getPreference(
                                context,
                                AppConstants.AUTH_KEY
                            )
                        }

                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(url)
                        context.startActivity(i)

                    } else {
                        Utilities.savePrefrence(
                            context,
                            AppConstants.SKU_ID,
                            item.skuId
                        )

                        val intent = Intent(
                            context,
                            ProcessedImageActivity::class.java
                        )
                        intent.putExtra(AppConstants.PROJECT_ID, item.projectId)
                        intent.putExtra(AppConstants.PROJECT_UUIID, item.projectUuid)
                        intent.putExtra(AppConstants.SKU_ID, item.skuId)
                        intent.putExtra(AppConstants.SKU_UUID, item.uuid)
                        intent.putExtra(AppConstants.SKU_NAME, item.skuName)
                        intent.putExtra(AppConstants.CATEGORY_ID, item.categoryId)
                        intent.putExtra(AppConstants.CATEGORY_NAME, item.categoryName)
                        intent.putExtra(AppConstants.SUB_CAT_ID, item.subcategoryId)
                        intent.putExtra(AppConstants.EXTERIOR_ANGLES, item.initialFrames)
                        intent.putExtra(AppConstants.FROM_VIDEO, item.videoPresent.toString())
                        intent.putExtra(AppConstants.VIDEO_URL_360, item.videoUrl)
                        intent.putExtra("is_paid", item.isPaid)
                        intent.putExtra(AppConstants.IMAGE_TYPE, item.categoryName)
                        intent.putExtra(AppConstants.IS_360, item.isThreeSixty)
                        Log.d("xyz", "itemSku" + item.isThreeSixty)
                        context.startActivity(intent)
                    }
                }

                it.llQcStatus.setOnClickListener {
                    if (bindingLinear?.llQcMessage?.visibility == View.GONE) {
                        bindingLinear?.ivArrow?.visibility = View.VISIBLE
                        bindingLinear?.llQcMessage?.visibility = View.VISIBLE
                    } else {
                        bindingLinear?.ivArrow?.visibility = View.GONE
                        bindingLinear?.llQcMessage?.visibility = View.GONE
                    }
                }
            }
        } else {
            bindingGrid?.let {
                it.ivSkuStatus.setImageResource(R.drawable.prjcomplete)
                it.tvStatus.text = "Complete"
                it.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.complete_prj))
                it.tvCategory.text =
                    if (item.subcategoryName.isNullOrEmpty()) item.categoryName else item.subcategoryName
                it.tvDate.text = getFormattedDate(item.createdAt)

//            if (item.isPaid)
//                it.tvPaid.visibility = View.VISIBLE
//            else
//                it.tvPaid.visibility = View.GONE
                if (item.categoryName.isNullOrEmpty() && item.subcategoryName.isNullOrEmpty()) {
                    it.tvCategory.text = when (item.categoryId) {
                        "cat_d8R14zUNx" -> "Bikes"
                        "cat_d8R14zUNE" -> "Automobile"
                        "cat_Ujt0kuFxY" -> "Ecom"
                        "cat_Ujt0kuFxF" -> "Food & Beverages"
                        "cat_P4t6BRVCxx" -> "Health & Beauty"
                        "cat_P4t6BRVAyy" -> "Accessories"
                        "cat_P4t6BRVART" -> "Womens Fashion"
                        "cat_P4t6BRVAMN" -> "Mens Fashion"
                        "cat_Ujt0kuFxX" -> "Footwear"
                        "cat_P4t6BRVCAP" -> "Caps"
                        "cat_skJ7HIvnc" -> "Fashion"
                        else -> {
                            ""
                        }
                    }
                }

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

                if (context.getString(R.string.app_name) == AppConstants.SWEEP) {
                    apply {
                        it.tvCategory.visibility = View.INVISIBLE
                        // it.flCategory.visibility = View.INVISIBLE
                        //it.flImages.visibility = View.INVISIBLE
                        it.tvCategories.visibility = View.INVISIBLE
                        // it.tvImage.visibility = View.INVISIBLE
                    }
                } else {
//                it.tvCategory.text = item.categoryName
                }

//            it.tvPaid.text = if (item.isPaid) "Paid" else "Un Paid"
                it.tvSkuNameValue.text = item.skuName
                it.tvImages.text = "Images: ${item.imagesCount}"
                it.tvDate.text = getFormattedDate(item.createdAt)

                if (Utilities.getPreference(context, AppConstants.SHOOT_TYPE) == "Inspection") {
                    it.tvImages.text = "Download Report"
                }

                if (Utilities.getBool(
                        context,
                        AppConstants.CHECK_QC,
                        false
                    ) && Utilities.getPreference(context, AppConstants.SHOOT_TYPE) != "Inspection"
                ) {
                    item.qcStatus.let {
                        bindingGrid?.llQcStatus?.visibility = View.VISIBLE
                        when (it) {
                            "approved" -> {
                                bindingGrid?.ivQcStatus?.setBackgroundResource(R.drawable.qcapproved)
                                bindingGrid?.tvQcStatus?.text = "Assured"

                            }
                            "in_progress" -> {
                                bindingGrid?.ivQcStatus?.setBackgroundResource(R.drawable.qcinprogress)
                                bindingGrid?.tvQcStatus?.text = "Under review"
                            }
                            "rejected" -> {
                                bindingGrid?.ivQcStatus?.setBackgroundResource(R.drawable.qcreshoot)
                                bindingGrid?.tvQcStatus?.text = "Reshoot required"
                            }
                            "qc_done" -> {
                                bindingGrid?.ivQcStatus?.setBackgroundResource(R.drawable.qcapproved)
                                bindingGrid?.tvQcStatus?.text = "Assured"
                            }
                            "reshoot" -> {
                                bindingGrid?.ivQcStatus?.setBackgroundResource(R.drawable.qcreshoot)
                                bindingGrid?.tvQcStatus?.text = "Reshoot required"
                            }
                            else -> {
                                bindingGrid?.ivQcStatus?.setBackgroundResource(R.drawable.qcinprogress)
                                bindingGrid?.tvQcStatus?.text = "Under review"
                            }
                        }
                    }
                } else {
                    bindingGrid?.llQcStatus?.visibility = View.GONE
                    bindingGrid?.ivArrow?.visibility = View.GONE
                    bindingGrid?.llQcMessage?.visibility = View.GONE
                }

                it.clSku.setOnClickListener {
                        if(Utilities.getPreference(context,AppConstants.SHOOT_TYPE)=="Inspection"){

                            val url = if (AppConstants.BASE_URL.contains("beta")) {
                                "https://beta-web.spyne.xyz/pdf/inspection?" + "sku_id=" + item.skuId +
                                        "&auth_key=" + Utilities.getPreference(
                                    context,
                                    AppConstants.AUTH_KEY
                                )
                            } else {
                                "https://www.spyne.ai/pdf/inspection?" + "sku_id=" + item.skuId +
                                        "&auth_key=" + Utilities.getPreference(
                                    context,
                                    AppConstants.AUTH_KEY
                                )
                            }

                            val i = Intent(Intent.ACTION_VIEW)
                            i.data = Uri.parse(url)
                            context.startActivity(i)

                        } else {
                            Utilities.savePrefrence(
                                context,
                                AppConstants.SKU_ID,
                                item.skuId
                            )

                            val intent = Intent(
                                context,
                                ProcessedImageActivity::class.java
                            )
                            intent.putExtra(AppConstants.PROJECT_ID, item.projectId)
                            intent.putExtra(AppConstants.PROJECT_UUIID, item.projectUuid)
                            intent.putExtra(AppConstants.CATEGORY_ID, item.categoryId)
                            intent.putExtra(AppConstants.SUB_CAT_ID, item.subcategoryId)
                            intent.putExtra(AppConstants.SUB_CAT_NAME, item.subcategoryName)
                            intent.putExtra(AppConstants.CATEGORY_NAME, item.categoryName)
                            intent.putExtra(AppConstants.SKU_NAME, item.skuName)
                            intent.putExtra(AppConstants.PROJECT_NAME, item.skuName)
                            intent.putExtra(AppConstants.SKU_CREATED, true)
                            intent.putExtra(AppConstants.FROM_VIDEO, item.videoPresent.toString())
                            intent.putExtra(AppConstants.IMAGE_COUNT, item.imagesCount)
                            intent.putExtra(AppConstants.SKU_ID, item.skuId)
                            intent.putExtra(AppConstants.SKU_UUID, item.uuid)
                            intent.putExtra(AppConstants.VIDEO_URL_360, item.videoUrl)
                            intent.putExtra(AppConstants.EXTERIOR_ANGLES, item.initialFrames)
                            intent.putExtra("is_paid", item.isPaid)
                            intent.putExtra(AppConstants.IMAGE_TYPE, item.categoryName)
                            intent.putExtra(AppConstants.IS_360, item.isThreeSixty)
                            intent.putExtra(AppConstants.SHOOT_TYPE, item.shootType)
                            context.startActivity(intent)
                        }
                }


                it.llQcStatus.setOnClickListener {
                    if (bindingGrid?.llQcMessage?.visibility == View.GONE) {
                        bindingGrid?.ivArrow?.visibility = View.VISIBLE
                        bindingGrid?.llQcMessage?.visibility = View.VISIBLE
                    } else {
                        bindingGrid?.ivArrow?.visibility = View.GONE
                        bindingGrid?.llQcMessage?.visibility = View.GONE
                    }
                }

            }

        }
    }

}