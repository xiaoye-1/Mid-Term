package com.example.android.notepad.util;


import android.content.Context;
import android.content.SharedPreferences;
import com.example.android.notepad.application.MyApplication;



public class SharedPreferenceUtil {

    private static String FILENAME = "Config";


    public static boolean CommitDate(String key, String date) {
        SharedPreferences sp = MyApplication.getContext().getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, date);
        return editor.commit();
    }


    public static void ApplyDate(String key, String date) {
        SharedPreferences sp = MyApplication.getContext().getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, date);
        editor.apply();
    }

    /*
    *获取数据
     */
    public static String getDate(String key) {
        SharedPreferences sp = MyApplication.getContext().getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        String str = sp.getString(key, "");
        if (!str.isEmpty()) {
            return str;
        } else {
            return null;
        }
    }
}