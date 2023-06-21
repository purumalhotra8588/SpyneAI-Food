package com.spyneai.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class StartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
//        if (intent.action == Intent.ACTION_BOOT_COMPLETED && getServiceState(context) == ServiceState.STARTED) {
//            val shootLocalRepository = ImagesRepoV2(SpyneAppDatabase.getInstance(context).imageDao())
//
//            if (GlobalScope.launch(Dispatchers.IO) { shootLocalRepository.getOldestImage() } != null){
//
//                var action = Actions.START
//                if (getServiceState(context) == com.spyneai.service.ServiceState.STOPPED && action == Actions.STOP)
//                    return
//
//                val serviceIntent = Intent(context, DataSyncService::class.java)
//                serviceIntent.putExtra(AppConstants.SERVICE_STARTED_BY, StartReceiver::class.simpleName)
//                serviceIntent.action = action.name
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    log("Starting the service in >=26 Mode")
//                    ContextCompat.startForegroundService(context, serviceIntent)
//                    return
//                } else {
//                    log("Starting the service in < 26 Mode")
//                    context.startService(serviceIntent)
//                }
//
//                val properties = HashMap<String,Any?>()
//                    .apply {
//                        put("service_state","Started")
//                        put("email",
//                            Utilities.getPreference(context, AppConstants.EMAIL_ID).toString())
//                        put("medium","Main Actity")
//                    }
//
//                context.captureEvent(Events.SERVICE_STARTED,properties)
//            }
//        }
    }
}
