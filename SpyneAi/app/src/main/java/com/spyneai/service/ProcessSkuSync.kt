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
import com.spyneai.shootapp.repository.model.sku.Sku
import com.spyneai.shootapp.repository.model.sku.SkuDao
import com.spyneai.shootapp.data.ProcessRepository
import com.spyneai.shootapp.data.ShootRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProcessSkuSync(
    val context: Context,
    val shootDaoApp: SkuDao,
    val listener: DataSyncListener,
    var retryCount: Int = 0,
    var connectionLost: Boolean = false,
    var isActive: Boolean = false
) {

    val TAG = "ProcessSkuSync"

    companion object {
        @Volatile
        private var INSTANCE: ProcessSkuSync? = null

        fun getInstance(context: Context, listener: DataSyncListener): ProcessSkuSync {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = ProcessSkuSync(
                        context,
                        SpyneAppDatabase.getInstance(BaseApplication.getContext()).skuDao(),
                        listener
                    )

                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    fun processSkuParent(type: String, startedBy: String?) {
        context.captureEvent(Events.PROCESS_SKU_PARENT_TRIGGERED, HashMap<String, Any?>().apply {
            put("type", type)
            put("service_started_by", startedBy)
            put("upload_running", isActive)
        })

        //update triggered value
        Utilities.saveBool(context, AppConstants.PROCESS_SKU_PARENT_TRIGGERED, true)

        if (Utilities.getBool(context, AppConstants.PROCESS_SKU_PARENT_TRIGGERED, true)
            &&
            !isActive
        ) {
            if (context.isInternetActive())
                GlobalScope.launch(Dispatchers.Default) {
                    isActive = true
                    context.captureEvent(Events.PROCESS_SKU_STARTED, HashMap())
                    startSkuProcessing()
                }
            else {
                isActive = false
                listener.onConnectionLost("Process Sku Stopped", ServerSyncTypes.PROCESS)
                Log.d(TAG, "uploadParent: connection lost")
            }
        } else {
            Log.d(TAG, "processSkuParent: running")
        }
    }

    private suspend fun startSkuProcessing() {
        do {
            val sku = shootDaoApp.getProcessAbleSku() ?: break

            Log.d(TAG, "startSkuProcessing: " + Gson().toJson(sku))

            if (connectionLost) {
                val count = shootDaoApp.getPendingSku()
                context.captureEvent(
                    Events.PROCESS_SKU_CONNECTION_CONNECTION_BREAK,
                    HashMap<String, Any?>()
                        .apply {
                            put("sku_remaining", count)
                        }
                )
                isActive = false
                listener.onConnectionLost("Process Sku Stopped", ServerSyncTypes.CREATE)
                break
            } else {
                val properties = HashMap<String, Any?>()
                    .apply {
                        put("project_id", sku.projectId)
                        put("sku_id", sku.skuId)
                        put("data", Gson().toJson(sku))
                    }

                context.captureEvent(
                    Events.SKU_SELECTED,
                    properties
                )

                if (retryCount > 4) {
                    //skip project
                    val skip = shootDaoApp.skipSku(
                        sku.uuid,
                        sku.toProcessAt.plus(sku.retryCount * sku.retryCount.getDelay())
                    )

                    context.captureEvent(
                        Events.SKU_SKIPPED,
                        properties.apply {
                            put("db_count", skip)
                        }
                    )
                    retryCount = 0
                    continue
                }

                //in progress listener
                listener.inProgress("Processing Sku ${sku.skuName}", ServerSyncTypes.PROCESS)
                isActive = true


                if (sku.totalFramesUpdated) {
                    processSku(sku)
                } else {
                    //update total frames
                    if (sku.totalFrames != null && sku.initialFrames != null) {
                        if (sku.totalFrames!! > sku.initialFrames!!) {
                            val isTotalFramesUpdated = updateTotalFrames(sku)

                            if (!isTotalFramesUpdated)
                                continue

                            processSku(sku)
                            continue
                        } else {
                            processSku(sku)
                            continue
                        }
                    } else {
                        processSku(sku)
                        continue
                    }
                }
            }
        } while (sku != null)

        if (!connectionLost) {
            listener.onCompleted("All Skus Background Id Updated", ServerSyncTypes.PROCESS)
            isActive = false
        }
    }

    private suspend fun updateTotalFrames(skuApp: Sku): Boolean {
        Log.d(TAG, "updateTotalFrames: " + skuApp.totalFrames)
        val properties = HashMap<String, Any?>()
            .apply {
                put("project_id", skuApp.projectId)
                put("sku_id", skuApp.skuId)
                put("data", Gson().toJson(skuApp))
            }

        val response = ShootRepository().updateTotalFrames(
            skuApp.skuId!!,
            skuApp.totalFrames.toString(),
            Utilities.getPreference(context, AppConstants.AUTH_KEY)!!
        )

        context.captureEvent(
            Events.UPDATE_TOTAL_FRAMES_INITIATED,
            properties
        )

        if (response is Resource.Failure) {
            context.captureEvent(
                Events.UPDATE_TOTAL_FRAMES_FAILED,
                properties.apply {
                    put("response", response)
                    put("throwable", response.throwable)
                }
            )
            retryCount++
            return false
        }

        context.captureEvent(
            Events.SKU_TOTAL_FRAMES_UPDATED,
            properties.apply {
                put("response", response)
            }
        )

        //update total frames updated
        skuApp.totalFramesUpdated = true
        val updateCount = shootDaoApp.updateSku(skuApp)

        context.captureEvent(
            Events.SKU_TOTAL_FRAMES_IN_DB_UPDATED,
            properties.apply {
                put("response", response)
                put("db_count", updateCount)
            }
        )

        return true
    }

    private suspend fun processSku(skuApp: Sku): Boolean {
        val properties = HashMap<String, Any?>()
            .apply {
                put("project_id", skuApp.projectId)
                put("sku_id", skuApp.skuId)
                put("data", Gson().toJson(skuApp))
            }

        if (skuApp.processDataMap == null)
            skuApp.processDataMap = HashMap()

        val response = ProcessRepository().processSku(
            skuApp.processDataMap?.apply {
                put("skuId", skuApp.skuId.toString())
                put("prodSubCatId", skuApp.subcategoryId.toString())
                put("backgroundId", skuApp.backgroundId.toString())
                put("marketPlace_id", skuApp.marketplaceId.toString())
                put("totalFramesNo", skuApp.totalFrames.toString())
                put("is360", true)
                put("auth_key", Utilities.getPreference(context, AppConstants.AUTH_KEY).toString())
                put("videoPresent", skuApp.videoPresent)
                put("tintWindow", Utilities.getBool(context, AppConstants.TINT_WINDOW))
                put(
                    "tintType",
                    if (Utilities.getPreference(context, AppConstants.TINT_TYPE)
                            .isNullOrEmpty()
                    ) "medium" else Utilities.getPreference(context, AppConstants.TINT_TYPE)
                        .toString()
                )
            }!!
        )


        context.captureEvent(
            Events.PROCESS_SKU_INTIATED,
            properties
        )

        if (response is Resource.Failure) {
            context.captureEvent(
                Events.PROCESS_SKU_FAILED,
                properties.apply {
                    put("response", response)
                    put("throwable", response.throwable)
                }
            )
            retryCount++
            return false
        }

        context.captureEvent(
            Events.SKU_PROCESSED,
            properties.apply {
                put("response", response)
            }
        )

        //update sku processed
        skuApp.isProcessed = true
        val updateCount = shootDaoApp.updateSku(skuApp)

        context.captureEvent(
            Events.SKU_PROCESSED_IN_DB_UPDATED,
            properties.apply {
                put("response", response)
                put("db_count", updateCount)
            }
        )


        retryCount = 0
        return true
    }
}