package com.rosan.ruto.service;

import android.graphics.PointF;
import android.view.InputEvent;

interface IInputManager {
    void injectEvent(in InputEvent event, int displayId);

    void click(in PointF p, int displayId);

    void doubleClick(in PointF p, int displayId);

    void longClick(in PointF p, int displayId);

    void swipe(in PointF start, in PointF end, int displayId);

    void clickBack(int displayId);

    void clickHome(int displayId);
}