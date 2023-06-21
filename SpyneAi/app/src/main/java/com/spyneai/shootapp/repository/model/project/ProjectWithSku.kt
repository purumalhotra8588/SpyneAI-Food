package com.spyneai.shootapp.repository.model.project

import androidx.room.Embedded
import androidx.room.Relation
import com.spyneai.shootapp.repository.model.sku.Sku


data class ProjectWithSku(
    @Embedded val projectApp: Project?,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "projectUuid"
    )
    val skuses: List<Sku>?
)