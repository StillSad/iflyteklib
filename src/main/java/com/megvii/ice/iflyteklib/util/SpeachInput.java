package com.megvii.ice.iflyteklib.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;


/**
 * Created by ICE on 2017/1/12.
 */

public class SpeachInput {

    private static SpeachInput instance;
    private SpeachInputListener inputListener;
    private String pcmFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/reverseme.pcm";
    private String wavFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/demo.wav";

    private DataOutputStream dos;

    public RecognizerListener mRecoListener = new RecognizerListener() {
        //听写结果回调接口(返回Json格式结果，用户可参见附录13.1)；
        //一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
        //关于解析Json的代码可参见Demo中JsonParser类；

        //volume音量值0~30，data音频数据
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            if (inputListener != null) {
                inputListener.onVolumeChanged(volume, data);
            }

            try {
                dos.write(data, 0, data.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(SpeachUtil.TAG, "volume = " + volume + ",data = ");
        }

        //开始录音
        @Override
        public void onBeginOfSpeech() {
            if (inputListener != null) {
                inputListener.onBeginOfSpeech();
            }
            Log.d(SpeachUtil.TAG, "开始录音");
        }

        //结束录音
        @Override
        public void onEndOfSpeech() {
            StringBuffer resultBuffer = new StringBuffer();
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }
            if (inputListener != null) {
                inputListener.onEndOfSpeech(resultBuffer.toString());
                pcm2wav();
            }
            Log.d(SpeachUtil.TAG, "结束录音");
        }

        //isLast等于true时会话结束
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            if (inputListener != null) {
                inputListener.onResult(results, isLast);
            }
            printResult(results);
            Log.d(SpeachUtil.TAG, "isLast = " + isLast);
        }

        //会话发生错误回调接口
        @Override
        public void onError(SpeechError speechError) {
            if (inputListener != null) {
                inputListener.onError(speechError);
            }
        }

        //扩展用接
        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {
            if (inputListener != null) {
                inputListener.onEvent(i, i1, i2, bundle);
            }
        }
    };

    private SpeachInput() {
        //创建文件
        File file = new File(pcmFilePath);
        if (file.exists())
            file.delete();
        try {
            file.createNewFile();
            Log.d("SpeachActivity", "创建文件reverseme.pcm");
            Log.d("SpeachActivity", file.getAbsolutePath().toString());
            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create " + file.toString());
        }
    }

    public String getWavPath() {
        return wavFilePath;
    }

    public static synchronized SpeachInput getInstance() {
        if (instance == null) {
            instance = new SpeachInput();
        }
        return instance;
    }

    public SpeachInput setInputListener(SpeachInputListener listener) {
        this.inputListener = listener;
        return this;
    }

    public void in(Context context) {
        //1.创建SpeechRecognizer对象，第二个参数：本地识别时传InitListener
        SpeechRecognizer mIat = SpeechRecognizer.createRecognizer(context, null);
        //2.设置听写参数，详见《MSC Reference Manual》SpeechConstant类
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
        //3.开始听写
        int ret = mIat.startListening(mRecoListener);
        if (ret != ErrorCode.SUCCESS) {

        } else {

        }
    }

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        Log.d(SpeachUtil.TAG, resultBuffer.toString());
    }

    private void pcm2wav() {
        Pcm2Wav tool = new Pcm2Wav(new Pcm2Wav.SuccessListener() {
            @Override
            public void onSuccess() {
                if (inputListener != null) {
                    inputListener.onPcm2WavSuccess(wavFilePath);
                }

            }
        });
        try {
            tool.convertAudioFiles(pcmFilePath, wavFilePath);
        } catch (Exception e) {
        }
    }
}
