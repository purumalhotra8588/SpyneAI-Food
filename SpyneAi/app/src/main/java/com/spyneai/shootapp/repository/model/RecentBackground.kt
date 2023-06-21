package com.spyneai.shootapp.repository.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecentBackground(
    @PrimaryKey(autoGenerate = false)
    val parentId : String,
    var categoryId : String,
    val bgName: String,
    val gifUrl: String?,
    val imageCredit: Int,
    val bgId: String,
    val imageUrl: String
)