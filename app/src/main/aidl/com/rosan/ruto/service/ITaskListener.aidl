package com.rosan.ruto.service;

interface ITaskListener {
    oneway void onThink(String text);
    oneway void onError(String msg);
    oneway void onAction(String action);
    oneway void onFinish();
}
