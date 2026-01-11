package com.rosan.ruto.di

import android.content.Context
import android.util.Log
import com.rosan.installer.ext.service.ShizukuServiceManager
import com.rosan.installer.ext.service.ShizukuTerminalServiceManager
import com.rosan.installer.ext.service.TerminalServiceManager
import com.rosan.ruto.device.DeviceManager
import com.rosan.ruto.util.PermissionProvider
import com.rosan.ruto.util.SettingsManager
import org.koin.dsl.module
import org.koin.dsl.onClose

val deviceModule = module {
    // 外部变量用于跨 Factory 缓存实例
    var cached: DeviceManager? = null

    factory<DeviceManager> {
        val context: Context = get()
        // 获取当前的权限提供者（默认 Shizuku）
        val provider = SettingsManager.getPermissionProvider(context) ?: PermissionProvider.SHIZUKU

        // 1. 定义校验规则：检查当前缓存的实例是否与当前的 Provider 匹配
        val isCacheValid = cached?.let { dev ->
            val sm = dev.serviceManager
            when (provider) {
                PermissionProvider.SHIZUKU -> sm is ShizukuServiceManager
                PermissionProvider.SHIZUKU_TERMINAL -> sm is ShizukuTerminalServiceManager
                PermissionProvider.TERMINAL, PermissionProvider.ROOT -> {
                    val targetShell = if (provider == PermissionProvider.ROOT) "su"
                    else SettingsManager.getTerminalShell(context)
                    sm is TerminalServiceManager && sm.shell == targetShell
                }
            }
        } ?: false

        // 2. 根据校验结果获取或创建实例
        val deviceManager: DeviceManager = if (isCacheValid) {
            // 缓存有效，直接断言非空返回
            cached!!
        } else {
            // 缓存无效，执行“先清理、再创建”
            Log.d("r0s", "Cache invalid or provider changed, recreating DeviceManager...")

            // 优美的清理：取出旧引用并立即置空，防止后续逻辑误用
            cached?.also { cached = null }?.apply {
                close()
                serviceManager.close()
            }

            // 根据 Provider 创建新的 ServiceManager
            val newServiceManager = when (provider) {
                PermissionProvider.SHIZUKU -> ShizukuServiceManager(context)
                PermissionProvider.SHIZUKU_TERMINAL -> ShizukuTerminalServiceManager(context)
                else -> {
                    val shell = if (provider == PermissionProvider.ROOT) "su"
                    else SettingsManager.getTerminalShell(context)
                    TerminalServiceManager(context, shell)
                }
            }

            // 创建新 DeviceManager 并存入缓存
            DeviceManager(context, newServiceManager).also {
                cached = it
            }
        }

        // 明确返回非空类型，修复编译错误
        deviceManager

    } onClose { instance ->
        // 3. 生命周期终点：彻底清理
        Log.d("r0s", "Koin onClose: Cleaning up DeviceManager resources")

        // 如果 onClose 的实例正好是缓存的实例，则置空缓存
        if (cached == instance) {
            cached = null
        }

        // 释放资源
        instance?.apply {
            close()
            serviceManager.close()
        }
    }
}
