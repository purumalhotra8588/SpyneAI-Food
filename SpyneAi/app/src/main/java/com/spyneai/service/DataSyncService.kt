package com.spyneai.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.*
import com.spyneai.R
import com.spyneai.allDataSynced
import com.spyneai.checkPendingDataSync
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.posthog.captureEvent
import com.spyneai.shootapp.repository.model.image.Image

import com.spyneai.shootapp.utils.objectToString


class DataSyncService : Service(), DataSyncListener {

    private var wakeLock: PowerManager.WakeLock? = null
    lateinit var notificationManager: NotificationManager
    lateinit var channel: NotificationChannel
    lateinit var builder: Notification.Builder
    private var receiver: InternetConnectionReceiver? = null
    private var imageUploader: ImageUploader? = null
    private var prjSync: ProjectSkuSync? = null
    private var processSkuSync: ProcessSkuSync? = null
    private var projectSubmitSync: ProjectSubmitSync? = null
    private var notificationId = 0
    val notificationChannelId = "PROCESSING SERVICE CHANNEL"
    var currentImageApp: Image? = null
    val TAG = "ImageUploader"
    var serviceStartedBy: String? = null
    var handler: Handler? = null

    override fun onDestroy() {
        super.onDestroy()
        if (receiver != null)
            unregisterReceiver(receiver)
    }

    override fun onCreate() {
        super.onCreate()
        setServiceState(this, ServiceState.STARTED)

        //register internet connection receiver
        this.receiver = InternetConnectionReceiver()
        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        this.registerReceiver(receiver, filter)

        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ProcessService::lock").apply {
                acquire()
            }
        }

        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fetchDataAndStartService()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (handler == null)
            handler = Handler(Looper.getMainLooper())

        handler?.removeCallbacksAndMessages(null)

        if (intent == null)
            return START_STICKY

        val action = intent.action

        when (action) {
            Actions.START.name -> {
                this.serviceStartedBy = intent.getStringExtra(AppConstants.SERVICE_STARTED_BY)

                when (intent.getSerializableExtra(AppConstants.SYNC_TYPE)) {
                    ServerSyncTypes.CREATE -> {
                        startProjectSync("onStartCommand")

                    }

                    ServerSyncTypes.PROCESS -> {
                        startProcessSync("onStartCommand")
                    }

                    ServerSyncTypes.UPLOAD -> {
                        val properties = java.util.HashMap<String, Any?>()
                            .apply {
                                put("service_state", "Started")
                                put("medium", "Image Uploading Service")
                            }

                        captureEvent(Events.SERVICE_STARTED, properties)
                        resumeUpload("onStartCommand")
                    }

                    ServerSyncTypes.SUBMIT_PROJECT -> {
                        startProjectSubmitSync("onStartCommand")
                    }
                }
            }
            Actions.STOP.name -> stopService()
            else -> error("No action in the received intent")
        }

        return START_STICKY
    }


    private fun fetchDataAndStartService() {
        createOngoingNotificaiton()
    }

    private fun createOngoingNotificaiton() {
        try {
            notificationId = 100
            val title = getString(R.string.app_name)
            val text = getString(R.string.image_uploading_in_progess)
            var notification = createNotification(title, text, true)

            notificationManager.notify(notificationId, notification)

            startForeground(notificationId, notification)
        } catch (e: ForegroundServiceStartNotAllowedException) {
            applicationContext.captureEvent("Service Start Exception",
                HashMap<String, Any?>().apply
                {
                    put("startedBy", serviceStartedBy)
                    put("exception", e.objectToString())
                })
        } catch (e: Exception) {
            applicationContext.captureEvent("Service Start Exception",
                HashMap<String, Any?>().apply
                {
                    put("startedBy", serviceStartedBy)
                    put("exception", e.objectToString())
                })
        }
    }

    private fun createNotification(title: String, text: String, isOngoing: Boolean): Notification {
        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(
                notificationChannelId,
                "Process Images Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Process Images Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(false)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this,
            if(Utilities.getBool(this,AppConstants.NEW_ENTERPRISE_USER,false)) MainDashboardActivity::class.java
            else MainDashboardActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
            ) else
                Notification.Builder(this)

        return builder
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.app_logo)
            .setOngoing(isOngoing)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()

    }

    private fun stopService() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }

            stopForeground(true)
            stopSelf()

            //Utilities.saveBool(this, AppConstants.UPLOADING_RUNNING, false)
            //cancel all jobs started by service


        } catch (e: Exception) {
            val properties = java.util.HashMap<String, Any?>()
                .apply {
                    put("type", e::class.java.simpleName)
                    put("error", e.message)
                }

            captureEvent("SERVICE_STOPPED_EXCEPTION", properties)
        }

        setServiceState(this, ServiceState.STOPPED)
    }


    override fun inProgress(title: String, type: ServerSyncTypes) {
        synchronized(this) {
            val internet =
                if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
            val finalContent = getString(R.string.innter_connection_label) + internet
            var notification = createNotification(title, finalContent, true)
            notificationManager.notify(notificationId, notification)
        }


//        if (type == ServerSyncTypes.UPLOAD)
    }

    override fun onCompleted(title: String, type: ServerSyncTypes) {

        val internet =
            if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
        val content = getString(R.string.innter_connection_label) + internet
        var notification = createNotification(title, content, true)

        notificationManager.notify(notificationId, notification)

        try {
            if (allDataSynced()) {
                handler?.postDelayed({
                    val title = getString(R.string.all_uploaded)
                    val internet =
                        if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
                    val content = getString(R.string.innter_connection_label) + internet
                    var notification = createNotification(title, content, false)

                    notificationManager.notify(notificationId, notification)
                    stopService()
                }, 5000)
            }
        } catch (e: java.lang.Exception) {

        }
    }

    override fun onConnectionLost(title: String, type: ServerSyncTypes) {
        captureEvent(Events.INTERNET_DISCONNECTED,
            HashMap<String, Any?>().apply {
                put("medium", "Service")
            })

        val internet =
            if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
        val content = getString(R.string.innter_connection_label) + internet
        var notification = createNotification(title, content, true)

        notificationManager.notify(notificationId, notification)
    }

    inner class InternetConnectionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val isConnected = context?.isInternetActive()

            if (isConnected == true) {
                //push event of internet connected
                captureEvent(
                    Events.INTERNET_CONNECTED,
                    HashMap<String, Any?>().apply {
                        put("medium", "Service")
                    })

                prjSync?.connectionLost = false
                processSkuSync?.connectionLost = false
                imageUploader?.connectionLost = false
                projectSubmitSync?.connectionLost = false

                checkPendingDataSync(DataSyncService::class.java.simpleName)
            } else {
                //push event of internet not connected
                captureEvent(Events.INTERNET_DISCONNECTED,
                    HashMap<String, Any?>().apply {
                        put("medium", "Service")
                    })

                prjSync?.connectionLost = true
                processSkuSync?.connectionLost = true
                imageUploader?.connectionLost = true
                projectSubmitSync?.connectionLost = true
            }
        }
    }

    private fun resumeUpload(type: String) {
        imageUploader = ImageUploader.getInstance(this, this)
        imageUploader?.uploadParent(type, serviceStartedBy)
    }

    private fun startProjectSync(type: String) {
        prjSync = ProjectSkuSync.getInstance(this, this)

        prjSync?.projectSyncParent(type, serviceStartedBy)
    }


    private fun startProcessSync(type: String) {
        processSkuSync = ProcessSkuSync.getInstance(
            this, this
        )

        processSkuSync?.processSkuParent(type, serviceStartedBy)
    }

    private fun startProjectSubmitSync(type: String) {
        projectSubmitSync = ProjectSubmitSync.getInstance(this, this)

        projectSubmitSync?.submitProjectParent(type, serviceStartedBy)
    }

}