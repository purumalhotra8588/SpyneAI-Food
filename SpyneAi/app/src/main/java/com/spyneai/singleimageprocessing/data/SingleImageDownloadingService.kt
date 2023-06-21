package com.spyneai.singleimageprocessing.data

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.webkit.MimeTypeMap
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.extras.events.ProcessingImagesEvent
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.captureEvent
import com.spyneai.service.ServiceState
import com.spyneai.service.log
import com.spyneai.service.setServiceState
import org.greenrobot.eventbus.EventBus
import java.io.File

class SingleImageDownloadingService : Service(), SingleImageDownloadManager.Listener {
    private var wakeLock: PowerManager.WakeLock? = null

    var tasksInProgress = ArrayList<SingleDownloadTask>()
    lateinit var notificationManager: NotificationManager
    lateinit var channel: NotificationChannel
    lateinit var builder: Notification.Builder
    var TAG = "SIngleImageDownloadingService"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null)
            return START_STICKY

        val action = intent.action

        when (action) {
            "START" -> fetchDataAndStartService(intent)
            "STOP" -> stopService()
            else -> error("No action in the received intent")
        }

        return START_STICKY

    }

    private fun fetchDataAndStartService(intent: Intent) {
        val task = SingleDownloadTask()

        task.listHdQuality.addAll(intent.getParcelableArrayListExtra(AppConstants.LIST_HD_QUALITY)!!)
        task.imageNameList.addAll(intent.getParcelableArrayListExtra(AppConstants.LIST_IMAGE_NAME)!!)

        tasksInProgress.add(task)


        checkAndFinishService()

        SingleImageDownloadManager(task, this).start()
    }

    private fun checkAndFinishService() {
        //clear all notifications

        notificationManager.cancelAll()

        tasksInProgress.forEach {

            if (it.isCompleted) {
                createCompletedNotification(it.isHd)

            } else if (it.isFailure) {
                createFailureNotification(it.isHd)
            } else
                createOngoingNotificaiton(it.isHd)
        }
        if (tasksInProgress.filter { !it.isCompleted || !it.isFailure }.isEmpty()) {
            stopService()
        }
    }

    private fun createOngoingNotificaiton(hd: Boolean) {
        var notificationId = (0..999999).random()
        val text = if (hd)
            "HD image downloading in progress..."
        else
            "Watermark image downloading in progress..."

        var notification = createNotification(text, true)

        notificationManager.notify(notificationId, notification)
        startForeground(notificationId, notification)

    }

    private fun createCompletedNotification(hd: Boolean) {
        captureEvent("Download Completed", HashMap<String,Any?>())

        var notification = if (hd)
            createNotification("HD images downloaded! Check in your gallery", false)
        else
            createNotification("Watermark images downloaded! Check in your gallery", false)
        notificationManager.notify((0..999999).random(), notification)
    }

    private fun createFailureNotification(hd: Boolean) {
        captureEvent("Download Failed", HashMap<String,Any?>())

        var notification = if (hd) createNotification(
            "HD images downloading failed! please try again",
            false
        )else
            createNotification(
                "Watermark images downloading failed! please try again",
                false
            )

        notificationManager.notify((0..999999).random(), notification)
    }

    private fun createNotification(text: String, isOngoing: Boolean): Notification {
        val notificationChannelId = "HD IMAGE DOWNLOAD SERVICE CHANNEL"
        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(
                notificationChannelId,
                "HD Images Downloading Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "HD Images Downloading Images Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this,
                if(Utilities.getBool(this,AppConstants.NEW_ENTERPRISE_USER,false)) MainDashboardActivity::class.java
                else MainDashboardActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, getPendingIntentFlags(true))
            }


        builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
            ) else
                Notification.Builder(this)

        var notificationBuilder = builder
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.app_ic)
            .setOngoing(isOngoing)
            .setOnlyAlertOnce(true)
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility

        if(!isOngoing){
            notificationBuilder.setAutoCancel(true)
        }

        return notificationBuilder.build()
    }

    private fun getPendingIntentFlags(isMutable: Boolean = false) =
        when {
            isMutable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE

            !isMutable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            else -> PendingIntent.FLAG_UPDATE_CURRENT
        }

    override fun onCreate() {
        super.onCreate()
        setServiceState(this, ServiceState.STARTED)

        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ProcessService::lock").apply {
                acquire()
            }
        }

        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun stopService() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            var processingImageEvent = ProcessingImagesEvent();
            processingImageEvent.setShootStatus("fail")
            EventBus.getDefault().post(processingImageEvent)

            stopForeground(true)
            stopSelf()

        } catch (e: Exception) {
            log("Service stopped without being started: ${e.message}")
        }
//        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
    }

    override fun onSuccess(task: SingleDownloadTask) {
        task.isCompleted = true
        //notify to activity

        stopService()
        checkAndFinishService()
    }

    override fun onScan(filePath: String) {

        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }

        MediaScannerConnection.scanFile(
            BaseApplication.getContext(), arrayOf(filePath), arrayOf(type)
        ) { path, uri -> Log.i("TAG", "Finished scanning $path") }
    }

    override fun onRefresh(filePath: String) {
        Log.d(TAG, "onRefresh: " + filePath)
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        var file = File(filePath)
        val contentUri: Uri = Uri.fromFile(file)
        mediaScanIntent.data = contentUri
        sendBroadcast(mediaScanIntent)
    }

    override fun onFailure(task: SingleDownloadTask) {
        task.isFailure = true
        stopService()
        checkAndFinishService()
    }
}