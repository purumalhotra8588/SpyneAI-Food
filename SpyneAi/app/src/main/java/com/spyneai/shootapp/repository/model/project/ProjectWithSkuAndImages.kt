package com.spyneai.shootapp.repository.model.project



import com.spyneai.shootapp.repository.model.image.Image
import com.spyneai.shootapp.repository.model.sku.Sku


data class ProjectWithSkuAndImages(
    val skus: Sku,
    val imageApps: List<Image>?
)