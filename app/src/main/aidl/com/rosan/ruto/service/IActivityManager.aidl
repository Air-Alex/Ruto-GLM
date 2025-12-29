package com.rosan.ruto.service;

import android.content.Intent;

interface IActivityManager {
    void startLabel(in String label, int displayId);

    void startApp(in String packageName, int displayId);

    void startActivity(in Intent intent, int displayId);
}