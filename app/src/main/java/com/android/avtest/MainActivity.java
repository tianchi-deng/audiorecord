package com.android.avtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int MY_PERMISSIONS_REQUET = 1001;
    private static final String TAG = "MainActivity";
//    list of permissions
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private List<String> mPermissionList = new ArrayList<>();
    private Button mCancel;
    private Button mStartBtn;
    private Button mCompleteBtn;
    private Chronometer mTime;

    private AudioRecord mAudioRecord;
    private AudioTrack mAudioTrack;
    private byte[] mAudioData;
    private FileInputStream mFileInputStream;

    private AudioRecorder mAudioRecorder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        checkPermission();
    }

    private void init() {
        mCancel = findViewById(R.id.cancel);
        mStartBtn = findViewById(R.id.start);
        mCompleteBtn = findViewById(R.id.complete);
        mTime = findViewById(R.id.time);
        mAudioRecorder = AudioRecorder.getInstance();
//        mCancel.setOnClickListener(this);
        mCancel.setVisibility(View.GONE);
        mCompleteBtn.setVisibility(View.GONE);
        mStartBtn.setOnClickListener(this);
//        mCompleteBtn.setOnClickListener(this);
        mTime.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                //如果计时到现在超过了一小时秒
                if ( SystemClock.elapsedRealtime() - mTime.getBase() > 3600 * 1000) {
                    mTime.stop();
                }
            }
        });
    }



    private long sign = 0;
    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.cancel:

                break;
            case R.id.start:
//                mAudioRecorder.createAudioRecord();

                if (mAudioRecorder.getStatus() == RecordStatus.STATUS_READY) {
                    mAudioRecorder.startRecord();
                    mStartBtn.setText("Pause");
                    mCompleteBtn.setVisibility(View.VISIBLE);
                    mCompleteBtn.setOnClickListener(this);
                    mTime.setBase(SystemClock.elapsedRealtime());
                    mTime.start();
                }else if (mAudioRecorder.getStatus() == RecordStatus.STATUS_START) {
                    mAudioRecorder.pauseRecord();
                    mStartBtn.setText("continue");
                    Log.e(TAG, "getBase 1 "+mTime.getBase() );
                    sign = SystemClock.elapsedRealtime();
                    mTime.stop();
                }
                else if (mAudioRecorder.getStatus() == RecordStatus.STATUS_PAUSE) {
                    mAudioRecorder.startRecord();
                    mStartBtn.setText("Pause");
                    Log.e(TAG, "getBase 2 "+mTime.getBase() +" "+sign);
                    Log.e(TAG, "getBase 3 "+SystemClock.elapsedRealtime() );

                    mTime.setBase(mTime.getBase()+(SystemClock.elapsedRealtime()-sign));
                    mTime.start();
                }
                break;
            case R.id.complete:
                mAudioRecorder.stopRecord();
                mCompleteBtn.setVisibility(View.GONE);
                mStartBtn.setText("Start");
                mTime.stop();
                mTime.setBase(SystemClock.elapsedRealtime() );
                sign = 0;
                break;
        }
    }

    private void checkPermission(){
        for (int i = 0;i < permissions.length;i++){
           if (ContextCompat.checkSelfPermission(MainActivity.this,permissions[i]) != PackageManager.PERMISSION_GRANTED) {
               mPermissionList.add(permissions[i]);
            }
        }
        if (!mPermissionList.isEmpty()){
            permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                else {
                    Toast.makeText(this,"You denied the Permissions", Toast.LENGTH_SHORT);
                }
                break;
                default:
        }
    }


}
