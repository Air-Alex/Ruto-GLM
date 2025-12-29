package com.android.server.display;

import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public class DisplayControl {
    public static IBinder createVirtualDisplay(String name, boolean secure) {
        return null;
    }

    public static void destroyDisplay(IBinder displayToken) {
    }

    public static long[] getPhysicalDisplayIds() {
        return new long[0];
    }

    public static IBinder getPhysicalDisplayToken(long physicalDisplayId) {
        return null;
    }
}
