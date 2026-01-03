package com.rosan.installer.ext.util

import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.ResultReceiver
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

fun IBinder.shellCommand(vararg args: String) {
    val file = File("/dev/null")
    file.createNewFile()
    val input = file.inputStream()
    val output = file.outputStream()

    val data = Parcel.obtain()
    val reply = Parcel.obtain()
    data.writeFileDescriptor(input.fd)
    data.writeFileDescriptor(output.fd)
    data.writeFileDescriptor(output.fd)
    data.writeStringArray(args)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        data.writeParcelable(null, 0)
    ResultReceiver(Handler(Looper.getMainLooper())).writeToParcel(data, 0)
    try {
        this.transact(1598246212, data, reply, 0)
    } finally {
        input.close()
        output.close()
        data.recycle()
        reply.recycle()
    }
}

fun IBinder.dumpToBytes(vararg args: String): ByteArray {
    val pipe = ParcelFileDescriptor.createPipe()
    val readPfd = pipe[0]
    val writePfd = pipe[1]

    val data = Parcel.obtain()
    val reply = Parcel.obtain()

    try {
        data.writeFileDescriptor(writePfd.fileDescriptor)
        data.writeStringArray(args)

        this.transact(IBinder.DUMP_TRANSACTION, data, reply, 0)

        writePfd.close()

        val inputStream = FileInputStream(readPfd.fileDescriptor)
        val outputStream = ByteArrayOutputStream()

        var length = 0
        while (inputStream.available().also { length = it } > 0) {
            val buffer = ByteArray(length)
            outputStream.write(buffer, 0, inputStream.read(buffer))
        }

        return outputStream.toByteArray()

    } catch (e: Exception) {
        e.printStackTrace()
        return byteArrayOf()
    } finally {
        readPfd.close()
        try {
            writePfd.close()
        } catch (e: Exception) {
        }
        data.recycle()
        reply.recycle()
    }
}