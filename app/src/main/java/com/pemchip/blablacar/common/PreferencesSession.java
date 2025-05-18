package com.pemchip.blablacar.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesSession extends PreferenceKey{

    Context mContext;
    SharedPreferences mPreferences;
    SharedPreferences.Editor mEditor;
    private static PreferencesSession mPreferencesSession = null;
    private static final String PREFERENCE_KEY = "PREF&E$RE@N@CEKEYSK&FINA*$NCE";

    public static PreferencesSession getInstance(Context context) {
        if (mPreferencesSession == null) {
            mPreferencesSession = new PreferencesSession(context);
        }
        return mPreferencesSession;
    }

    @SuppressLint("CommitPrefEdits")
    public PreferencesSession(Context context) {
        super();
        this.mContext = context;
        mPreferences = this.mContext.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
    }

    public void saveStringData(String key, String value){
        mEditor.putString(key, value);
        mEditor.commit();
    }

    public void saveBooleanData(String key, boolean value) {
        mEditor.putBoolean(key, value);
        mEditor.commit();
    }

    public String getStringData(String urlKey){ return mPreferences.getString(urlKey, ""); }

    public boolean getBooleanData(String urlKey){ return mPreferences.getBoolean(urlKey, false); }

    public String getURL(String urlKey){ return mPreferences.getString(urlKey, ""); }

    public boolean isLoggedIn() { if(!getUserRole().isEmpty()){ return true; }else{return false; } }

    public String getUserRole() { return mPreferences.getString(USER_ROLE, ""); }

    public boolean isCustomer()
    {
        return mPreferences.getString(USER_ROLE, "").equals(PreferenceKey.CUSTOMER_ROLE);
    }

    public String getUserId() { return mPreferences.getString(USER_ID, ""); }
    public String getUserName() { return mPreferences.getString(USER_NAME, ""); }
    public String getUserGender() { return mPreferences.getString(USER_GENDER, ""); }

    public String getCurrencyCode(){ return mPreferences.getString(PreferenceKey.USER_CURRENCY,"CHF"); }

    public String getDeviceToken(){ return mPreferences.getString(PreferenceKey.FCM_TOKEN,""); }

    public void logout(){
        saveStringData(USER_ID,"");
        saveStringData(USER_ROLE,"");
        saveStringData(SECURITY_PIN,"");
    }
}
