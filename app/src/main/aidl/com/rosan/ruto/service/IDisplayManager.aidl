package com.rosan.ruto.service;

import android.os.IBinder;
import android.view.DisplayInfo;
import android.view.Surface;
import com.rosan.ruto.display.BitmapWrapper;

interface IDisplayManager {
    void onDestroy() = 16711679;

    int[] getDisplayIds() = 1;

    List<DisplayInfo> getDisplays() = 2;

    DisplayInfo getDisplayInfo(int displayId) = 3;

    int createDisplay(in String name, int width, int height, int density, in Surface surface) = 4;

    int mirrorDisplay(int displayId, in Surface surface) = 5;

    boolean isMyDisplay(int displayId) = 6;

    BitmapWrapper capture(int displayId) = 7;

    void setSurface(int displayId, in Surface surface) = 8;

    void release(int displayId) = 9;

    int createDisplay2(in Surface surface) = 10;
}