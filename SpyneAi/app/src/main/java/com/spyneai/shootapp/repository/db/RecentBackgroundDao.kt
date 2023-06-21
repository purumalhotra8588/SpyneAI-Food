package com.spyneai.shootapp.repository.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spyneai.shootapp.repository.model.RecentBackground

@Dao
interface RecentBackgroundDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(seletedBg: RecentBackground)

    @Query("select * from recentbackground where parentId = :parentId ")
    fun getRecentBg(parentId: String) : LiveData<RecentBackground>
}