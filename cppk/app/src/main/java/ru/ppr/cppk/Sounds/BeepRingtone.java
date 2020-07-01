package ru.ppr.cppk.Sounds;

import ru.ppr.cppk.utils.PlaySound;

public class BeepRingtone implements Ringtone {
    //
//	private static final String BEEPNAME = "Sounds/success_beep/beep-07.wav";
    private int soundID = -1;
    private PlaySound player;

    public BeepRingtone(PlaySound playSound, String filenName) {
        player = playSound;
        load(filenName);
    }

    @Override
    public void play() {
        if (soundID > 0)
            player.playSound(soundID);
    }

    @Override
    public void stop() {
        if (soundID > 0)
            player.stopSound(soundID);
    }

    public void load(String filename) {
        soundID = player.initSound(filename);
    }

    @Override
    public void unload() {
        if (soundID > 0) {
            player.unloadSounf(soundID);
            soundID = -1;
        }
    }

}
