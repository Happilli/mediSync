package com.bca.medisync;

import android.app.Application;

import com.bca.medisync.data.remote.ApiClient;

public class MediSyncApp extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        ApiClient.init(getApplicationContext());
    }
}
