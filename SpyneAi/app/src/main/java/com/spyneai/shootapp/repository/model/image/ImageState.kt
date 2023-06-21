package com.spyneai.shootapp.repository.model.image

enum class ImageState {
    CLASSIFICATION_PENDING,
    QUEUED,
    UPLOADING,
    UPLOADED,
    PROCESSED
}