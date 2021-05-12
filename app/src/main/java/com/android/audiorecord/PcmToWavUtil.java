package com.android.audiorecord;

import android.media.AudioFormat;
import android.media.AudioRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PcmToWavUtil {

    /**
     * 清除文件
     *
     * @param filePathList
     */
    private static void clearFiles(List<String> filePathList) {
        for (int i = 0; i < filePathList.size(); i++) {
            File file = new File(filePathList.get(i));
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 缓存的音频大小
     */
    private int mBufferSize;
    /**
     * 采样率
     */
    private int mSampleRate;
    /**
     * 声道数
     */
    private int mChannel;

    /**
     * @param sampleRate sample rate、采样率
     * @param channel channel、声道
     * @param encoding Audio data format、音频格式
     */
    PcmToWavUtil(int sampleRate, int channel, int encoding) {
        this.mSampleRate = sampleRate;
        this.mChannel = channel;
        this.mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, encoding);
    }

    /**
     * pcm文件转wav文件
     *
     * @param inFilenames 源文件路径
     * @param outFilename 目标文件路径
     */
    public void mergePcmToWav(List<String> inFilenames, String outFilename) {
        File[] file = new File[inFilenames.size()];
        long totalAudioLen = 0;
        if (inFilenames.size()>0) {
            for (int i = 0; i < inFilenames.size(); i++) {
                file[i] = new File(inFilenames.get(i));
                totalAudioLen += file[i].length();
            }
        }
        FileInputStream in ;
        FileOutputStream out;
        long totalDataLen;
        long longSampleRate = mSampleRate;
        int channels = mChannel == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
        long byteRate = 16 * mSampleRate * channels / 8;
        byte[] data = new byte[mBufferSize];
        try {
            out = new FileOutputStream(outFilename);
            totalDataLen = totalAudioLen + 36;

            WaveHeader.writeWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            for (int i = 0; i < inFilenames.size(); i++) {  //循环读取各个pcm
                in = new FileInputStream(inFilenames.get(i));
                while (in.read(data) != -1) {
                    out.write(data);
                }
                in.close();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pcmToWav(String inFilename, String outFilename) {
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen;
        long totalDataLen;
        long longSampleRate = mSampleRate;
        int channels = mChannel == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
        long byteRate = 16 * mSampleRate * channels / 8;
        byte[] data = new byte[mBufferSize];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            WaveHeader.writeWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
