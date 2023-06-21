package com.spyneai.shootapp.data

import com.spyneai.shootapp.repository.model.image.ImageDao
import com.spyneai.shootapp.repository.model.image.Image


class ImagesRepoV2(val imageDaoApp: ImageDao) {

    fun isImageExist(skuUuid: String,overlayId: String,sequence: Int) = imageDaoApp.getImageBySkuUuid(skuUuid,overlayId)

    fun updateImage(Image: Image) = imageDaoApp.updateImage(Image)

    //fun updateImagePathOnReclick(path: String, uuid: String) = imageDaoApp.updateImagePathOnReclick(path, uuid)

    fun getImagesBySkuId(uuid: String) = imageDaoApp.getImagesBySkuId(uuid)

    fun getImagesPathBySkuId(uuid: String) = imageDaoApp.getImagesPathBySkuIdLiveData(uuid)

    fun totalRemainingUpload() = imageDaoApp.totalRemainingUpload()

    fun totalRemainingMarkDone() = imageDaoApp.totalRemainingUpload(true, isMarkedDone = false)

    fun getRemainingAbove(uuid: String) = 0

    fun getRemainingAboveSkipped(uuid: String) = 0

    fun getRemainingBelow(uuid: String) = 0

    fun getRemainingBelowSkipped(uuid: String) = 0

    fun getOldestImage() = imageDaoApp.getOldestImage()

    fun skipImage(uuid: String,toProcessAt: Long) = imageDaoApp.skipImage(uuid,toProcessAt)

    fun markDone(uuid: String) = imageDaoApp.markDone(uuid)

    fun addPreSignedUrl(Image: Image) = imageDaoApp.addPreSignedUrl(Image.uuid,Image.preSignedUrl!!,Image.imageId!!)

    fun markUploaded(uuid: String) = imageDaoApp.markUploaded(uuid)

    fun markStatusUploaded(uuid: String) = imageDaoApp.markStatusUploaded(uuid)

    fun getImage(uuid: String): Image = imageDaoApp.getImage(uuid)

    fun getUploadedCount(skuUuId: String) = imageDaoApp.getUploadedCount(skuUuId)

    fun getTotalImageCount(skuUuId: String) = imageDaoApp.getTotalImageCount(skuUuId)

    fun getImageForComment(skuId: String, overlayId: String) = imageDaoApp.getImageForComment(skuId,overlayId)
}