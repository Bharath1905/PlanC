package com.pemchip.blablacar.common;

import android.util.Log;

public class CustomLog {

    public static boolean mCanDisableLog = false;

    public static void trace(Object LogStr)
    {
        if(mCanDisableLog)return;
        String mLogStr = LogStr.toString();
        Log.d("CUSTOM_TRACE",mLogStr);
    }
}
