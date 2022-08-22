package com.test.toastproject;

import android.app.Application;

import com.github.toast.ToastUtils;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.init(this);
    }
}