package com.spyneai.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.posthog.android.PostHog

import com.spyneai.shootapp.workmanager.InternetWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit


@SuppressLint("StaticFieldLeak")
@HiltAndroidApp
class BaseApplication : Application() {

    private val POSTHOG_API_KEY = "phc_hlFWW1WOhvHpb2YiDyIcM4ewhPHOi0fu5jUFRbzgHgR"
    private val POSTHOG_HOST = "https://posthog.spyne.ai"


    companion object {
        private lateinit var context: Context

        fun getContext(): Context {
            return context;
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        //disable night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val posthog: PostHog = PostHog.Builder(context, POSTHOG_API_KEY, POSTHOG_HOST)
            .captureApplicationLifecycleEvents() // Record certain application events automatically!
            .build()

        // Set the initialized instance as a globally accessible instance.
        PostHog.setSingletonInstance(posthog)

        val repeatInternal = 30L
        val flexInterval = 25L
        val workerTag = "InternetWorker"

        PeriodicWorkRequest
            .Builder(
                InternetWorker::class.java, repeatInternal,
                TimeUnit.MINUTES, flexInterval, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
            .build()
            .also {
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(workerTag,
                    ExistingPeriodicWorkPolicy.REPLACE, it)
            }

    }

}