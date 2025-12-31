package com.rosan.ruto.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rosan.ruto.data.dao.AITaskDao
import com.rosan.ruto.data.dao.TaskDataDao
import com.rosan.ruto.data.model.AITask
import com.rosan.ruto.data.model.TaskData

@Database(entities = [AITask::class, TaskData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun aiTaskDao(): AITaskDao
    abstract fun taskDataDao(): TaskDataDao
}
