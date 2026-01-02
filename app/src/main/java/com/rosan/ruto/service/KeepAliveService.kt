package com.rosan.ruto.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.ImageReader
import android.os.Binder
import android.os.Build
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.view.InputEvent
import android.view.Surface
import androidx.core.app.NotificationCompat
import com.rosan.ruto.device.repo.DeviceRepo
import com.rosan.ruto.ruto.DefaultRutoRuntime
import com.rosan.ruto.ruto.RutoGLM
import com.rosan.ruto.ruto.repo.RutoObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class KeepAliveService : Service(), KoinComponent {
    private val ruto by inject<RutoObserver>()

    private val CHANNEL_ID = "KeepAliveServiceChannel"
    private val NOTIFICATION_ID = 1

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val deviceRepo: DeviceRepo by inject()

    private class InternalTaskContext(
        val apiKey: String,
        val hostUrl: String,
        val modelId: String,
        val task: String,
        var displayId: Int,
        var status: TaskStatus = TaskStatus.RUNNING,
        var statusMessage: String? = null,
        val listeners: MutableList<ITaskListener> = CopyOnWriteArrayList(),
        var job: Job? = null
    )

    private val tasks = ConcurrentHashMap<String, InternalTaskContext>()
    private val imageReaderMap = ConcurrentHashMap<String, ImageReader>()

    private val thread = HandlerThread("image copy")

    inner class LocalBinder : Binder() {
        fun getService(): KeepAliveService = this@KeepAliveService
    }

    companion object {
        var instance: KeepAliveService? = null
            private set

        fun isRunning(): Boolean = instance != null

        fun start(context: Context) {
            val intent = Intent(context, KeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        thread.start()
        ruto.onInitialize(CoroutineScope(Dispatchers.IO))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        ruto.onDestroy()
        thread.join()
        instance = null
        serviceScope.cancel()
        tasks.keys.forEach { stopTask(it) } // Stop all tasks on service destroy
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun getTasks(): Map<String, TaskContext> {
        return tasks.mapValues { (_, internalContext) ->
            TaskContext(
                apiKey = internalContext.apiKey,
                hostUrl = internalContext.hostUrl,
                modelId = internalContext.modelId,
                task = internalContext.task,
                displayId = internalContext.displayId,
                status = internalContext.status,
                statusMessage = internalContext.statusMessage
            )
        }
    }

    fun bindListener(key: String, listener: ITaskListener) {
        tasks[key]?.listeners?.add(listener)
    }

    fun unbindListener(key: String, listener: ITaskListener) {
        tasks[key]?.listeners?.remove(listener)
    }

    fun setSurface(key: String, surface: Surface?) {
        Log.e("r0s", "setSurface $surface $key")
        tasks[key]?.let { taskContext ->
            if (taskContext.displayId != -1) {
                deviceRepo.displayManager.setSurface(taskContext.displayId, surface)
            }
        }
    }

    fun onTouch(key: String, event: InputEvent) {
        Log.e("r0s", "onTOuch $event")
        tasks[key]?.let { taskContext ->
            deviceRepo.inputManager.injectEvent(event, taskContext.displayId)
        }
    }

    fun stopTask(key: String) {
        tasks.remove(key)?.let { taskContext ->
            taskContext.job?.cancel()

            imageReaderMap.remove(key)?.close()
            if (taskContext.displayId != -1) {
                deviceRepo.displayManager.release(taskContext.displayId)
            }
            taskContext.status = TaskStatus.STOPPED
            taskContext.statusMessage = "Task stopped by user."
            taskContext.listeners.forEach { listener ->
                try {
                    listener.onFinish()
                } catch (e: Exception) {
                }
            }
        }
    }

    fun executeTask(
        key: String,
        packageName: String,
        apiKey: String,
        hostUrl: String,
        modelId: String,
        task: String,
        displayId: Int,
        listener: ITaskListener? = null
    ) {

        if (tasks.containsKey(key)) {
            stopTask(key)
        }

        val taskContext = InternalTaskContext(
            apiKey = apiKey,
            hostUrl = hostUrl,
            modelId = modelId,
            task = task,
            displayId = displayId
        )
        listener?.let { taskContext.listeners.add(it) }
        tasks[key] = taskContext

        val prompt = """
你是一个智能体分析专家，可以根据操作历史和当前状态图执行一系列操作来完成任务。
你必须严格按照要求输出以下格式：
<think>选择这个操作的简短推理说明</think>
<answer>指令</answer>

操作指令集及其作用如下：
- do(action="Launch", app="xxx")
- do(action="Tap", element=[x,y])
- do(action="Type", text="xxx")
- do(action="Swipe", start=[x1,y1], end=[x2,y2])
- finish(message="xxx")
""".trimIndent()

        val job = serviceScope.launch {
            runCatching {
                if (taskContext.displayId == -1) {
                    taskContext.displayId =
                        deviceRepo.displayManager.createDisplay2(null)
                }

                if (packageName.isNotBlank()) {
                    deviceRepo.activityManager.startApp(packageName, taskContext.displayId)
                }
                val runtime = DefaultRutoRuntime(deviceRepo, taskContext.displayId)
                val glm = RutoGLM(
                    hostUrl, modelId, apiKey, prompt, runtime, deviceRepo, taskContext.displayId
                )

                glm.ruto(
                    text = task,
                    onStreaming = { think ->
                        taskContext.status = TaskStatus.THINKING
                        taskContext.statusMessage = think.trim()
                        taskContext.listeners.forEach { it.onThink(think.trim()) }
                    },
                    onCapture = {
                        val co = tasks[key]!!
                        val display = deviceRepo.displayManager.getDisplayInfo(co.displayId)
                        val width = display.logicalWidth
                        val height = display.logicalHeight
                        deviceRepo.displayManager.capture(co.displayId)?.bitmap
                            ?: Bitmap.createBitmap(
                                width, height,
                                Bitmap.Config.ARGB_8888
                            )
                    }
                )
//                taskContext.status = TaskStatus.COMPLETED
//                taskContext.statusMessage = "Task completed successfully."
//                taskContext.listeners.forEach { it.onFinish() }
            }.onFailure { error ->
                error.printStackTrace()
                if (error is kotlinx.coroutines.CancellationException) {
                    return@onFailure
                }
                taskContext.status = TaskStatus.FAILED
                taskContext.statusMessage = error.stackTraceToString()
                taskContext.listeners.forEach { it.onError(error.stackTraceToString()) }
                imageReaderMap.remove(key)?.close()
                if (taskContext.displayId != -1) {
                    deviceRepo.displayManager.release(taskContext.displayId)
                }
            }
        }
        taskContext.job = job
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Keep Alive Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            android.app.PendingIntent.getActivity(
                this,
                0,
                it,
                android.app.PendingIntent.FLAG_IMMUTABLE
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ruto Service")
            .setContentText("Ruto is running to stay active.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }
}