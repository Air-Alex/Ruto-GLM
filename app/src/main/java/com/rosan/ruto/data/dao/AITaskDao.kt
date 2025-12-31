package com.rosan.ruto.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rosan.ruto.data.model.AITask
import com.rosan.ruto.data.model.TaskData

@Dao
interface AITaskDao {
    @Insert
    suspend fun insertTask(task: AITask): Long

    @Query("SELECT * FROM ai_tasks WHERE id = :taskId")
    suspend fun getTask(taskId: Long): AITask?
}

@Dao
interface TaskDataDao {
    @Insert
    suspend fun insertData(data: TaskData): Long

    @Query("SELECT * FROM task_data WHERE task_id = :taskId")
    suspend fun getDataForTask(taskId: Long): List<TaskData>
}
