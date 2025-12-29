package com.rosan.ruto.service;

import android.os.IBinder;
import android.view.DisplayInfo;
import android.view.Surface;
import com.rosan.ruto.display.BitmapWrapper;

interface IDisplayManager {
    int[] getDisplayIds();

    DisplayInfo getDisplayInfo(int displayId);

    int createDisplay(in Surface surface);

    BitmapWrapper capture(int displayId);

    void release(int displayId);

    void setSurface(int displayId, in Surface surface);
}