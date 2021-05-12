package com.android.audiorecord;

import android.media.AudioFormat;
import android.media.AudioManager;;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author tianchi.deng
 * @description:
 * @date :5/11/21 3:21 PM
 */
public class Pcmplayer {
    private static final String TAG = "PcmPlayer";

    private static Pcmplayer pcmplayer;
    private AudioTrack audioTrack;
    public static Pcmplayer getInstance() {
        if (pcmplayer == null) {
            pcmplayer = new Pcmplayer();
        }
        return pcmplayer;
    }
    int channelConfig = AudioFormat.CHANNEL_OUT_MONO;

    /**
    * @method  playInModeStream
    * @description MODE_STREAM下播放pcm格式音频，分批次讲数据传入缓冲区
    * @date: 5/11/21 4:03 PM
    * @author: dtc
    */
    public void playInModeStream(File file) {
        Log.e("dtc", "playInModeStream: file "+file.getPath());
        final int minBufferSize = AudioTrack.getMinBufferSize(44100, channelConfig, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
        audioTrack.play();
        try {
            final FileInputStream in = new FileInputStream(file);
            final byte[] data = new byte[minBufferSize];
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (in.available() > 0) {
                            int count = in.read(data);
                            if (count == AudioTrack.ERROR_INVALID_OPERATION || count == AudioTrack.ERROR_BAD_VALUE) {
                                continue;
                            }
                            if (count != 0 && count != -1) {
                                Log.e("dtc", "run: 1");
                                audioTrack.write(data,0,count);
                            }
                        }
                    }catch (IOException e) {e.printStackTrace();}
                }
            }).start();
        }catch(Exception e) {e.printStackTrace();}
    }

    /**
    * @method playInModeStatic
    * @description MODE_STATIC下播放音频,一次性将数据放入缓冲区，不适合大文件
    * @date: 5/11/21 4:04 PM
    * @author: dtc
    */
    private  byte[] audioData ;
    public void playInModeStatic(final File file) {
        Log.e("dtc", "playInModeStatic: file "+file.getPath());
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    InputStream in = new FileInputStream(file) ;
                    try {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        for (int b; (b = in.read()) != -1; ) {
                            out.write(b);
                        }
                        Log.d(TAG, "Got the data");
                        Log.e("dtc", "run: 2");
                        audioData = out.toByteArray();
                    } finally {
                        in.close();
                    }
                } catch (IOException e) {
                    Log.wtf(TAG, "Failed to read", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                Log.i(TAG, "Creating track...audioData.length = " + audioData.length);
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT, audioData.length, AudioTrack.MODE_STATIC);
                Log.d(TAG, "Writing audio data...");
                audioTrack.write(audioData, 0, audioData.length);
                Log.d(TAG, "Starting playback");
                audioTrack.play();
                Log.d(TAG, "Playing");
            }

        }.execute();
    }
}
