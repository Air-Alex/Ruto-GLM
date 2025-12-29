package com.rosan.ruto.service;

import android.content.pm.ApplicationInfo;

interface IPackageManager {
    List<ApplicationInfo> getInstalledApplications(int flags, int appFlags);
}