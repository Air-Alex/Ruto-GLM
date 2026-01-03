package com.rosan.ruto.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rosan.ruto.data.model.AiModel
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun add(model: AiModel)

    @Update
    abstract suspend fun update(model: AiModel)

    @Query("DELETE FROM ai_models WHERE id in (:ids)")
    abstract suspend fun remove(ids: List<Long>)

    open suspend fun remove(vararg ids: Long) = remove(ids.toList())

    @Query("SELECT * FROM ai_models WHERE id = :id")
    abstract suspend fun get(id: Long): AiModel?

    @Query("SELECT * FROM ai_models")
    abstract fun observeAll(): Flow<List<AiModel>>
}
