package com.spyneai.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spyneai.isInternetActive


import android.os.Handler
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {

    private var receiver: InternetConnectionReceiver? = null
    open var TAG = BaseActivity::class.simpleName
    var fisrtime = true
    var notifyChange = true
    var handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    abstract fun onConnectionChange(isConnected: Boolean)

    override fun onStart() {
        super.onStart()
        receiver = InternetConnectionReceiver()
        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        this.registerReceiver(receiver, filter)
    }

    override fun onStop() {
        super.onStop()
        receiver?.let {
            unregisterReceiver(it)
        }
    }

    inner class InternetConnectionReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            if (!fisrtime){
                context?.let {
                    if (notifyChange){
                        val isConnected = it.isInternetActive()
                        if (!Utilities.getBool(it,AppConstants.NOTOFIED_ONCE,false) || Utilities.getBool(it,AppConstants.LAST_NOTIFIED,false) != isConnected){
                            Utilities.saveBool(it,AppConstants.NOTOFIED_ONCE,true)
                            Utilities.saveBool(it,AppConstants.LAST_NOTIFIED,isConnected)
                            onConnectionChange(isConnected)
                            notifyChange = false
                            handler.removeCallbacksAndMessages(null)
                            handler.postDelayed({
                                notifyChange = true
                            },1000)
                        }

                    }
                }
            }else{
                fisrtime = false
            }

        }
    }
}