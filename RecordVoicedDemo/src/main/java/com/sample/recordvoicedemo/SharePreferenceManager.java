package com.sample.recordvoicedemo;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferenceManager {
    static SharedPreferences sp;

    public static void init(Context context, String name) {
        sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    private static final String KEY_CACHED_PATH = "record_voice_path";

    public static void setCachedPath(String path) {
        if (null != sp) {
            sp.edit().putString(KEY_CACHED_PATH, path).commit();
        }
    }

    public static String getCachedPath() {
        if (null != sp) {
            return sp.getString(KEY_CACHED_PATH, null);
        }
        return null;
    }


    private static final String KEY_DURATION = "duration";

    public static void setCachedDuration(int height){
        if(null != sp){
            sp.edit().putInt(KEY_DURATION, height).commit();
        }
    }

    public static int getCachedDuration(){
        if(null != sp){
            return sp.getInt(KEY_DURATION, 0);
        }
        return 0;
    }

}
