package com.spyneai.service

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.getDelay
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.shootapp.repository.model.project.Project
import com.spyneai.shootapp.repository.model.project.ProjectDao
import com.spyneai.shootapp.data.ShootRepository
import com.spyneai.shootapp.repository.model.payment.SubmitPaidProjectUpdatedBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProjectSubmitSync(
    val context: Context,
    val projectDaoApp: ProjectDao,
    val listener: DataSyncListener,
    var retryCount: Int = 0,
    var connectionLost: Boolean = false,
    var isActive: Boolean = false
) {

    val TAG = "SubmitProjectSync"

    companion object {
        @Volatile
        private var INSTANCE: ProjectSubmitSync? = null

        fun getInstance(context: Context, listener: DataSyncListener): ProjectSubmitSync {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = ProjectSubmitSync(
                        context,
                        SpyneAppDatabase.getInstance(BaseApplication.getContext()).projectDao(),
                        listener
                    )

                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    fun submitProjectParent(type: String, startedBy: String?) {
        context.captureEvent(Events.PROCESS_SKU_PARENT_TRIGGERED, HashMap<String, Any?>().apply {
            put("type", type)
            put("service_started_by", startedBy)
            put("upload_running", isActive)
        })

        //update triggered value
        Utilities.saveBool(context, AppConstants.SUBMIT_PROJECT_TRIGGERED, true)

        if (Utilities.getBool(context, AppConstants.SUBMIT_PROJECT_TRIGGERED, true)
            &&
            !isActive
        ) {
            if (context.isInternetActive())
                GlobalScope.launch(Dispatchers.Default) {
                    isActive = true
                    context.captureEvent(Events.SUBMIT_PROJECT_STARTED, HashMap())
                    startSubmittingProjects()
                }
            else {
                isActive = false
                listener.onConnectionLost("Submit Project Stopped", ServerSyncTypes.SUBMIT_PROJECT)
            }
        } else {
            Log.d(TAG, "submitProjectParent: running")
        }
    }

    private suspend fun startSubmittingProjects() {
        do {
            val projectIdsList = projectDaoApp.getPaidProjectIds()

            if (connectionLost) {
                val count = projectDaoApp.getSubmissionPendingProjects()
                context.captureEvent(
                    Events.PROCESS_SKU_CONNECTION_CONNECTION_BREAK,
                    HashMap<String, Any?>()
                        .apply {
                            put("project_remaining", count)
                        }
                )
                isActive = false
                listener.onConnectionLost("Project Submission Stopped", ServerSyncTypes.CREATE)
                break
            }


            if (projectIdsList.isNullOrEmpty()) {
                val count = projectDaoApp.getSubmissionPendingProjects()
                context.captureEvent(
                    Events.ALL_PROJECTS_SUBMITTED_BREAKS,
                    HashMap<String, Any?>()
                        .apply {
                            put("project_remaining", count)
                        }
                )
                break
            } else {
                val properties = HashMap<String, Any?>()
                    .apply {
                        put("data", Gson().toJson(projectIdsList))
                    }

                context.captureEvent(
                    Events.SKU_SELECTED,
                    properties
                )

                if (retryCount > 4) {
                    //skip project
                    projectIdsList.forEach {
                        val skip = projectDaoApp.skipProject(
                            it.uuid,
                            it.toProcessAt.plus(it.retryCount * it.retryCount.getDelay())
                        )

                        context.captureEvent(
                            Events.PROJECT_SUBMISSION_SKIPPED,
                            properties.apply {
                                put("db_count", skip)
                            }
                        )
                    }

                    retryCount = 0
                    continue
                }

                //in progress listener
                listener.inProgress(
                    "Submitting Project",
                    ServerSyncTypes.SUBMIT_PROJECT
                )
                isActive = true

                submitProject(projectIdsList)

                continue


            }
        } while (!projectIdsList.isNullOrEmpty())

        if (!connectionLost) {
            listener.onCompleted(
                "All Projects Submitted For Processing",
                ServerSyncTypes.SUBMIT_PROJECT
            )
            isActive = false
        }
    }

    private suspend fun submitProject(paidProjectAppList: List<Project>): Boolean {

        val project = paidProjectAppList[0]

        val orderId = project.orderId

        if (orderId != null){
            val projectList = projectDaoApp.getProjectsByOrderId(orderId)

            val properties = HashMap<String, Any?>()
                .apply {
                    put("data", Gson().toJson(projectList))
                }

            val idsList = projectList.map {
                it.projectId
            }

            val creditDetailsList = ArrayList<SubmitPaidProjectUpdatedBody.CreditDetail>()

//            Log.d(TAG, "ProjectCreditDetails: "+creditDetailsList[0].toString())


            idsList.forEach {
                val skuCreditsList = ArrayList<SubmitPaidProjectUpdatedBody.CreditDetail.Sku>()

                val skuList = projectDaoApp.getSkusByProjectId(it.toString())

                skuList.forEach { sku ->
                    val skuCreditDetails = SubmitPaidProjectUpdatedBody.CreditDetail.Sku(
                        skuId = sku.skuId.toString(),
                        total = SubmitPaidProjectUpdatedBody.CreditDetail.Sku.Total(if(sku.credits?.total?.credit == null) 0 else sku.credits?.total?.credit!!,if (sku.credits?.total?.images == null) 0 else sku.credits?.total?.images!!),
                        exterior = SubmitPaidProjectUpdatedBody.CreditDetail.Sku.Exterior(sku?.credits?.exterior?.credit,sku?.credits?.exterior?.images),
                        interior = SubmitPaidProjectUpdatedBody.CreditDetail.Sku.Interior(sku?.credits?.interior?.credit,sku?.credits?.interior?.images),
                        miscellanous = SubmitPaidProjectUpdatedBody.CreditDetail.Sku.Miscellanous(sku?.credits?.miscellanous?.credit,sku?.credits?.miscellanous?.images)
                    )
                    skuCreditsList.add(skuCreditDetails)
                }

                val creditDetails = SubmitPaidProjectUpdatedBody.CreditDetail(
                    projectId = project.projectId!!,
                    total = SubmitPaidProjectUpdatedBody.CreditDetail.Total(if (project.credits?.total?.credit == null) 0 else project.credits?.total?.credit!!,if (project.credits?.total?.images == null) 0 else project.credits?.total?.images!!),
                    exterior = SubmitPaidProjectUpdatedBody.CreditDetail.Exterior(project?.credits?.exterior?.credit,project?.credits?.exterior?.images),
                    interior = SubmitPaidProjectUpdatedBody.CreditDetail.Interior(project?.credits?.interior?.credit,project?.credits?.interior?.images),
                    miscellanous = SubmitPaidProjectUpdatedBody.CreditDetail.Miscellanous(project?.credits?.miscellanous?.credit,project?.credits?.miscellanous?.images),
                    skuList = skuCreditsList
                )

                creditDetailsList.add(creditDetails)
            }

            val updatedBody = SubmitPaidProjectUpdatedBody(
                orderId = orderId,
                projectIdList = idsList,
                creditDetails = creditDetailsList,
                authKey = Utilities.getPreference(context,AppConstants.AUTH_KEY).toString(),
            )

            val response = ShootRepository().submitPaidProject(updatedBody)

            context.captureEvent(
                Events.PROJECT_SUBMISSION_INTIATED,
                properties
            )

            if (response is Resource.Failure) {
                context.captureEvent(
                    Events.PROJECT_SUBMISSION_FAILED,
                    properties.apply {
                        put("response", response)
                        put("throwable", response.throwable)
                    }
                )
                retryCount++
                return false
            }

            context.captureEvent(
                Events.PROJECT_SUBMITTED,
                properties.apply {
                    put("response", response)
                }
            )

            //update submitted project
            projectList.forEach {
                it.isSubmitted = true
                val updateCount = projectDaoApp.updateProject(it)

                context.captureEvent(
                    Events.SKU_PROCESSED_IN_DB_UPDATED,
                    properties.apply {
                        put("db_count", updateCount)
                    }
                )
            }
            retryCount = 0
            return true
        }else{
            retryCount++
            return false
        }
    }
}