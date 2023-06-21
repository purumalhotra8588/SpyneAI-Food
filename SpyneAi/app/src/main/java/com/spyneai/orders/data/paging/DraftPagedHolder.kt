package com.spyneai.orders.data.paging

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.databinding.ItemGridProjectV2Binding
import com.spyneai.databinding.ItemLinearProjectV2Binding
import com.spyneai.getFormattedDate
import com.spyneai.loadSmartlyWithCache
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.ui.activity.SkuPagedActivity
import com.spyneai.shootapp.repository.model.project.Project


class DraftPagedHolder(
    val view: View,
    val context: Context
) : RecyclerView.ViewHolder(view) {


    var bindingGrid: ItemGridProjectV2Binding? = null
    var bindingLinear: ItemLinearProjectV2Binding? = null

    init {
        if (!Utilities.getBool(context, "viewTypeGrid", false)) {
            bindingLinear = ItemLinearProjectV2Binding.bind(itemView)
        } else {
            bindingGrid = ItemGridProjectV2Binding.bind(itemView)
        }

    }

    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(context: Context, parent: ViewGroup): DraftPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = if (!Utilities.getBool(context, "viewTypeGrid", false)) {
                inflater.inflate(R.layout.item_linear_project_v2, parent, false)
            } else {
                inflater.inflate(R.layout.item_grid_project_v2, parent, false)
            }
            return DraftPagedHolder(view, context)
        }
    }

    @ExperimentalPagingApi
    fun bind(item: Project?) {
        item?.let {
            showData(item)
        }
    }

    @ExperimentalPagingApi
    private fun showData(item: Project) {

            if (!Utilities.getBool(context, "viewTypeGrid", false)) {
                bindingLinear?.let {
                    it.tvProjectNameValue.text = item.projectName.toString()
                    it.ivDownload.visibility = View.INVISIBLE
                    it.ivDownload.visibility=View.GONE
                    it.tvCategory.text = item.categoryName
//            it.llThreeSixty.visibility = View.GONE

                    try {
                        if (item.thumbnail.isNullOrEmpty()) {
                            Glide.with(context)
                                .load(R.drawable.spyne_thumbnail)
                                .into(it.prjThumbnail)
                        } else {
                            if (item.categoryId == AppConstants.CARS_CATEGORY_ID || item.categoryId == AppConstants.BIKES_CATEGORY_ID) {
                                Glide.with(context)
                                    .load(item.thumbnail)
                                    .thumbnail(Glide.with(context).load(R.drawable.placeholder_gif))
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                    .skipMemoryCache(false)
                                    .into(it.prjThumbnail)
                            } else {
                                context.loadSmartlyWithCache(
                                    item.thumbnail,
                                    it.prjThumbnail
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } catch (e: IndexOutOfBoundsException) {
                        e.printStackTrace()
                    }

                    it.tvSkus.text = "Skus : " + item.skuCount.toString()
                    it.tvDate.text = getFormattedDate(item.createdAt)
                    it.tvImages.text = "Images: ${item.imagesCount}"

                    if(item.shoot_type=="Inspection"){
                        it.tvImages.text="Car Inspection"
                        it.tvSkus.visibility=View.GONE
                    }

                    it.clProject.setOnClickListener {
                        if (item.shoot_type == "Inspection")
                            Toast.makeText(BaseApplication.getContext(),"Draft Feature for dent and damage is not Completed Yet!",Toast.LENGTH_SHORT).show()
                        else
                        context.startActivity(getDraftIntent(item))
                    }
                }
            } else {

                bindingGrid?.let {
                    it.ivProjectStatus.setImageResource(R.drawable.draft_prj)
                    it.tvStatus.text = "Draft"
                    it.tvStatus.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.draft_text_colour
                        )
                    )
                    it.ivDownload.visibility = View.INVISIBLE
                it.ivDownload.visibility=View.GONE

                    it.tvCategory.text = item.categoryName

                    try {
                        if (item.thumbnail.isNullOrEmpty()) {
                            Glide.with(context)
                                .load(R.drawable.spyne_thumbnail)
                                .into(it.prjThumbnail)
                        } else {
                            if (item.categoryId == AppConstants.CARS_CATEGORY_ID || item.categoryId == AppConstants.BIKES_CATEGORY_ID) {
                                Glide.with(context)
                                    .load(item.thumbnail)
                                    .thumbnail(Glide.with(context).load(R.drawable.placeholder_gif))
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                    .skipMemoryCache(false)
                                    .into(it.prjThumbnail)
                            } else {
                                context.loadSmartlyWithCache(
                                    item.thumbnail,
                                    it.prjThumbnail
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } catch (e: IndexOutOfBoundsException) {
                        e.printStackTrace()
                    }

                    it.tvProjectName.text = item.projectName
                    it.tvSkus.text = "Skus : " + item.skuCount.toString()
                    it.tvDate.text = getFormattedDate(item.createdAt)
                    it.tvImages.text = "Images: ${item.imagesCount}"

                    if(item.shoot_type=="Inspection"){
                        it.tvImages.text="Car Inspection"
                        it.tvSkus.visibility=View.GONE
                    }

                    it.clProject.setOnClickListener {
                        if (item.shoot_type == "Inspection")
                            Toast.makeText(BaseApplication.getContext(),"Draft Feature for dent and damage is not Completed Yet!",Toast.LENGTH_SHORT).show()
                        else
                            context.startActivity(getDraftIntent(item))
                    }
                }
        }
    }

    @ExperimentalPagingApi
    fun getDraftIntent(item: Project) = Intent(context, SkuPagedActivity::class.java).apply {
        putExtra(AppConstants.STATUS, "draft")
        putExtra("position", position)
        putExtra(AppConstants.FROM_LOCAL_DB, true)
        putExtra(AppConstants.PROJECT_NAME, item.projectName)
        putExtra(AppConstants.SKU_COUNT, item.skuCount)
        putExtra(AppConstants.PROJECT_UUIID, item.uuid)
        putExtra(AppConstants.PROJECT_ID, item.projectId)
        putExtra(AppConstants.TAB_ID,0)
    }
}