package ru.ppr.cppk.logic;

import ru.ppr.core.manager.eds.EdsManagerWrapper;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.edssft.model.GetKeyInfoResult;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.logger.Logger;

/**
 * Выполняет проверку совпадает ли deviceId в локальной базе с deviceId на котором работает sft
 *
 * @author Grigoriy Kashka
 */
public class DeviceIdChecker {

    private static final String TAG = Logger.makeLogTag(CriticalNsiChecker.class);

    private final EdsManagerWrapper edsManagerWrapper;
    private final Holder<PrivateSettings> privateSettings;

    public DeviceIdChecker(EdsManagerWrapper edsManagerWrapper, Holder<PrivateSettings> privateSettings) {
        this.edsManagerWrapper = edsManagerWrapper;
        this.privateSettings = privateSettings;
    }

    public enum Result {
        SUCCESS,
        EDS_ERROR,
        INVALID_PTK_NUMBER
    }

    /**
     * Выполнит проверку корректности номера ПТК
     */
    public Result check() {
        Result out = Result.EDS_ERROR;
        SignDataResult signDataRes = edsManagerWrapper.pingEdsBlocking();
        Logger.info(TAG, "validateDeviceId() signDataRes = " + signDataRes.toString());
        if (signDataRes.isSuccessful()) {
            GetKeyInfoResult getKeyInfoRes = edsManagerWrapper.getKeyInfoBlocking(signDataRes.getEdsKeyNumber());
            Logger.info(TAG, "validateDeviceId() getKeyInfoRes = " + getKeyInfoRes.toString());
            if (getKeyInfoRes.isSuccessful()) {
                out = privateSettings.get().getTerminalNumber() == getKeyInfoRes.getDeviceId() ? Result.SUCCESS : Result.INVALID_PTK_NUMBER;
            }
        }
        Logger.trace(TAG, "validateDeviceId() FINISH result = " + out);
        return out;
    }
}
