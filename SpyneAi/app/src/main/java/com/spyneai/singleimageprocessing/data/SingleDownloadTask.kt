package com.spyneai.singleimageprocessing.data

data class SingleDownloadTask(
    var listHdQuality: ArrayList<String> = ArrayList(),
    var imageNameList: ArrayList<String> = ArrayList(),
    var isCompleted: Boolean = false,
    var isFailure: Boolean = false,
    var failureNotified: Boolean = false,
    var isHd: Boolean = true
)