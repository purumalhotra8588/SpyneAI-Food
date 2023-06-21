package com.spyneai.homev12.holder

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.getFormattedDate
import com.spyneai.loadSmartly
import com.spyneai.loadThumbnail
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.ui.activity.SkuPagedActivity
import com.spyneai.pxFromDp
import com.spyneai.shootapp.repository.model.project.Project


class HomeProjectsHolder(
    val context: Context,
    val view: View
) : RecyclerView.ViewHolder(view) {

    val ivImage: ImageView = view.findViewById(R.id.ivImage)
    val tvProject: TextView = view.findViewById(R.id.tvProject)
    val tvDate: TextView = view.findViewById(R.id.tvDate)
    val tvTime: TextView = view.findViewById(R.id.tvTime)
    val ivStatus: ImageView = view.findViewById(R.id.ivStatus)
    val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    val clBackground: ConstraintLayout = view.findViewById(R.id.clBackground)
    val llFailed: LinearLayout = view.findViewById(R.id.llFailed)
    val llOngoing: LinearLayout = view.findViewById(R.id.llOngoing)
    val llQcStatus: LinearLayout = view.findViewById(R.id.llQcStatus)
    val ivQcStatus: ImageView = view.findViewById(R.id.ivQcStatus)
    val ivArrow: ImageView = view.findViewById(R.id.ivArrow)
    val llQcMessage: LinearLayout = view.findViewById(R.id.llQcMessage)
    val tvQcStatus: TextView = view.findViewById(R.id.tvQcStatus)

    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(
            context: Context,
            parent: ViewGroup
        ): HomeProjectsHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_my_projects_home, parent, false)
            return HomeProjectsHolder(context, view)
        }
    }

    fun bind(item: Project?) {
        item?.let { showData(it) }
    }

    private fun showData(item: Project) {
        tvProject.text = item.projectName
        tvDate.text = getFormattedDate(item.createdAt)
        //tvTime.text = getFormattedTime(item.createdAt)

        try {
            if (item.imagesCount == 0) {
                when (item.status) {
                    "In Progress", "Yet to Start", "ongoing" -> {
                        tvStatus.text = "On-Going"
                        ivStatus.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_clock
                            )
                        )
                        tvStatus.setTextColor(ContextCompat.getColor(context, R.color.light))
                    }

                    "Done", "completed", "done" -> {
                        tvStatus.text = "Completed"
                        tvStatus.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.credit_success
                            )
                        )
                        ivStatus.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_completed
                            )
                        )
                    }

                    else -> {
                        ivStatus.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.draft_prj
                            )
                        )
                        tvStatus.text = "Draft"
                        tvStatus.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.draft_text_colour
                            )
                        )

                    }
                }
//                if (item.subCategoryName == "360_exterior"
//                    || item.subCategoryName.equals("360_interior")){
//                    Glide.with(context)
//                        .load(R.drawable.three_sixty_thumbnail)
//                        .into(ivImage)
//                }else {
//                    Glide.with(context)
//                        .load(R.mipmap.defaults)
//                        .into(ivImage)
//                }
            } else {
                when (item.status) {
                    "In Progress", "Yet to Start", "ongoing" -> {
                        tvStatus.text = "On-Going"
                        ivStatus.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_clock
                            )
                        )
                        tvStatus.setTextColor(ContextCompat.getColor(context, R.color.light))
                    }

                    "Done", "completed", "done" -> {
                        tvStatus.text = "Completed"
                        tvStatus.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.credit_success
                            )
                        )
                        ivStatus.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_completed
                            )
                        )
                    }

                    else -> {
                        ivStatus.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.draft_prj
                            )
                        )
                        tvStatus.text = "Draft"
                        tvStatus.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.draft_text_colour
                            )
                        )
                    }
                }

                item.thumbnail?.let {
                    context.loadThumbnail(
                        it,
                        ivImage,
                        pxFromDp(context, 60f).toInt(),
                        pxFromDp(context, 60f).toInt()
                    )
                }
            }


            if (item.status == "Done" || item.status == "completed" && item.shoot_type != "Inspection") {
                if (Utilities.getBool(context, AppConstants.CHECK_QC, false)) {
                    item.qcStatus.let {
                        llQcStatus.visibility = View.VISIBLE
                        when (it) {
                            "approved" -> {
                                ivQcStatus.setBackgroundResource(R.drawable.qcapproved)
                                tvQcStatus.text = "Assured"
                            }

                            "in_progress" -> {
                                ivQcStatus.setBackgroundResource(R.drawable.qcinprogress)
                                tvQcStatus.text = "Under review"
                            }

                            "rejected" -> {
                                ivQcStatus.setBackgroundResource(R.drawable.qcreshoot)
                                tvQcStatus.text = "Reshoot required"
                            }

                            "qc_done" -> {
                                ivQcStatus.setBackgroundResource(R.drawable.qcapproved)
                                tvQcStatus.text = "Assured"
                            }

                            "reshoot" -> {
                                ivQcStatus.setBackgroundResource(R.drawable.qcreshoot)
                                tvQcStatus.text = "Reshoot required"
                            }

                            else -> {
                                ivQcStatus.setBackgroundResource(R.drawable.qcinprogress)
                                tvQcStatus.text = "Under review"
                            }
                        }
                    }
                }
            } else {
                llQcStatus.visibility = View.GONE
                ivArrow.visibility = View.GONE
                llQcMessage.visibility = View.GONE
            }


        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }

        clBackground.setOnClickListener {

            if (item.shoot_type == "Inspection") {
                if (item.status == "Done" || item.status == "completed") {
                    Utilities.savePrefrence(context, AppConstants.SHOOT_TYPE, item.shoot_type)
                    val intent = Intent(context, SkuPagedActivity::class.java)
                    intent.putExtra(AppConstants.STATUS, item.status)
                    intent.putExtra("type", "Inspection")
                    intent.putExtra("TAB_ID", 1)
                    intent.putExtra(AppConstants.PROJECT_NAME, item.projectName)
                    intent.putExtra(AppConstants.SKU_COUNT, item.skuCount)
                    intent.putExtra(AppConstants.PROJECT_UUIID, item.uuid)
                    intent.putExtra(AppConstants.PROJECT_ID, item.projectId)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(
                        BaseApplication.getContext(),
                        "Draft Feature for dent and damage is not Completed Yet!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val intent = Intent(context, SkuPagedActivity::class.java)
                intent.putExtra(AppConstants.STATUS, item.status)
                intent.putExtra("TAB_ID", 1)
                intent.putExtra(AppConstants.PROJECT_NAME, item.projectName)
                intent.putExtra(AppConstants.SKU_COUNT, item.skuCount)
                intent.putExtra(AppConstants.PROJECT_UUIID, item.uuid)
                intent.putExtra(AppConstants.PROJECT_ID, item.projectId)
                context.startActivity(intent)
            }
        }

        llQcStatus.setOnClickListener {
            if (llQcMessage.visibility == View.GONE) {
                ivArrow.visibility = View.VISIBLE
                llQcMessage.visibility = View.VISIBLE
            } else {
                ivArrow.visibility = View.GONE
                llQcMessage.visibility = View.GONE
            }
        }

    }
}