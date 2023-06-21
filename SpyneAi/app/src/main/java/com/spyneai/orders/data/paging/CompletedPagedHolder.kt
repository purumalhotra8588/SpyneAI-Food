package com.spyneai.orders.data.paging

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.databinding.ItemGridProjectV2Binding
import com.spyneai.databinding.ItemLinearProjectV2Binding
import com.spyneai.getFormattedDate
import com.spyneai.getTimeStamp
import com.spyneai.loadSmartlyWithCache
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.ui.activity.SkuPagedActivity
import com.spyneai.shootapp.repository.model.project.Project


@ExperimentalPagingApi
class CompletedPagedHolder(
    val context: Context,
    val view: View
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
        fun getInstance(context: Context, parent: ViewGroup): CompletedPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = if (!Utilities.getBool(context, "viewTypeGrid", false)) {
                inflater.inflate(R.layout.item_linear_project_v2, parent, false)
            } else {
                inflater.inflate(R.layout.item_grid_project_v2, parent, false)
            }
            return CompletedPagedHolder(context, view)
        }
    }

    fun bind(item: Project?) {
        item?.let {
            showData(item)
        }
    }


    private fun showData(item: Project) {

        if (!Utilities.getBool(context, "viewTypeGrid", false)) {
            bindingLinear?.let {

                it.tvCategory.text = item.categoryName
//                it.llThreeSixty.visibility = View.GONE

                it.tvSkus.text = "Skus: " + item.skuCount.toString()
                it.tvImages.text = "Images: ${item.imagesCount}"

                try {
                    if (item.thumbnail.isNullOrEmpty()) {
                        Glide.with(context)
                            .load(R.drawable.spyne_thumbnail)
                            .into(it.prjThumbnail)
                    } else {
                        context.loadSmartlyWithCache(item.thumbnail, it.prjThumbnail)
                    }
                    if(item.createdOn.contains("2022"))
                        it.tvDate.text = getFormattedDate(getTimeStamp(item.createdOn))
                    else
                        it.tvDate.text = getFormattedDate(item.createdOn.toLong())
                } catch (e: Exception) {
                    e.printStackTrace()
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }


                if(item.shoot_type=="Inspection"){
                    it.tvImages.text="Car Inspection"
                    it.tvSkus.visibility=View.GONE
                }

                if(Utilities.getBool(context,AppConstants.CHECK_QC,false) && item.shoot_type != "Inspection") {
                    item.qcStatus.let {
                        bindingLinear?.llQcStatus?.visibility=View.VISIBLE
                        when (it) {
                            "approved"->{
                                bindingLinear?.ivQcStatus?.setBackgroundResource(R.drawable.qcapproved)
                                bindingLinear?.tvQcStatus?.text="Assured"

                            }
                            "in_progress"->{
                                bindingLinear?.ivQcStatus?.setBackgroundResource(R.drawable.qcinprogress)
                                bindingLinear?.tvQcStatus?.text="Under review"
                            }
                            "rejected"->{
                                bindingLinear?.ivQcStatus?.setBackgroundResource(R.drawable.qcreshoot)
                                bindingLinear?.tvQcStatus?.text="Reshoot required"
                            }
                            "qc_done"->{
                                bindingLinear?.ivQcStatus?.setBackgroundResource(R.drawable.qcapproved)
                                bindingLinear?.tvQcStatus?.text="Assured"
                            }
                            "reshoot"->{
                                bindingLinear?.ivQcStatus?.setBackgroundResource(R.drawable.qcreshoot)
                                bindingLinear?.tvQcStatus?.text="Reshoot required"
                            }
                            else->{
                                bindingLinear?.ivQcStatus?.setBackgroundResource(R.drawable.qcinprogress)
                                bindingLinear?.tvQcStatus?.text="Under review"
                            }
                        }
                    }
                }else{
                    bindingLinear?.llQcStatus?.visibility = View.GONE
                    bindingLinear?.ivArrow?.visibility=View.GONE
                    bindingLinear?.llQcMessage?.visibility=View.GONE
                }

                it.tvProjectNameValue.text = item.projectName

                it.clProject.setOnClickListener {
                    Utilities.savePrefrence(context,AppConstants.SHOOT_TYPE,item.shoot_type)
                    Intent(context, SkuPagedActivity::class.java)
                        .apply {
                            putExtra(AppConstants.STATUS, "completed")
                            putExtra("position", position)
                            putExtra(AppConstants.FROM_LOCAL_DB, true)
                            putExtra(AppConstants.PROJECT_NAME, item.projectName)
                            putExtra(AppConstants.SKU_COUNT, item.skuCount)
                            putExtra(AppConstants.PROJECT_UUIID, item.uuid)
                            putExtra(AppConstants.PROJECT_ID, item.projectId)
                            putExtra(AppConstants.TAB_ID,2)
                            context.startActivity(this)
                        }
                }

                it.llQcStatus.setOnClickListener{
                    if(bindingLinear?.llQcMessage?.visibility==View.GONE){
                        bindingLinear?.ivArrow?.visibility=View.VISIBLE
                        bindingLinear?.llQcMessage?.visibility=View.VISIBLE
                    }else{
                        bindingLinear?.ivArrow?.visibility=View.GONE
                        bindingLinear?.llQcMessage?.visibility=View.GONE
                    }
                }

            }

        } else {
            bindingGrid?.let {

                it.ivProjectStatus.setImageResource(R.drawable.prjcomplete)
                it.tvStatus.text = "Completed"
                it.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.complete_prj))

                it.tvCategory.text = item.categoryName
//                it.llThreeSixty.visibility = View.GONE

                it.tvSkus.text = "Skus: " + item.skuCount.toString()
                it.tvImages.text = "Images: ${item.imagesCount}"

                try {
                    if (item.thumbnail.isNullOrEmpty()) {
                        Glide.with(context)
                            .load(R.drawable.spyne_thumbnail)
                            .into(it.prjThumbnail)
                    } else {
                        context.loadSmartlyWithCache(item.thumbnail, it.prjThumbnail)
                    }
                    if(item.createdOn.contains("2022"))
                        it.tvDate.text = getFormattedDate(getTimeStamp(item.createdOn))
                    else
                        it.tvDate.text = getFormattedDate(item.createdOn.toLong())
                } catch (e: Exception) {
                    e.printStackTrace()
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }

                if(item.shoot_type=="Inspection"){
                    it.tvImages.text="Car Inspection"
                    it.tvSkus.visibility=View.GONE
                }

                if(Utilities.getBool(context,AppConstants.CHECK_QC,false)  && item.shoot_type != "Inspection") {
                    item.qcStatus.let {
                        bindingGrid?.llQcStatus?.visibility=View.VISIBLE
                        when (it) {
                            "approved"->{
                                bindingGrid?.ivQcStatus?.setBackgroundResource(R.drawable.qcapproved)
                                bindingGrid?.tvQcStatus?.text="Assured"

                            }
                            "in_progress"->{
                                bindingGrid?.ivQcStatus?.setBackgroundResource(R.drawable.qcinprogress)
                                bindingGrid?.tvQcStatus?.text="Under review"
                            }
                            "rejected"->{
                                bindingGrid?.ivQcStatus?.setBackgroundResource(R.drawable.qcreshoot)
                                bindingGrid?.tvQcStatus?.text="Reshoot required"
                            }
                            "qc_done"->{
                                bindingGrid?.ivQcStatus?.setBackgroundResource(R.drawable.qcapproved)
                                bindingGrid?.tvQcStatus?.text="Assured"
                            }
                            "reshoot"->{
                                bindingGrid?.ivQcStatus?.setBackgroundResource(R.drawable.qcreshoot)
                                bindingGrid?.tvQcStatus?.text="Reshoot required"
                            }
                            else->{
                                bindingGrid?.ivQcStatus?.setBackgroundResource(R.drawable.qcinprogress)
                                bindingGrid?.tvQcStatus?.text="Under review"
                            }
                        }
                    }
                }else{
                    bindingGrid?.llQcStatus?.visibility = View.GONE
                    bindingGrid?.ivArrow?.visibility=View.GONE
                    bindingGrid?.llQcMessage?.visibility=View.GONE
                }
                it.tvProjectName.text = item.projectName

                it.clProject.setOnClickListener {

                    Utilities.savePrefrence(context,AppConstants.SHOOT_TYPE,item.shoot_type)

//                    if (item.skuCount == 0) {
//                        Toast.makeText(context, "No SKU data found", Toast.LENGTH_SHORT).show()
//                    } else {
                        Intent(context, SkuPagedActivity::class.java)
                            .apply {
                                putExtra(AppConstants.STATUS, "completed")
                                putExtra("position", position)
                                putExtra(AppConstants.FROM_LOCAL_DB, true)
                                putExtra(AppConstants.PROJECT_NAME, item.projectName)
                                putExtra(AppConstants.SKU_COUNT, item.skuCount)
                                putExtra(AppConstants.PROJECT_UUIID, item.uuid)
                                putExtra(AppConstants.PROJECT_ID, item.projectId)
                                putExtra(AppConstants.TAB_ID,2)
                                context.startActivity(this)
                            }
//                    }
                }

                it.llQcStatus.setOnClickListener{
                    if(bindingGrid?.llQcMessage?.visibility==View.GONE){
                        bindingGrid?.ivArrow?.visibility=View.VISIBLE
                        bindingGrid?.llQcMessage?.visibility=View.VISIBLE
                    }else{
                        bindingGrid?.ivArrow?.visibility=View.GONE
                        bindingGrid?.llQcMessage?.visibility=View.GONE
                    }


                }
            }

        }
    }

}