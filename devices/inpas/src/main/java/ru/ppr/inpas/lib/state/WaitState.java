package ru.ppr.inpas.lib.state;

import android.support.annotation.NonNull;

import ru.ppr.inpas.lib.internal.PosContext;
import ru.ppr.inpas.lib.logger.InpasLogger;

/**
 * Класс для обработки состояния ожидания.
 */
public class WaitState implements IPosState {
    private static final String TAG = InpasLogger.makeTag(WaitState.class);

    private final PosContext mContext;

    public WaitState(@NonNull final PosContext context) {
        mContext = context;
    }

    /**
     * Метод для выполнения конктретного дейтсвия, согласно текущему состоянию.
     */
    @Override
    public void doAction() {
        InpasLogger.info(TAG, "WaitState.");
        mContext.getPosCommunicator().delay(500L);

        if (mContext.getMessageConstructor().hasMessage()) {
            mContext.getStateManager().setNextState(PosState.PARSE);
        } else {
            mContext.getStateManager().setNextState(PosState.RECEIVE);
        }
    }

}