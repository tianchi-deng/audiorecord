package com.android.audiorecord;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AudioRecorder {
    private static final String TAG = "AudioRecorder";

    /**
     * 采样率，现在能够保证在所有设备上使用的采样率是44100Hz, 但是其他的采样率（22050, 16000, 11025）在一些设备上也可以使用。
     */
    public static final int SAMPLE_RATE_INHZ = 44100;
    /**
     * 声道数。CHANNEL_IN_MONO and CHANNEL_IN_STEREO. 其中CHANNEL_IN_MONO是可以保证在所有设备能够使用的。
     */
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 返回的音频数据的格式。 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT, and ENCODING_PCM_FLOAT.
     */
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord mAudioRecord;
    private static AudioRecorder mAudioRecorder;
    private int minBufferSize;
    private RecordStatus status = RecordStatus.STATUS_READY;
    private String fileName;
    private List<String> files = new ArrayList<>();
    private AudioRecorder() {}

    public static AudioRecorder getInstance() {
        if (mAudioRecorder == null) {
            mAudioRecorder = new AudioRecorder();
        }
        return mAudioRecorder;
    }

    public void createAudioRecord() {
        fileName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        Log.e(TAG, "fileName: "+fileName );
        minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        mAudioRecord =new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);
    }

    public void startRecord() {
        createAudioRecord();
        final byte data[] =new byte[minBufferSize];
        String currentFileName = fileName;
        if (status == RecordStatus.STATUS_PAUSE) {
            currentFileName += files.size();
        }
        files.add(currentFileName);
        final File file = new File(FileUtils.getPcmFileAbsolutePath(currentFileName));
        if (!file.mkdirs()) {
            Log.d(TAG, "directory not created!");
        }
        if (file.exists()) {
            file.delete();
        }
        mAudioRecord.startRecording();
        status = RecordStatus.STATUS_START;
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                }catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (os != null) {
                    while (status == RecordStatus.STATUS_START) {
                        int read = mAudioRecord.read(data, 0, minBufferSize);
                        if (AudioRecord.ERROR_INVALID_OPERATION !=read) {
                            try {
                                os.write(data);
                            }catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        Log.i(TAG, "run: close file outputStream");
                        os.close();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void pauseRecord() {
        mAudioRecord.stop();
//        mAudioRecord.release();
//        mAudioRecord = null;
        status = RecordStatus.STATUS_PAUSE;
    }

    public void stopRecord() {
        status = RecordStatus.STATUS_READY;
        if (mAudioRecord != null){
            mAudioRecord.stop();
            mAudioRecord.release();
            mergePcmFilesToWavFile();
            mAudioRecord = null;
        }
    }

    public void mergePcmFilesToWavFile() {
        try {
            if (files.size() > 0) {
                final List<String> filePaths = new ArrayList<>();
                for (String file : files){
                    filePaths.add(FileUtils.getPcmFileAbsolutePath(file));
                }
                files.clear();
                final PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "fileName: "+fileName );
                        for(String name : filePaths){
                            Log.e(TAG, "filepath " + name );
                        }
                        pcmToWavUtil.mergePcmToWav(filePaths,FileUtils.getWavFileAbsolutePath(fileName));

                        fileName = null;
                    }
                }).start();
            }

        }catch (IllegalStateException e) {
           e.printStackTrace();
        }
    }

    public RecordStatus getStatus() {
        return status;
    }
}
