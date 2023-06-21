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
import com.spyneai.databinding.ItemGridProjectV2Binding
import com.spyneai.databinding.ItemLinearProjectV2Binding
import com.spyneai.getFormattedDate
import com.spyneai.loadSmartlyWithCache
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.ui.activity.SkuPagedActivity
import com.spyneai.shootapp.repository.model.project.Project


@ExperimentalPagingApi
class OngoingPagedHolder(
    val context: Context,
    val view: View
) : RecyclerView.ViewHolder(view) {

    var bindingGrid : ItemGridProjectV2Binding? = null
    var bindingLinear : ItemLinearProjectV2Binding? = null

    init {
        if(!Utilities.getBool(context, "viewTypeGrid" , false)){
            bindingLinear = ItemLinearProjectV2Binding.bind(itemView)
        }else{
            bindingGrid = ItemGridProjectV2Binding.bind(itemView)
        }
    }

    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(
            context: Context,
            parent: ViewGroup
        ): OngoingPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = if(!Utilities.getBool(context, "viewTypeGrid" , false)) {
                inflater.inflate(R.layout.item_linear_project_v2, parent, false)
            }else{
                inflater.inflate(R.layout.item_grid_project_v2, parent, false)
            }
            return OngoingPagedHolder(context, view)
        }
    }

    fun bind(item: Project?) {
        item?.let { showData(it) }
    }

    private fun showData(item: Project) {

        if (!Utilities.getBool(context, "viewTypeGrid", false)) {
            bindingLinear?.let {
                it.tvCategory.text = item.categoryName

                it.tvProjectNameValue.text = item.projectName
                it.ivDownload.visibility = View.GONE

                it.tvImages.visibility = View.VISIBLE

                it.tvSkus.text = "SKUs : " + item.skuCount.toString()

                it.tvImages.text = "Images: ${item.imagesCount}"

                it.tvDate.text = getFormattedDate(item.createdAt)
                //need thumbnal
                try {
                    if (item.thumbnail.isNullOrEmpty()) {
                        Glide.with(context)
                            .load(R.drawable.spyne_thumbnail)
                            .into(it.prjThumbnail)
                    } else {
                        context.loadSmartlyWithCache(item.thumbnail, it.prjThumbnail)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }

                it.clProject.setOnClickListener {

                    if (item.skuCount == 0) {
                        Toast.makeText(context, "No SKU data found", Toast.LENGTH_SHORT).show()
                    } else {
                        Intent(context, SkuPagedActivity::class.java)
                            .apply {
                                putExtra(AppConstants.STATUS, "ongoing")
                                putExtra("position", position)
                                putExtra(AppConstants.FROM_LOCAL_DB, true)
                                putExtra(AppConstants.PROJECT_NAME, item.projectName)
                                putExtra(AppConstants.SKU_COUNT, item.skuCount)
                                putExtra(AppConstants.PROJECT_UUIID, item.uuid)
                                putExtra(AppConstants.PROJECT_ID, item.projectId)
                                putExtra(AppConstants.TAB_ID,1)
                                context.startActivity(this)
                            }
                    }
                }
            }
        }else{
            bindingGrid?.let {
//            it.llThreeSixty.visibility = View.GONE
                it.tvCategory.text = item.categoryName

                it.ivProjectStatus.setImageResource(R.drawable.ongoing_prj)
                it.tvStatus.text = "Processing"
                it.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.ongoing_text_colour
                    )
                )
                it.ivDownload.visibility = View.GONE

                it.tvImages.visibility = View.VISIBLE

                it.tvSkus.text = "SKUs : " + item.skuCount.toString()

                it.tvImages.text = "Images: ${item.imagesCount}"

                it.tvProjectName.text = item.projectName
                it.tvDate.text = getFormattedDate(item.createdAt)
                //need thumbnal
                try {
                    if (item.thumbnail.isNullOrEmpty()) {
                        Glide.with(context)
                            .load(R.drawable.spyne_thumbnail)
                            .into(it.prjThumbnail)
                    } else {
                        if (item.categoryId == AppConstants.CARS_CATEGORY_ID || item.categoryId == AppConstants.BIKES_CATEGORY_ID){
                        Glide.with(context)
                            .load(item.thumbnail)
                            .thumbnail(Glide.with(context).load(R.drawable.placeholder_gif))
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .skipMemoryCache(false)
                            .into(it.prjThumbnail)
                    }else {
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

                it.clProject.setOnClickListener {

                    if (item.skuCount == 0) {
                        Toast.makeText(context, "No SKU data found", Toast.LENGTH_SHORT).show()
                    } else {
                        Intent(context, SkuPagedActivity::class.java)
                            .apply {
                                putExtra(AppConstants.STATUS, "ongoing")
                                putExtra("position", position)
                                putExtra(AppConstants.FROM_LOCAL_DB, true)
                                putExtra(AppConstants.PROJECT_NAME, item.projectName)
                                putExtra(AppConstants.SKU_COUNT, item.skuCount)
                                putExtra(AppConstants.PROJECT_UUIID, item.uuid)
                                putExtra(AppConstants.PROJECT_ID, item.projectId)
                                putExtra(AppConstants.TAB_ID,1)
                                context.startActivity(this)
                            }
                    }
                }
            }

        }
    }
}