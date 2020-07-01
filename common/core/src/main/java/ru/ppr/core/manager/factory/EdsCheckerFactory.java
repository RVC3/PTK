package ru.ppr.core.manager.factory;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.manager.IBluetoothManager;
import ru.ppr.core.manager.eds.EdsConfig;
import ru.ppr.core.domain.model.EdsType;
import ru.ppr.core.manager.handler.SftEdsCheckerBluetoothHandler;
import ru.ppr.core.manager.eds.EdsDirs;
import ru.ppr.edssft.SftEdsChecker;
import ru.ppr.edssft.real.RealSftEdsChecker;
import ru.ppr.edssft.stub.StubSftEdsChecker;
import ru.ppr.logger.Logger;

/**
 * Фабрика контроллера ЭЦП.
 *
 * @author Aleksandr Brazhkin
 */
public class EdsCheckerFactory implements IEdsCheckerFactory {

    private final String TAG = Logger.makeLogTag(EdsCheckerFactory.class);

    private final Context context;
    private final IBluetoothManager bluetoothManager;

    @Inject
    public EdsCheckerFactory(@NonNull final Context context, @NonNull final IBluetoothManager bluetoothManager) {
        this.context = context;
        this.bluetoothManager = bluetoothManager;
    }

    @Override
    public SftEdsChecker getEdsChecker(@NonNull final EdsConfig edsConfig) {

        EdsType edsType = edsConfig.edsType();
        if (edsType == null) {
            throw new IllegalStateException("Eds type is null");
        }
        if (edsType == EdsType.SFT) edsType = EdsType.STUB;
        switch (edsType) {
            case STUB: {
                SftEdsChecker edsChecker = new StubSftEdsChecker(edsConfig.deviceId());
                Logger.trace(TAG, "Stub eds checker created");
                return edsChecker;
            }
            case SFT: {
                EdsDirs edsDirs = edsConfig.edsDirs();
                SftEdsChecker edsChecker = new RealSftEdsChecker(context,
                        new SftEdsCheckerBluetoothHandler(bluetoothManager),
                        edsConfig.deviceId(),
                        edsDirs.getEdsWorkingDir(),
                        edsDirs.getEdsTransportDir(),
                        edsDirs.getEdsTransportInDir(),
                        edsDirs.getEdsTransportOutDir(),
                        edsDirs.getEdsUtilDir(),
                        edsDirs.getEdsUtilSrcDir(),
                        edsDirs.getEdsUtilDstDir()
                );
                Logger.trace(TAG, "Sft eds checker created");
                return edsChecker;
            }
            default: {
                throw new IllegalArgumentException("Eds type " + edsType + " is not supported");
            }
        }
    }

}
