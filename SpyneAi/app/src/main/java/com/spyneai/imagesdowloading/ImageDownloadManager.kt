package com.spyneai.imagesdowloading

import android.os.Build
import android.os.Environment
import android.util.Log
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.spyneai.R
import com.spyneai.app.BaseApplication
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.Request
import java.io.File
import java.util.*


class ImageDownloadManager(var task: DownloadTask, var listener: Listener) {

    var path_save_photos: String = ""
    var  milli_second_time: String= System.currentTimeMillis().toString()

    fun start() {
        if (task.listHdQuality.size > 0 && task.listHdQuality != null) {

            for (i in 0 until task.listHdQuality.size) {
                if (task.listHdQuality[i] == null) {

                } else {

                    try {
                        download(task.listHdQuality[i], task.imageNameList[i])
                    }catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            }
        }
    }

    private fun download(imageFile: String, imageLabel : String) {

        var imageName= ""
//        if (imageName.length > 4 && imageName.takeLast(4) != ".jpg") {
//            imageName += if (task.isHd)
//               getUniqueIdentifier() + ".jpg"
//            else
//                getUniqueIdentifier() + "_watermark.jpg"
//        } else {
//            if (!task.isHd && imageName.length > 4)
//                imageName = imageName.dropLast(4) + getUniqueIdentifier() + "_watermark.jpg"
//            else
//                imageName = imageName.dropLast(4) + getUniqueIdentifier() + ".jpg"
//        }

        if (imageFile.takeLast(4) == ".png"){
            imageName = if (task.isHd)
                imageLabel.dropLast(4) + ".png"
            else
                imageLabel.dropLast(4) + "_watermark.png"
        }
            else if (imageFile.takeLast(4) == ".jpg"){
            imageName = if (task.isHd)
                imageLabel.dropLast(4) + ".jpg"
            else
                imageLabel.dropLast(4) + "_watermark.jpg"
        }
            else {
                if (task.isHd)
                    imageName = imageLabel.dropLast(4) + ".jpg"
                else
                    imageName = imageLabel.dropLast(4) + "_watermark.jpg"
            }


        val fetchConfiguration: FetchConfiguration =
            FetchConfiguration.Builder(BaseApplication.getContext())
                .build()
            val fetch = Fetch.getInstance(fetchConfiguration)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            path_save_photos =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + File.separator + R.string.app_name+" ($milli_second_time)"
        } else {
            path_save_photos = Environment.getExternalStorageDirectory().absolutePath +
                    File.separator +
                    BaseApplication.getContext().resources.getString(R.string.app_name)+" ($milli_second_time)"
        }

        var file = File(path_save_photos)

        val request = Request(imageFile, path_save_photos + "/${imageName}")
//        request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG")

        fetch!!.enqueue(request, { updatedRequest ->
            listener.onScan(file.absolutePath + "/" + imageName)
            task.downloadCount++

            if (task.downloadCount == task.listHdQuality.size) {
                listener.onSuccess(task)
            }

        }) { error ->
            Log.d("ImageDownloadManager", "onError: " + error.toString())
            if (!task.failureNotified) {
                task.failureNotified = true
                listener.onFailure(task)
            }
        }
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


    //Download
    private fun downloadWithHighQuality(imageFile: String, imageName: String) {

        var imageName = imageName
        if (imageName.length > 4 && imageName.takeLast(4) != ".jpg") {
            imageName += if (task.isHd)
                ".jpg"
            else
                "_watermark.jpg"
        } else {
            if (!task.isHd && imageName.length > 4)
                imageName = imageName.dropLast(4) + "_watermark.jpg"
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            path_save_photos =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + File.separator + R.string.app_name;
        } else {
            path_save_photos = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator +
                    BaseApplication.getContext().getResources().getString(R.string.app_name)
        }

        var file = File(path_save_photos)

        //delete existing file
        if (File(path_save_photos).exists())
            File(path_save_photos).delete()

        PRDownloader.download(
            imageFile,
            path_save_photos,
            imageName
        )
            .build()
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    listener.onScan(file.absolutePath + "/" + imageName)
                    task.downloadCount++

                    if (task.downloadCount == task.listHdQuality.size) {
                        listener.onSuccess(task)
                    }
                }

                override fun onError(error: Error?) {
                    Log.d("ImageDownloadManager", "onError: " + error.toString())
                    if (error?.connectionException != null && error.connectionException.message == "Rename Failed") {
                        task.downloadCount++

                        if (task.downloadCount == task.listHdQuality.size) {
                            listener.onSuccess(task)
                        }
                    } else {
                        if (!task.failureNotified) {
                            task.failureNotified = true
                            listener.onFailure(task)
                        }
                    }

                }

            })
    }

    interface Listener {
        fun onSuccess(task: DownloadTask)
        fun onScan(filePath: String)
        fun onRefresh(filePath: String)
        fun onFailure(task: DownloadTask)
    }
}