package ru.ppr.core.manager;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.core.domain.model.RfidType;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.image.RfidImage;
import ru.ppr.rfidreal.RfidReal;
import ru.ppr.rfidreal.RfidReal_9000S;

/**
 * Менеджер считывателя RFID.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class RfidManager {

    private static final String TAG = Logger.makeLogTag(RfidManager.class);

    public static final ExecutorService RFID_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(r -> new Thread(r, "rfid-thread"));
    public String getDeviceModel(){return android.os.Build.MODEL;}

    private final Context context;
    private final ScreenManager screenManager;
    private final ConfigProvider configProvider;
    private IRfid rfid;

    @Inject
    public RfidManager(Context context,
                       ScreenManager screenManager,
                       ConfigProvider configProvider) {
        this.context = context;
        this.screenManager = screenManager;
        this.configProvider = configProvider;
        screenManager.addScreenStateListener(screenStateListener);

        updateRfid(configProvider.getDefaultRfidType());

        Logger.trace(TAG, "RfidManager initialized");
    }

    private void destroy() {
        screenManager.removeScreenStateListener(screenStateListener);
        Logger.trace(TAG, "RfidManager destroyed");
    }

    public IRfid getRfid() {
        return rfid;
    }

    public void updateRfid(RfidType rfidType) {
        try {
            if (rfid != null) {
                rfid.close();
                rfid = null;
            }

            Logger.trace(TAG, "updateRfid(), mode = " + rfidType);

            switch (rfidType) {
                case FILE:
                    rfid = new RfidImage(configProvider.getRfidImageConfig());
                    break;
                case REAL:
                    if (!getDeviceModel().equals("i9000S"))
                        rfid = new RfidReal(context, configProvider.getRfidRealConfig());
                    else rfid = new RfidReal_9000S(context, configProvider.getRfidRealConfig());
                    break;
                default:
                    throw new IllegalStateException("Incorrect RFID type");
            }
        } catch (Exception e) {
            Logger.error(TAG, e);
            throw new IllegalStateException("Can not create instance rfid");
        }
    }

    private ScreenManager.ScreenStateListener screenStateListener = screenOn -> {
        if (!screenOn) {
            if (rfid != null) {
                RFID_EXECUTOR_SERVICE.execute(() -> getRfid().close());
            }
        }
    };

    /**
     * Провайдер конфигурации для {@link RfidManager}.
     */
    public interface ConfigProvider {
        RfidReal.Config getRfidRealConfig();

        RfidImage.Config getRfidImageConfig();

        RfidType getDefaultRfidType();
    }
}
