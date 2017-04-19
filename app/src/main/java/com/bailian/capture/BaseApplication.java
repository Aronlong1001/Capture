package com.bailian.capture;

import android.app.Application;
import android.content.Context;

/**
 * Created by aron on 2017/4/18.
 */

public class BaseApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
    }

    public static Context getContext(){
        return mContext;
    }
}
