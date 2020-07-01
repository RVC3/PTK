package ru.ppr.cppk.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;

import java.io.IOException;

import ru.ppr.logger.Logger;

public class PlaySound {

    private static final String TAG = "PlaySound";
    private static final int MAX_SOUND_STREAMS = 1;

    private SoundPool mSoundPool = null;
    private Context mContext = null;

    public PlaySound(Context context) {
        mContext = context;
        mSoundPool = new SoundPool(MAX_SOUND_STREAMS, AudioManager.STREAM_MUSIC, 0);
    }

    public int initSound(String fileName) {

        int soundDescriptor = -1;
        AssetFileDescriptor afd = null;

        try {
            afd = mContext.getAssets().openFd(fileName);
        } catch (IOException e) {
            Logger.info(TAG, e.getMessage());
        }
        soundDescriptor = mSoundPool.load(afd, 1);
        return soundDescriptor;
    }

    public void playSound(int soundDescriptor) {
        if (soundDescriptor > 0)
            mSoundPool.play(soundDescriptor, 1, 1, 1, 0, 1);
    }

    public void unloadSounf(int soundDescriptor) {
        if (soundDescriptor > 0)
            mSoundPool.unload(soundDescriptor);
    }

    public void stopSound(int soundDescriptor) {
        if (soundDescriptor > 0)
            mSoundPool.stop(soundDescriptor);
    }

}
