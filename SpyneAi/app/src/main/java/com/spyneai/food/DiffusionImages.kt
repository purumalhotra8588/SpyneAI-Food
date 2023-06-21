package com.spyneai.food

data class DiffusionImages(
    var frameNumber : Int,
    val rawUrl : String,
    var processedImageUrl : String?,
    var isSelected : Boolean,
    var isEnabled: Boolean,
    var imageId : String?
)
