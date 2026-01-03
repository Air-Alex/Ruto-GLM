package com.rosan.ruto.ruto

import android.util.Log
import com.rosan.ruto.ruto.script.RutoRuntime

object GLMCommandParser {

    sealed class Status {
        object None : Status()

        // 发现指令苗头
        data class Triggered(val candidates: List<Command>, val prefixText: String) : Status()

        // 第一个 Marker 已匹配完成
        data class Pending(val command: Command, val prefixText: String) : Status()

        // 完整匹配
        data class Completed(val command: Command, val fragments: List<String>) : Status() {
            // 优雅的参数提取：只取 Marker 之间的内容
            val arguments: List<String> = if (fragments.size >= command.markerList.size) {
                fragments.subList(1, command.markerList.size)
            } else emptyList()

            // 提取指令前后的正文内容
            val prefixContent: String = fragments.first()
            val suffixContent: String = fragments.last()

            fun callFunction(runtime: RutoRuntime): Any {
                return runtime.callFunction(command.mapping, arguments = arguments.toTypedArray())
            }
        }
    }

    enum class Command(val mapping: String, vararg markers: String) {
        Launch("launch", "do(action=\"Launch\", app=\"", "\")"),
        TapM("click", "do(action=\"Tap\", element=[", ",", "], message=\"", "\")"),
        Tap("click", "do(action=\"Tap\", element=[", ",", "])"),
        Type("text", "do(action=\"Type\", text=\"", "\")"),
        TypeName("type", "do(action=\"Type_Name\", text=\"", "\")"),
        Interact("interact", "do(action=\"Interact\")"),
        Swipe("swipe", "do(action=\"Swipe\", start=[", ",", "], end=[", ",", "])"),
        Note("note", "do(action=\"Note\", message=\"", "\")"),
        CallAPI("call_api", "do(action=\"Call_API\", instruction=\"", "\")"),
        LongPress("long_click", "do(action=\"Long Press\", element=[", ",", "])"),
        DoubleTap("double_click", "do(action=\"Double Tap\", element=[", ",", "])"),
        TakeOver("take_over", "do(action=\"Take_over\", message=\"", "\")"),
        Back("back", "do(action=\"Back\")"),
        Home("home", "do(action=\"Home\")"),
        Wait("wait", "do(action=\"Wait\", duration=\"", " seconds\")"),
        Finish("finish", "finish(message=\"", "\")");

        val markerList = markers.toList()
        val firstMarker = markers.first()
        val isNoArgs = markers.size == 1
    }

    // 按 Marker 复杂度排序，确保优先匹配长指令（如 TapName 优先于 Tap）
    private val sortedCommands = Command.entries.sortedByDescending { it.markerList.size }

    fun parse(text: String): Status {
        if (text.isBlank()) return Status.None

        // 1. 尝试匹配 Completed (完整指令)
        for (command in sortedCommands) {
            val markers = command.markerList
            val matchIndices = IntArray(markers.size)
            var lastPos = text.length
            var fullMatch = true

            // 从后往前搜，确保获取的是最后一条指令
            for (i in markers.indices.reversed()) {
                val found = text.lastIndexOf(markers[i], lastPos - 1)
                // 边界检查：指令起始 Marker 必须满足非字母边界
                if (found != -1 && (i > 0 || isBoundary(text, found))) {
                    matchIndices[i] = found
                    lastPos = found
                } else {
                    fullMatch = false
                    break
                }
            }

            if (fullMatch) {
                return Status.Completed(command, extractSegments(text, matchIndices, markers))
            }
        }

        // 2. 尝试匹配 Pending (起始 Marker 已写完)
        for (command in sortedCommands) {
            val first = command.firstMarker
            val pos = text.lastIndexOf(first)
            if (pos != -1 && isBoundary(text, pos)) {
                return Status.Pending(command, text.substring(0, pos))
            }
        }

        // 3. 探测 Triggered (正在输入 Marker 中)
        var bestMatchLen = 0
        val candidates = mutableListOf<Command>()

        for (command in Command.entries) {
            val matchLen = getMatchLengthAtEnd(text, command.firstMarker)
            if (matchLen > 0) {
                if (matchLen > bestMatchLen) {
                    bestMatchLen = matchLen
                    candidates.clear()
                }
                if (matchLen == bestMatchLen) {
                    candidates.add(command)
                }
            }
        }

        return if (candidates.isNotEmpty()) {
            Status.Triggered(candidates, text.dropLast(bestMatchLen))
        } else {
            Status.None
        }
    }

    private fun isBoundary(text: String, pos: Int): Boolean {
        return pos == 0 || !text[pos - 1].isLetter()
    }

    private fun getMatchLengthAtEnd(text: String, prefix: String): Int {
        val maxLen = minOf(text.length, prefix.length)
        for (len in maxLen downTo 2) { // 至少匹配2个字符，减少干扰
            if (text.regionMatches(text.length - len, prefix, 0, len)) return len
        }
        return 0
    }

    private fun extractSegments(
        text: String,
        indices: IntArray,
        markers: List<String>
    ): List<String> {
        val segments = mutableListOf<String>()
        var cursor = 0
        for (i in indices.indices) {
            segments.add(text.substring(cursor, indices[i]))
            cursor = indices[i] + markers[i].length
        }
        segments.add(text.substring(cursor))
        return segments
    }
}