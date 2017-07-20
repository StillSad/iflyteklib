package com.megvii.ice.iflyteklib.util;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechListener;
import com.iflytek.cloud.SpeechUtility;

/**
 * Created by ICE on 2017/1/12.
 */

public class SpeachUtil {
    public static final String TAG = "Speach";
    private static Bundle mMetaData;
    //初始化
    public static void initSpeach(Application application) {
        String appKey = getMetaData(application);
//        59706703
        SpeechUtility.createUtility(application, SpeechConstant.APPID + "=" + appKey);
    }

    //语音转文字
    public static void speachInput(Context context, SpeachInputListener inputListener) {
        SpeachInput.getInstance()
                .setInputListener(inputListener)
                .in(context);
    }

    //语音转文字
    public static String getWavPath() {
        return SpeachInput.getInstance().getWavPath();
    }

    //文字转语音
    public static void speachOutput(Context context, String string) {
        SpeachOutput.getInstance().out(context, string);
    }

    //声纹检测注册
    public static void speachCheckRegister(Context context, String authId, ISpeachRegisterListener speachRegisterListener) {
        Log.d(TAG, "authId = " + authId);
        SpeachCheck speachCheck = new SpeachCheck(context);
        speachCheck.setSpeachRegisterListener(speachRegisterListener);
        speachCheck.register(authId);

    }

    //声纹检测校对
    public static void speachCheckVerify(Context context, String authId, ISpeachVerifierListener speachVerifierListener) {
        Log.d(TAG, "authId = " + authId);
        SpeachCheck speachCheck = new SpeachCheck(context);
        speachCheck.setSpeachVerifierListener(speachVerifierListener);
        speachCheck.verify(authId);
    }

    public static void delSpeachId(Context context,String authId,String textPwd) {
        SpeachCheck speachCheck = new SpeachCheck(context);
        speachCheck.performModelOperation("del",authId,textPwd);
    }

    public static String getMetaData(Application application) {
        String msg = "";
        try {
            if (mMetaData == null) {
                ApplicationInfo appInfo = application.getPackageManager().getApplicationInfo(
                        application.getPackageName(), PackageManager.GET_META_DATA);
                mMetaData = appInfo.metaData;
            }
            msg = mMetaData.getString("APP_KEY").replace("ifly_","");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } finally {
            return msg;
        }
    }

}
