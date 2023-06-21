package com.spyneai.loginsignup.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Categories(
    @PrimaryKey
    val userId: String,
    val data: String
)
