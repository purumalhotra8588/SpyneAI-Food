package com.spyneai.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import com.google.gson.Gson
import com.spyneai.*
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.posthog.captureFailureEvent
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.shootapp.repository.model.image.Image
import com.spyneai.shootapp.data.ImagesRepoV2
import com.spyneai.shootapp.data.ShootRepository
import com.spyneai.shootapp.utils.objectToString
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.*
import java.util.*


class ImageUploader(
    val context: Context,
    val localRepository: ImagesRepoV2,
    val shootRepository: ShootRepository,
    var listener: DataSyncListener,
    var lastIdentifier: String = "0",
    var imageType: String = AppConstants.REGULAR,
    var retryCount: Int = 0,
    var connectionLost: Boolean = false,
    var isActive: Boolean = false
) {

    companion object {
        @Volatile
        private var INSTANCE: ImageUploader? = null

        fun getInstance(context: Context, listener: DataSyncListener): ImageUploader {
            synchronized(this) {
                var instance = ImageUploader.INSTANCE

                if (instance == null) {
                    instance = ImageUploader(
                        context,
                        ImagesRepoV2(
                            SpyneAppDatabase.getInstance(BaseApplication.getContext()).imageDao()
                        ),
                        ShootRepository(),
                        listener
                    )

                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    val TAG = "ImageUploader"
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    fun uploadParent(type: String, startedBy: String?) {
        context.captureEvent("UPLOAD PARENT TRIGGERED", HashMap<String, Any?>().apply {
            put("type", type)
            put("service_started_by", startedBy)
            put("upload_running", isActive)
        })

        //update triggered value
        Utilities.saveBool(context, AppConstants.UPLOAD_TRIGGERED, true)

        if (Utilities.getBool(context, AppConstants.UPLOAD_TRIGGERED, true) && !isActive) {
            if (context.isInternetActive())
                scope.launch {
                    this@ImageUploader.isActive = true
                    context.captureEvent("START UPLOADING CALLED", HashMap())
                    startUploading()
                }
            else {
                this@ImageUploader.isActive = false
                listener.onConnectionLost("Image uploading paused", ServerSyncTypes.UPLOAD)
            }
        }
    }

    suspend fun startUploading() {
        do {
            lastIdentifier = getUniqueIdentifier()

            var image = localRepository.getOldestImage()

            if (connectionLost) {
                context.captureEvent(
                    AppConstants.CONNECTION_BREAK,
                    HashMap<String, Any?>()
                        .apply {
                            put("remaining_images", JSONObject().apply {
                                put("upload_remaining", localRepository.totalRemainingUpload())
                                put("mark_done_remaining", localRepository.totalRemainingMarkDone())
                            }.toString())
                        }
                )
                this@ImageUploader.isActive = false
                val text =
                    if (image == null) "Image uploading paused" else "Image uploading paused at ${image.skuName?.uppercase()} (${image.image_category}-${image.sequence})"
                listener.onConnectionLost(text, ServerSyncTypes.UPLOAD)
                break
            }


            if (image == null) {
                context.captureEvent(
                    AppConstants.ALL_UPLOADED_BREAK,
                    HashMap<String, Any?>()
                        .apply {
                            put("remaining_images", JSONObject().apply {
                                put("upload_remaining", localRepository.totalRemainingUpload())
                                put("mark_done_remaining", localRepository.totalRemainingMarkDone())
                            }.toString())
                        }
                )
                this@ImageUploader.isActive = false
                break
            } else {
                lastIdentifier = image.name + "_" + image.skuId

                val imageProperties = HashMap<String, Any?>()
                    .apply {
                        put("sku_id", image.skuId)
                        put("iteration_id", lastIdentifier)
                        put("retry_count", retryCount)
                        put("upload_type", imageType)
                        put("data", Gson().toJson(image))
                        put("remaining_images", JSONObject().apply {
                            put("upload_remaining", localRepository.totalRemainingUpload())
                            put("mark_done_remaining", localRepository.totalRemainingMarkDone())
                            put("remaining_above", localRepository.getRemainingAbove(image.uuid!!))
                            put(
                                "remaining_above_skipped",
                                localRepository.getRemainingAboveSkipped(image.uuid!!)
                            )
                            put("remaining_below", localRepository.getRemainingBelow(image.uuid!!))
                            put(
                                "remaining_below_skipped",
                                localRepository.getRemainingBelowSkipped(image.uuid!!)
                            )
                        }.toString())

                    }

                context.captureEvent(
                    AppConstants.IMAGE_SELECTED,
                    imageProperties
                )

                listener.inProgress(
                    "Uploading ${image.skuName?.uppercase()} (${image.image_category}-${image.sequence})",
                    ServerSyncTypes.UPLOAD
                )
                this@ImageUploader.isActive = true

                val retryLimit = if (image.isUploaded) 1 else 4

                if (retryCount > retryLimit) {
                    val delay = image.retryCount.getDelay()

                    val skip = localRepository.skipImage(
                        image.uuid,
                        image.toProcessAT.plus(image.retryCount * delay)
                    )

                    captureEvent(
                        Events.MAX_RETRY,
                        image,
                        false,
                        "Image upload limit reached",
                        skip
                    )
                    retryCount = 0
                    continue
                }

                if (!image.isUploaded) {
                    if (image.preSignedUrl != AppConstants.DEFAULT_PRESIGNED_URL) {
                        when (image.image_category) {
                            "Exterior",
                            "Interior",
                            "Focus Shoot",
                            "360int",
                            "Info" -> {
                                val imageUploaded = uploadImage(image)

                                if (!imageUploaded)
                                    continue

                                markDoneImage(image)
                                continue
                            }
                            else -> {

                                try {
                                    val bitmap =
                                        modifyOrientation(
                                            BitmapFactory.decodeFile(image?.path),
                                            image.path
                                        )

                                    File(outputDirectory).mkdirs()
                                    val outputFile = File(
                                        outputDirectory + System.currentTimeMillis()
                                            .toString() + ".jpg"
                                    )
                                    outputFile.createNewFile()

                                    val os: OutputStream = BufferedOutputStream(
                                        FileOutputStream(outputFile)
                                    )
                                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, os)
                                    os.close()

                                    image.path = outputFile.path
                                    val imageUploaded = uploadImage(image)

                                    if (!imageUploaded)
                                        continue

                                    val imageMarkedDone = markDoneImage(image)
                                    continue
                                } catch (
                                    e: Exception
                                ) {
                                    captureEvent(
                                        Events.IMAGE_ROTATION_EXCEPTION,
                                        image,
                                        false,
                                        e.localizedMessage
                                    )
                                    retryCount++
                                    continue
                                }
                            }
                        }
                    } else {
                        val uploadType = if (retryCount == 1) "Direct" else "Retry"
                        image.tags =
                            if (image.tags.isNullOrEmpty()) JSONObject().toString() else JSONObject(
                                image.tags
                            ).toString()
//                        image.debugData =
//                            if (image.debugData.isNullOrEmpty()) JSONObject().toString() else JSONObject(
//                                image.debugData
//                            ).toString()

                        val gotPresigned = getPresigned(image, uploadType)

                        if (!gotPresigned)
                            continue

                        when (image.image_category) {
                            "Exterior",
                            "Interior",
                            "Focus Shoot",
                            "360int",
                            "Info" -> {
                                val imageUploaded = uploadImage(image)

                                if (!imageUploaded)
                                    continue

                                markDoneImage(image)
                                continue
                            }
                            else -> {
                                val bitmap =
                                    modifyOrientation(
                                        BitmapFactory.decodeFile(image.path),
                                        image.path
                                    )

                                try {
                                    File(outputDirectory).mkdirs()
                                    val outputFile = File(
                                        outputDirectory + System.currentTimeMillis()
                                            .toString() + ".jpg"
                                    )
                                    outputFile.createNewFile()

                                    val os: OutputStream = BufferedOutputStream(
                                        FileOutputStream(outputFile)
                                    )
                                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, os)
                                    os.close()

                                    image.path = outputFile.path

                                    val imageUploaded = uploadImage(image)

                                    if (!imageUploaded)
                                        continue

                                    val imageMarkedDone = markDoneImage(image)
                                    continue

                                } catch (
                                    e: Exception
                                ) {
                                    captureEvent(
                                        Events.IMAGE_ROTATION_EXCEPTION,
                                        image,
                                        false,
                                        e.localizedMessage
                                    )
                                    retryCount++
                                    continue
                                }
                            }
                        }
                    }
                } else {
                    if (image.imageId == null) {
                        captureEvent(
                            AppConstants.IMAGE_ID_NULL,
                            image,
                            true,
                            null
                        )
                        localRepository.markDone(image.uuid)
                        retryCount = 0
                        continue
                    } else {
                        val imageMarkedDone = markDoneImage(image)
                        if (imageMarkedDone)
                            retryCount = 0

                        continue

                    }
                }
            }
        } while (image != null)

        //upload images clicked while service uploading skipped images
        if (!connectionLost) {
            deleteTempFiles(File(outputDirectory))
            listener.onCompleted("All images uploaded", ServerSyncTypes.UPLOAD)
            this@ImageUploader.isActive = false
        }
    }

    private suspend fun getPresigned(Image: Image, uploadType: String): Boolean {
        Image.name = if (Image.image_category == "360int")
            Image.skuName?.uppercase() + "_" + Image.skuId + "_360int_1.JPG"
        else {
            val imageNameArray = Image.name.split("_").toMutableList()
            imageNameArray[1] = Image.skuId.toString()
            imageNameArray[0] + "_" + imageNameArray[1] + "_" + imageNameArray[2] + "_" + imageNameArray[3]
        }
        var response = shootRepository.getPreSignedUrl(
            uploadType,
            Image
        )

        captureEvent(
            Events.GET_PRESIGNED_CALL_INITIATED, Image, true, null,
            retryCount = retryCount
        )

        when (response) {
            is Resource.Failure -> {
                captureEvent(
                    Events.GET_PRESIGNED_FAILED,
                    Image,
                    false,
                    getErrorMessage(response),
                    response = Gson().toJson(response).toString(),
                    retryCount = retryCount,
                    throwable = response.throwable
                )

                retryCount++
                return false
            }
            else -> {

            }
        }

        val imagePreSignedRes = (response as Resource.Success).value

        Image.preSignedUrl = imagePreSignedRes.data.presignedUrl
        Image.imageId = imagePreSignedRes.data.imageId

        captureEvent(
            Events.GOT_PRESIGNED_IMAGE_URL, Image,
            true,
            null,
            response = Gson().toJson(response.value).toString(),
            retryCount = retryCount
        )

        val count = localRepository.addPreSignedUrl(Image)

        captureEvent(
            Events.IS_PRESIGNED_URL_UPDATED,
            localRepository.getImage(Image.uuid!!),
            true,
            null,
            count,
            retryCount = retryCount
        )

        return true
    }

    private suspend fun uploadImage(Image: Image): Boolean {
        val requestFile = File(Image.path)

        if (!requestFile.exists() || Image.path.isNullOrEmpty()) {
            val markUploadCount = localRepository.markUploaded(Image.uuid)
            val count = localRepository.markStatusUploaded(Image.uuid)

            context.captureEvent(Events.IMAGE_DELETED, HashMap<String, Any?>().apply {
                put("sku_id", Image.skuId)
                put("project_id", Image.projectId)
                put("uuid", Image.skuUuid)
                put("data", Image.objectToString())
                put("image_exits", requestFile.exists())
                put("path", Image.path)
                put("marked_uploaded", markUploadCount)
                put("marked_done", count)
            })

            return false
        }

        var compressedImageFile: File? = null
        try {
            when (Image.image_category) {
                "Exterior",
                "Interior",
                "Focus Shoot",
                "360int",
                "Info" -> {
                    File("/storage/emulated/0/DCIM/SpyneC/").mkdirs()

                    val dir = File("/storage/emulated/0/DCIM/SpyneC/")

                    if (dir.isDirectory) {
                        val outputFile = File(
                            "/storage/emulated/0/DCIM/SpyneC/" + System.currentTimeMillis()
                                .toString() + ".jpg"
                        )

                        if (outputFile.exists())
                            outputFile.delete()

                        outputFile.createNewFile()

                        compressedImageFile =
                            Compressor.compress(BaseApplication.getContext(), requestFile) {
                                quality(80)
                                destination(outputFile)
                            }
                    }
                }
                else -> {
                    compressedImageFile = Compressor.compress(context, requestFile) {
                        resolution(2300, 2300)
                        quality(90)
                    }
                }
            }

            context.captureEvent(
                Events.IMAGE_COMPRESSED,
                HashMap<String, Any?>()
                    .apply {
                        put("sku_id", Image.skuId)
                        put("requestFile", requestFile)
                        put("compressedFile", compressedImageFile)
                        put("image", Image)
                    }
            )

        } catch (e: Exception) {
            if (e.localizedMessage.contains("The source file doesn't exist.")) {
                val imageMarkDone = localRepository.markStatusUploaded(Image.uuid)
                val markUploadCount = localRepository.markUploaded(Image.uuid)

                Log.d(TAG, "Compressing Image:imageMarkedDone $imageMarkDone $markUploadCount $e  ")
            }
            Events.IMAGE_COMPRESSED_EXCEPTION
            HashMap<String, Any?>()
                .apply {
                    put("sku_id", Image.skuId)
                    put("requestFile", requestFile)
                    put("compressedFile", compressedImageFile)
                    put("Exception", e)
                    put("image", Image)
                }
            e.printStackTrace()
            return false
        }

        if (compressedImageFile == null)
            return false

        val uploadResponse = shootRepository.uploadImageToGcp(
            Image.preSignedUrl,
            compressedImageFile.asRequestBody("text/x-markdown; charset=utf-8".toMediaTypeOrNull()),
            "application/octet-stream"
        )

        val imageProperties = HashMap<String, Any?>()
            .apply {
                put("sku_id", Image.skuId)
                put("iteration_id", lastIdentifier)
                put("upload_type", imageType)
                put("retry_count", retryCount)
                put("data", Gson().toJson(Image))
            }

        context.captureEvent(
            Events.UPLOADING_TO_GCP_INITIATED,
            imageProperties
        )

        when (uploadResponse) {
            is Resource.Failure -> {
                captureEvent(
                    Events.IMAGE_UPLOAD_TO_GCP_FAILED,
                    Image,
                    false,
                    getErrorMessage(uploadResponse),
                    response = Gson().toJson(uploadResponse).toString(),
                    retryCount = retryCount,
                    throwable = uploadResponse.throwable
                )
                retryCount++
                return false
            }
            else -> {

            }
        }

        captureEvent(
            Events.IMAGE_UPLOADED_TO_GCP, Image,
            true,
            null,
            response = Gson().toJson(uploadResponse).toString(),
            retryCount = retryCount
        )

        val markUploadCount = localRepository.markUploaded(Image.uuid)

        val uploadImageCount = localRepository.getUploadedCount(Image.skuUuid.toString())
        val totalImageCount = localRepository.getTotalImageCount(Image.skuUuid.toString())

        context.captureEvent(
            "Upload Count",
            HashMap<String, Any?>().apply {
                put("sku_id", Image.skuId)
                put("uploadImageCount", uploadImageCount)
                put("totalImageCount", totalImageCount)
            }
        )

        if (uploadImageCount == totalImageCount) {
            //make sku processable
            val isUpdated = SpyneAppDatabase.getInstance(BaseApplication.getContext()).skuDao()
                .makeSkuProcessAble(Image.skuUuid.toString())

            context.captureEvent(
                "Upload Count",
                HashMap<String, Any?>().apply {
                    put("sku_id", Image.skuId)
                    put("uploadImageCount", uploadImageCount)
                    put("totalImageCount", totalImageCount)
                    put("isUpdated", isUpdated)
                }
            )

            //start processing sync service
            BaseApplication.getContext().startUploadingService(
                ImageUploader::class.java.simpleName,
                ServerSyncTypes.PROCESS
            )
        }

        captureEvent(
            Events.IS_MARK_GCP_UPLOADED_UPDATED,
            localRepository.getImage(Image.uuid!!),
            true,
            null,
            markUploadCount,
            retryCount = retryCount
        )

        return true
    }

    private suspend fun markDoneImage(Image: Image): Boolean {
        val markUploadResponse =
            shootRepository.markUploaded(Image.imageId!!)

        captureEvent(
            Events.MARK_DONE_CALL_INITIATED, Image, true, null,
            retryCount = retryCount
        )

        when (markUploadResponse) {
            is Resource.Failure -> {
                captureEvent(
                    Events.MARK_IMAGE_UPLOADED_FAILED,
                    Image,
                    false,
                    getErrorMessage(markUploadResponse),
                    response = Gson().toJson(markUploadResponse).toString(),
                    retryCount = retryCount,
                    throwable = markUploadResponse.throwable
                )
                retryCount++
                return false
            }
            else -> {

            }
        }

        captureEvent(
            Events.MARKED_IMAGE_UPLOADED, Image, true,
            null,
            response = Gson().toJson(markUploadResponse).toString()
        )

        val count = localRepository.markStatusUploaded(Image.uuid)

        captureEvent(
            Events.IS_MARK_DONE_STATUS_UPDATED,
            localRepository.getImage(Image.uuid!!),
            true,
            null,
            count,
            retryCount = retryCount
        )
        retryCount = 0
        return true
    }

    private fun getErrorMessage(response: Resource.Failure): String {
        return if (response.errorMessage == null) response.errorCode.toString() + ": Http exception from server" else response.errorCode.toString() + ": " + response.errorMessage
    }


    @Throws(IOException::class)
    fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String?): Bitmap? {
        val ei = ExifInterface(image_absolute_path!!)
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_UNDEFINED,
            ExifInterface.ORIENTATION_NORMAL,
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(bitmap, true, false)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(bitmap, false, true)
            else -> bitmap
        }
    }

    fun rotate(bitmap: Bitmap, degrees: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap? {
        val matrix = Matrix()
        matrix.preScale(
            (if (horizontal) -1 else 1.toFloat()) as Float,
            (if (vertical) -1 else 1.toFloat()) as Float
        )
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun captureEvent(
        eventName: String,
        Image: Image,
        isSuccess: Boolean,
        error: String?,
        dbUpdateStatus: Int = 0,
        response: String? = null,
        retryCount: Int = 0,
        throwable: String? = null
    ) {
        val properties = HashMap<String, Any?>()
            .apply {
                put("sku_id", Image.skuId)
                put("iteration_id", lastIdentifier)
                put("db_update_status", dbUpdateStatus)
                put("data", Gson().toJson(Image))
                put("response", response)
                put("retry_count", retryCount)
                put("throwable", throwable)
            }

        if (isSuccess) {
            context.captureEvent(
                eventName,
                properties
            )
        } else {
            context.captureFailureEvent(
                eventName,
                properties, error!!
            )
        }
    }

    private fun deleteTempFiles(file: File): Boolean {
        if (file.isDirectory) {
            val files = file.listFiles()
            if (files != null) {
                for (f in files) {
                    if (f.isDirectory) {
                        deleteTempFiles(f)
                    } else {
                        f.delete()
                    }
                }
            }
        }
        return file.delete()
    }

    private fun getUniqueIdentifier(): String {
        val SALTCHARS = "abcdefghijklmnopqrstuvwxyz1234567890"
        val salt = StringBuilder()
        val rnd = Random()
        while (salt.length < 7) { // length of the random string.
            //val index = (rnd.nextFloat() * SALTCHARS.length) as Int
            val index = rnd.nextInt(SALTCHARS.length)
            salt.append(SALTCHARS[index])
        }
        return salt.toString()
    }


    val outputDirectory = "/storage/emulated/0/DCIM/Spynetemp/"

}