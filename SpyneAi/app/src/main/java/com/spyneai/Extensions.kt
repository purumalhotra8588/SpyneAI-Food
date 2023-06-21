package com.spyneai

import GlideBlurTransformation
import RotateTransformation
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.camera.core.AspectRatio
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.spyneai.app.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboardingv2.ui.onboarding.SignInV3Activity
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.service.*
import com.spyneai.shootapp.data.ImagesRepoV2
import com.spyneai.shootapp.ui.dialogs.ConfirmTagsDialog
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


var TAG = "Locale_Check"

fun Context.gotoHome() {
    val intent = Intent(this, MainDashboardActivity::class.java)

    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}



fun getImageCategory(catId: String): String {
    return AppConstants.imageCatNameMap[catId]!!
}

fun Context.gotoHomeSignUp() {
    val intent = Intent(this, MainDashboardActivity::class.java)

    startActivity(intent)
}

fun Context.gotoLogin() {
    this.captureEvent(Events.LOG_OUT, HashMap<String, Any?>())

    Utilities.savePrefrence(this, AppConstants.TOKEN_ID, "")
    Utilities.savePrefrence(this, AppConstants.AUTH_KEY, "")
    Utilities.savePrefrence(this, AppConstants.PROJECT_ID, "")
    Utilities.savePrefrence(this, AppConstants.SHOOT_ID, "")
    Utilities.savePrefrence(this, AppConstants.SKU_ID, "")
    Intent.FLAG_ACTIVITY_CLEAR_TASK
    val intent = Intent(this, SignInV3Activity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}


fun Context.isValidGlideContext() = this !is Activity || (!this.isDestroyed && !this.isFinishing)

fun ImageButton.toggleButton(
    flag: Boolean, rotationAngle: Float, @DrawableRes firstIcon: Int, @DrawableRes secondIcon: Int,
    action: (Boolean) -> Unit
) {
    if (flag) {
        if (rotationY == 0f) rotationY = rotationAngle
        animate().rotationY(0f).apply {
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    action(!flag)
                }
            })
        }.duration = 200
        GlobalScope.launch(Dispatchers.Main) {
            delay(100)
            setImageResource(firstIcon)
        }
    } else {
        if (rotationY == rotationAngle) rotationY = 0f
        animate().rotationY(rotationAngle).apply {
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    action(!flag)
                }
            })
        }.duration = 200
        GlobalScope.launch(Dispatchers.Main) {
            delay(100)
            setImageResource(secondIcon)
        }
    }
}

fun Long.toDate(): String {
    val sdf = SimpleDateFormat("dd MMM, yyyy")
    val netDate = Date(this)
    return sdf.format(netDate)
}

fun Context.isMyServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun Context.isInternetActive(): Boolean {
    val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    } else {
        return connectivityManager.activeNetworkInfo?.isConnected ?: false
    }
}

fun Context.getInternetSpeed(): String {
    val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

    var downSpeed = "version below 23"
    var upSpeed = "version below 23"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val nc = cm.getNetworkCapabilities(cm.activeNetwork)
        downSpeed = (nc?.linkDownstreamBandwidthKbps?.div(1024)).toString() + " Mbps"
        upSpeed = (nc?.linkUpstreamBandwidthKbps?.div(1024)).toString() + " Mbps"
    }

    return JSONObject()
        .apply {
            put("is_active", isInternetActive())
            put("upload_speed", upSpeed)
            put("download_speed", downSpeed)
        }.toString()
}

fun Context.getBatteryLevel(): String {
    val bm = applicationContext.getSystemService(BATTERY_SERVICE) as BatteryManager
    val batLevel: Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    return "$batLevel%"
}

fun Context.getPowerSaveMode(): Boolean {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    val powerSaveMode = powerManager.isPowerSaveMode
    return powerSaveMode
}

fun Context.getNetworkName(): String {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val wifi: NetworkInfo? = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
    val mobile: NetworkInfo? = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

    var type = "None"

    if (wifi != null && wifi.isConnected)
        type = "Wi-Fi"

    if (mobile != null && mobile.isConnected)
        type = "Mobile"

    return type
}

fun getRequestHeaderData(): JSONObject {
    val headerData = JSONObject()

    headerData.put(
        "device_manufacturer", Utilities.getPreference(
            BaseApplication.getContext(), AppConstants.DEVICE_MANUFACTURER
        )
    )

    headerData.put(
        "model", Utilities.getPreference(
            BaseApplication.getContext(), AppConstants.MODEL
        )
    )

    headerData.put(
        "os_version", Utilities.getPreference(
            BaseApplication.getContext(), AppConstants.OS_VERSION
        )
    )

    headerData.put(
        "app_version", Utilities.getPreference(
            BaseApplication.getContext(), AppConstants.APP_VERSION
        )
    )

    headerData.put(
        "app_version_code", Utilities.getPreference(
            BaseApplication.getContext(), AppConstants.APP_VERSION_CODE
        )
    )


    headerData.put(
        "network_type", Utilities.getPreference(
            BaseApplication.getContext(), AppConstants.NETWORK_TYPE
        )
    )

    headerData.put(
        "device_id", Utilities.getPreference(
            BaseApplication.getContext(), AppConstants.DEVICE_ID
        )
    )

    return headerData
}

fun Context.isResolutionSupported(): Boolean {
    var resolutionSupported = false

    val resList = getResolutionList()

    if (resList != null) {
        resList?.forEach { it ->
            if (!resolutionSupported && it != null) {
                if (it.width == 1024 && it.height == 768)
                    resolutionSupported = true
            }
        }
    }

    return resolutionSupported
}

fun Context.getResolutionList(): Array<out Size>? {
    val cm = getSystemService(Context.CAMERA_SERVICE) as CameraManager

    return if (cm.cameraIdList != null && cm.cameraIdList.size > 1) {
        val characteristics: CameraCharacteristics =
            cm.getCameraCharacteristics("1")

        val configs = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )

        configs?.getOutputSizes(ImageFormat.JPEG)
    } else {
        null
    }
}

fun Context.getBestResolution(): Size? {
    if (isResolutionSupported())
        return Size(1024, 768)
    else {
        val resList = getResolutionList()

        if (resList == null)
            return null
        else {
            val fourByThreeList = ArrayList<Size>()

            resList.forEach {
                Log.d(
                    "Extensions",
                    "getBestResolution: " + it.width.toFloat().div(it.height.toFloat())
                )
                if (it.width.toFloat().div(it.height.toFloat()) == 1.3333334f
                    || it.width.toFloat().div(it.height.toFloat()) == 1.3333333f
                ) {
                    fourByThreeList.add(it)
                }
            }

            if (fourByThreeList.isEmpty())
                return null
            else {
                var max = fourByThreeList[0]

                fourByThreeList.forEach {
                    if (it.width > max.width && it.height > max.height)
                        max = it
                }

                return max
            }
        }
    }
}





fun Context.setLocale() {
    val locale = Locale(Utilities.getPreference(this, AppConstants.LOCALE))
//    Log.d(TAG, "setLocale:"+locale)
    Locale.setDefault(locale)
    val config = Configuration()
    config.locale = locale
    resources.updateConfiguration(config, resources.displayMetrics)
}

fun Context.isMagnatoMeterAvailable(): Boolean {
    val mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

    val mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val magneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    return mAccelerometer != null && magneticField != null
}

fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}


fun Context.loadSmartly(path: String?, imageView: ImageView) {
    val thumbnailSize = 0.75 // Adjust this value to set the desired thumbnail size
    path?.let {
        if (path.contains("http") || path.contains("https")) {

            Glide.with(this)
                .load(path)
                .override((thumbnailSize * imageView.width).toInt(), (thumbnailSize * imageView.height).toInt())
                .transition(DrawableTransitionOptions.withCrossFade())
                //.thumbnail(Glide.with(this).load(R.drawable.placeholder_gif))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView)
        }
        else {
            Glide.with(this)
                .load(path)
                .thumbnail(Glide.with(this).load(R.drawable.placeholder_gif))
                .transform(RotateTransformation( 90f))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView)
        }


    }
}

fun Context.loadThumbnail(path: String?, imageView: ImageView,width: Int, height: Int) {
    path?.let {
        if (path.contains("http") || path.contains("https")) {
            Picasso.get()
                .load(path)
                .resize(width,height)
                .noFade()
                .into(imageView)
        }
        else {
            Glide.with(this)
                .load(path)
                .thumbnail(Glide.with(this).load(R.drawable.placeholder_gif))
                .transform(RotateTransformation( 90f))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView)
        }
    }
}

fun Context.loadSmartlyWithCache(path: String?, imageView: ImageView) {
    path?.let {
        if (path.contains("http") || path.contains("https")
            || path.contains("Exterior")|| path.contains("Interior")
            || path.contains("Miscellaneous") || Utilities.getPreference(this,AppConstants.SELECTED_CATEGORY_ID)==AppConstants.CARS_CATEGORY_ID) {
            Glide.with(this)
                .load(path)
                .thumbnail(Glide.with(this).load(R.drawable.placeholder_gif))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .skipMemoryCache(false)
                .into(imageView)
        }
        else {
            Glide.with(this)
                .load(path)
                .thumbnail(Glide.with(this).load(R.drawable.placeholder_gif))
                .transform(RotateTransformation( 90f))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .skipMemoryCache(false)
                .into(imageView)
        }


    }
}

fun View.loadSmartly(path : String?,imageView : ImageView){
    path?.let {
        if (path.contains("http") || path.contains("https")
            || path.contains("Exterior")|| path.contains("Interior")
            || path.contains("Miscellaneous") || Utilities.getPreference(context,AppConstants.SELECTED_CATEGORY_ID)==AppConstants.CARS_CATEGORY_ID){
            Glide.with(this)
                .load(path)
                .thumbnail(Glide.with(context).load(R.drawable.placeholder_gif))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView)
        }
        else {
            Glide.with(this)
                .load(path)
                .thumbnail(Glide.with(context).load(R.drawable.placeholder_gif))
                .transform(RotateTransformation( 90f))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView)
        }
    }
}

fun Context.loadSmartlyThumbnail(path : String?,imageView : ImageView){
    path?.let {
        if (path.contains("http") || path.contains("https") ||
            path.contains("Exterior")|| path.contains("Interior")
            || path.contains("Miscellaneous") || Utilities.getPreference(this,AppConstants.SELECTED_CATEGORY_ID)==AppConstants.CARS_CATEGORY_ID){
            Glide.with(this)
                .load(path)
                .transform(GlideBlurTransformation(this))
                .into(imageView)
        }
        else {
            Glide.with(this)
                .load(path)
                .transform(RotateTransformation( 90f),GlideBlurTransformation(this))
                .into(imageView)
        }
    }
}

fun getUuid() = UUID.randomUUID().toString().replace("-", "")

fun Context.startUploadingService(startedBy: String, syncTypes: ServerSyncTypes) {
    try {
        val prperties = HashMap<String, Any?>()
            .apply {
                put(
                    "email",
                    Utilities.getPreference(this@startUploadingService, AppConstants.EMAIL_ID)
                        .toString()
                )
                put("medium", startedBy)
            }

        var action = Actions.START
        if (getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP)
            return

        val serviceIntent = Intent(this, DataSyncService::class.java)
        serviceIntent.putExtra(AppConstants.SERVICE_STARTED_BY, startedBy)
        serviceIntent.putExtra(AppConstants.SYNC_TYPE, syncTypes)
        serviceIntent.action = action.name

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            log("Starting the service in >=26 Mode")
            ContextCompat.startForegroundService(this, serviceIntent)
            return
        } else {
            log("Starting the service in < 26 Mode")
            this.startService(serviceIntent)
        }

        prperties.put("state", "Started")
        this.captureEvent(Events.SERVICE_STARTED, prperties)
    }catch (e: Exception){
        this.captureEvent(Events.SERVICE_STARTED, HashMap<String, Any?>().apply {
            put("medium",startedBy)
            put("type",syncTypes)
            put("msg",e.localizedMessage)
            put("cause",e.cause)
        })
    }
}


fun Context.checkPendingDataSync(startedBy: String?= null) {
    GlobalScope.launch(Dispatchers.IO) {
        try {
            val db = SpyneAppDatabase.getInstance(BaseApplication.getContext())
            val imageDao = db.imageDao()

            val properties = HashMap<String, Any?>()
            properties["email"] =
                Utilities.getPreference(this@checkPendingDataSync, AppConstants.USER_EMAIL).toString()

            val image = ImagesRepoV2(imageDao).getOldestImage()

            captureEvent(
                Events.OLDEST_IMAGE,
                properties.apply {
                    put("data", Gson().toJson(image))
                }
            )

            if (image != null
            ) {
                startUploadingService(
                    startedBy ?: MainDashboardActivity::class.java.simpleName,
                    ServerSyncTypes.UPLOAD
                )
            }


            val pendingProjects = db.projectDao().getPendingProjects()

            captureEvent(
                Events.CREATE_PROJECT_PENDING,
                properties.apply {
                    put("project_pending", pendingProjects)
                }
            )

            if (pendingProjects > 0) {
                startUploadingService(
                    MainDashboardActivity::class.java.simpleName,
                    ServerSyncTypes.CREATE
                )
            }

            val pendingSkus = db.skuDao().getPendingSku()

            captureEvent(
                Events.PROCESS_SKU_PENDING,
                properties.apply {
                    put("sku_pending", pendingSkus)
                }
            )

            if (pendingSkus > 0) {
                startUploadingService(
                    MainDashboardActivity::class.java.simpleName,
                    ServerSyncTypes.PROCESS
                )
            }

            val pendingProjectSubmission = db.projectDao().getSubmissionPendingProjects()

            if (pendingProjectSubmission > 0){
                startUploadingService(
                    MainDashboardActivity::class.java.simpleName,
                    ServerSyncTypes.SUBMIT_PROJECT
                )
            }
        }catch (e: Exception){

        }
    }
}

fun Long.toDateFormat(): String {
    val sdf = SimpleDateFormat("dd/MM/yy hh:mm:ss a")
    val netDate = Date(this)
    return sdf.format(netDate)
}

fun Context.allDataSynced(): Boolean {
    var allDataSynced = true

    //check projects
    val db = SpyneAppDatabase.getInstance(this)

    val pendingProjects = db.projectDao().getPendingProjects()
    if (pendingProjects > 0)
        return false

    val pendingSkus = db.skuDao().getPendingSku()

    if (pendingSkus > 0)
        return false

    val pendingImages = db.imageDao().totalRemainingUpload(isUploaded = false, isMarkedDone = false)

    if (pendingImages > 0)
        return false

    val pendingProjectSubmission = db.projectDao().getSubmissionPendingProjects()

    if (pendingProjectSubmission > 0)
        return false

    return allDataSynced
}


fun getFormattedDate(time: Long): String {
    val cal = Calendar.getInstance()
    val tz = cal.timeZone
    Log.d(TAG, "getFormattedDate: "+tz.displayName)
    val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm:ss")

    return formatter.format(Date(time))
}

fun getTimeStamp(date: String): Long {
    val cal = Calendar.getInstance()
    val tz = cal.timeZone

    Log.d(TAG, "getTimeStamp: "+tz.displayName)
    val nd = date.substringBefore("T") + " " + date.substringAfter("T").substringBefore(".")

    val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    //formatter.timeZone = tz

    val date = formatter.parse(nd) as Date
    Log.d(TAG, "getTimeStamp: "+date.time)
    val addedTime = 330
    val gmtMilis = addedTime.times(60).times(1000)

    return if (tz.displayName == "GMT+05:30" || tz.displayName == "India Standard Time")
        date.time.plus(gmtMilis)
    else
        date.time
}

fun Context.showConnectionChangeView(isConnected: Boolean, parent: View) {
    val layoutInflater = LayoutInflater.from(this)
    val snackbar = Snackbar.make(parent, "", Snackbar.LENGTH_LONG)

    // inflate the cutom_snackbar_view created previously
    val customSnackView =
        if (isConnected) layoutInflater.inflate(R.layout.online_snackbar_view, null) else
            layoutInflater.inflate(R.layout.offline_snackbar_view, null);

    // set the background of the default snackbar as transparent
    snackbar.view.setBackgroundColor(Color.TRANSPARENT)

    // now change the layout of the snackbar
    val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout

    // set padding of the all corners as 0
    snackbarLayout.setPadding(0, 0, 0, 0)
    snackbarLayout.layoutParams.height = pxFromDp(this, 25F).toInt()

    snackbarLayout.addView(customSnackView, 0)

    snackbar.show()
}

fun pxFromDp(context: Context, dp: Float): Float {
    return dp * context.resources.displayMetrics.density
}


fun Context.getNotificationText(contentType: Int, id: Int): String? {
    val type = if (contentType == 0) "android.text" else "android.title"
    var content: String? = ""
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val barNotifications = notificationManager.activeNotifications
        for (notification in barNotifications) {
            if (notification.id == id) {
                notification.notification.extras?.let {
                    content = it.getString(type)
                }

            }
        }
    }

    return content
}

fun Context.startUploadServiceWithCheck() {
    if (!isMyServiceRunning(DataSyncService::class.java))
        startUpload()
    else {
        val content = getNotificationText(1, 100)
        content?.let {
            if (it.contains("Uploaded")) {
                var action = Actions.STOP
                val serviceIntent = Intent(this, DataSyncService::class.java)
                serviceIntent.putExtra(
                    AppConstants.SERVICE_STARTED_BY,
                    ConfirmTagsDialog::class.simpleName
                )
                serviceIntent.action = action.name

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(this, serviceIntent)
                    return
                } else {
                    startService(serviceIntent)
                }

                startUpload()
            }
        }
    }
}

fun Context.startUpload() {
    var action = Actions.START
    if (getServiceState(this) == com.spyneai.service.ServiceState.STOPPED && action == Actions.STOP)
        return

    val serviceIntent = Intent(this, DataSyncService::class.java)
    serviceIntent.putExtra(AppConstants.SERVICE_STARTED_BY, ConfirmTagsDialog::class.simpleName)
    serviceIntent.action = action.name

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        log("Starting the service in >=26 Mode")
        ContextCompat.startForegroundService(this, serviceIntent)
        return
    } else {
        log("Starting the service in < 26 Mode")
        startService(serviceIntent)
    }
}

fun aspectRatio(width: Int, height: Int,appName: String): Int {
    val previewRatio = max(width, height).toDouble() / min(width, height)
    if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
        return AspectRatio.RATIO_4_3
    }
    if (appName == "Swiggy" ||
        appName == AppConstants.KARVI
    )
        return AspectRatio.RATIO_4_3

    return AspectRatio.RATIO_16_9
}

private const val RATIO_4_3_VALUE = 4.0 / 3.0 // aspect ratio 4x3
private const val RATIO_16_9_VALUE = 16.0 / 9.0 // aspect ratio 16x9




fun Fragment.handleFirstPageError(loadState: CombinedLoadStates, retry: () -> Unit): Boolean {
    val error = when {
        loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
        loadState.append is LoadState.Error -> loadState.append as LoadState.Error
        loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
        else -> null
    }

    error?.let {
        handleApiError(Resource.Failure(false, errorCode = error.hashCode(), error.error.message)) {
            retry()
        }
        return true
    }

    return false
}

fun Int.getDelay() = if (this <= 5) 10000 else 60000



