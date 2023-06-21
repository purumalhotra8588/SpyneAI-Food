package com.spyneai.reshoot.data

import com.spyneai.shootapp.repository.model.image.Image


class SelectedImagesHelper {
    companion object {
        var selectedOverlayIds = HashMap<Int,ReshootImage>()
        var selectedImages = ArrayList<Image>()
    }
}