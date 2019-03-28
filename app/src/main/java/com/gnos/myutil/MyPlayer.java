package com.gnos.myutil;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * 音乐播放类
 *
 */
public class MyPlayer {

    // 音效数组索引
    public final static int INDEX_TONE_ENTER = 0;
    public final static int INDEX_TONE_CANCEL = 1;

    // 音效的文件名
    private final static String[] TONE_NAMES = {
            "enter.mp3","cancel.mp3"};

    // 音效
    private static MediaPlayer[] mToneMediaPlayer = new MediaPlayer[TONE_NAMES.length];

    // 歌曲播放
    private static MediaPlayer mMusicMediaPlayer;

    // 播放音效，按钮点击的声音，无须反复加载
    public static void playTone(Context context, int index) {
        // 加载声音
        AssetManager assetManager = context.getAssets();

        if(mToneMediaPlayer[index] == null) {
            mToneMediaPlayer[index] = new MediaPlayer();

            try {
                // 打开指定音乐文件,获取assets目录下指定文件的AssetFileDescriptor对象
                AssetFileDescriptor fileDescriptor = assetManager.openFd(TONE_NAMES[index]);
                // 使用MediaPlayer加载指定的声音文件
                mToneMediaPlayer[index].setDataSource(fileDescriptor.getFileDescriptor(),
                        fileDescriptor.getStartOffset(),
                        fileDescriptor.getLength());

                // 准备声音
                mToneMediaPlayer[index].prepare();

            }catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 播放声音
        mToneMediaPlayer[index].start();
    }
    /**
     * 播放歌曲
     *
     * @param context
     * @param fileName
     */
    public static void playSong(Context context, String fileName) {
        if(mMusicMediaPlayer == null) {
            mMusicMediaPlayer = new MediaPlayer();
        }

        // 重置歌曲播放进度（针对非第一次播放）
        mMusicMediaPlayer.reset();

        // 加载声音
        AssetManager assetManager = context.getAssets();
        try {
            // 打开指定音乐文件,获取assets目录下指定文件的AssetFileDescriptor对象
            AssetFileDescriptor fileDescriptor = assetManager.openFd(fileName);
            // 使用MediaPlayer加载指定的声音文件
            mMusicMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());

            // 准备声音
            mMusicMediaPlayer.prepare();

            // 播放声音
            mMusicMediaPlayer.start();

        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void stopTheSong(Context context) {
        if(mMusicMediaPlayer != null) {
            mMusicMediaPlayer.stop();
        }
    }
}
