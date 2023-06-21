package com.spyneai.loginsignup.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CategoriesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveCategory(obj: Categories) : Long

    @Query("SELECT * FROM categories where userId = :userId")
    fun getCategory(userId: String): Categories?
}