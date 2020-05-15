package com.escapp.controller;

import android.util.Log;

/**
 * Created by laura on 23.1.15.
 */
public class Logger {
    // 1 = Errors only
    // 2 = +Warning msgs
    // 3 = +Info msgs
    // 4 = +Debug msgs
    // 5 = +Verbose msgs
    private static int LOG_LEVEL = 0;
    private static boolean LOG_CLASS = false;
    private static String LOG_TAG = "ESCAppLog";

    public static void setLogLevel(int logLevel, boolean logClass) {
        LOG_LEVEL = logLevel;
        LOG_CLASS = logClass;
    }

    public static void e(String message) {
        if (LOG_LEVEL > 0) {
            Log.e(getTag(), message);
        }
    }

    public static void w(String message) {
        if (LOG_LEVEL > 1) {
            Log.w(getTag(), message);
        }
    }

    public static void i(String message) {
        if (LOG_LEVEL > 2) {
            Log.i(getTag(), message);
        }
    }

    public static void d(String message) {
        if (LOG_LEVEL > 3) {
            Log.d(getTag(), message);
        }
    }

    public static void v(String message) {
        if (LOG_LEVEL > 4) {
            Log.v(getTag(), message);
        }
    }

    private static String getTag() {
        String tag = LOG_TAG;
        if (LOG_CLASS) {
            String clz = "";
            try {
                throw new Exception("");
            } catch( Exception e ) {
                tag = e.getStackTrace()[2].getClassName().replace("com.escapp.", "");
            }
        }
        return tag;
    }
}
