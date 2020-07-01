package ru.ppr.cppk.Sounds;

import android.support.annotation.Nullable;

import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Sounds.Ringtone.BeepType;
import ru.ppr.logger.Logger;
import ru.ppr.cppk.settings.SharedPreferencesUtils;

/**
 * Класс производит проигрование рингтонов при удачной/неудачной попытке считать ПД
 *
 * @author A.Ushakov
 */
public class BeepPlayer {

    private static final String TAG = "BeepPlayer";

    private volatile static BeepPlayer INSTANCE = null;

    private BeepRingtone successRingtone;
    private BeepRingtone failRigtone;
    private final Globals globals;

    private BeepPlayer(Globals globals) {
        this.globals = globals;

        //загружаем рингтоны
        loadBeep(BeepType.SUCCES_BEEP);
        loadBeep(BeepType.FAIL_BEEP);
    }

    public static BeepPlayer getInstance( Globals globals) {
        BeepPlayer localPlayer = INSTANCE;
        if (localPlayer == null) {
            synchronized (BeepPlayer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BeepPlayer(globals);
                }
            }
            return INSTANCE;
        }
        return localPlayer;
    }

    /**
     * Воспроизводит рингтон при удачном считывании
     */
    public void playSuccessBeep() {
        if (SharedPreferencesUtils.isSoundOnReadBskSuccesEnable(globals))
            playBeep(BeepType.SUCCES_BEEP);
    }

    /**
     * Воспроизводит рингтон при неудачном считывании
     */
    public void playFailBeep() {

        if (SharedPreferencesUtils.isSoundReadBskErrorEnabled(globals))
            playBeep(BeepType.FAIL_BEEP);
    }

    /**
     * Воспроизводит рингтон
     *
     * @param beepType тип рингнтона(удачный/неудачный)
     */
    private void playBeep(@Nullable BeepType beepType) {

        Logger.info(TAG, "play beep");

        if (beepType == null) {
            Logger.info(TAG, "beep type is null, error play beep");
            return;
        }

        BeepRingtone ringtone = null;

        switch (beepType) {
            case FAIL_BEEP:
                ringtone = failRigtone;
                break;

            case SUCCES_BEEP:
                ringtone = successRingtone;
                break;

            default:
                break;
        }

        if (ringtone != null) {
            ringtone.play();
        } else {
            Logger.info(TAG, "Ringtone for type " + beepType.getTypeValue() + " is null");
        }
    }

    /**
     * Производит загрузку нового рингтона в память
     *
     * @param beepType
     */
    public void loadBeep(@Nullable BeepType beepType) {

        if (beepType == null) {
            Logger.info(TAG, "beepType is null");
            return;
        }

        String beepName = SharedPreferencesUtils.getBeepFilename(globals, beepType);

        if (beepName == null) {
            Logger.trace(TAG, "Could not load ringtone for type " + beepType.getTypeValue() + " - file name for ringtone is null");
            return;
        }

        String pathToSound = null;
        StringBuilder stringBuilder = new StringBuilder();

        switch (beepType) {
            case FAIL_BEEP:
                pathToSound = GlobalConstants.FAIL_BEEP_PATH;
                if (failRigtone != null) {
                    failRigtone.unload();
                }
                stringBuilder.append(pathToSound).append("/").append(beepName);
                failRigtone = new BeepRingtone(globals.getPlaySound(), stringBuilder.toString());
                break;

            case SUCCES_BEEP:
                pathToSound = GlobalConstants.SUCCESS_BEEP_PATH;
                if (successRingtone != null) {
                    successRingtone.unload();
                }
                stringBuilder.append(pathToSound).append("/").append(beepName);
                successRingtone = new BeepRingtone(globals.getPlaySound(), stringBuilder.toString());
                break;

            default:
                break;
        }
    }

}
