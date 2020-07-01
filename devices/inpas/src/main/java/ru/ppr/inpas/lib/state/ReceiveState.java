package ru.ppr.inpas.lib.state;

import android.support.annotation.NonNull;

import ru.ppr.inpas.lib.internal.PosCommunicator;
import ru.ppr.inpas.lib.internal.PosContext;
import ru.ppr.inpas.lib.logger.InpasLogger;

/**
 * Класс пля обработки состояния приема данных от терминала.
 */
public class ReceiveState implements IPosState {
    private static final String TAG = InpasLogger.makeTag(ReceiveState.class);

    private final PosContext mContext;

    public ReceiveState(@NonNull final PosContext context) {
        mContext = context;
    }

    /**
     * Метод для выполнения конктретного дейтсвия, согласно текущему состоянию.
     */
    @Override
    public void doAction() {
        InpasLogger.info(TAG, "ReceiveState.");
        final PosCommunicator posCommunicator = mContext.getPosCommunicator();

        if (!posCommunicator.isReceiveTimeout()) {
            final byte[] data = posCommunicator.receive();

            if (data.length > 0) {
                mContext.getMessageConstructor().append(data);
                mContext.getStateManager().setNextState(PosState.PARSE);
            } else {
                mContext.getStateManager().setNextState(PosState.RECEIVE);
            }
        }
    }

}