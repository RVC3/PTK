package ru.ppr.inpas.lib.state;

import android.support.annotation.NonNull;

import ru.ppr.inpas.lib.command.custom.CustomCommandFactory;
import ru.ppr.inpas.lib.command.custom.ICustomCommand;
import ru.ppr.inpas.lib.internal.PosContext;
import ru.ppr.inpas.lib.logger.InpasLogger;
import ru.ppr.inpas.lib.protocol.SaPacket;

/**
 * Класс для обработки комманд.
 */
public class ExecuteState implements IPosState {
    private static final String TAG = InpasLogger.makeTag(ExecuteState.class);

    private final PosContext mContext;

    public ExecuteState(@NonNull final PosContext context) {
        mContext = context;
    }

    /**
     * Метод для выполнения конктретного дейтсвия, согласно текущему состоянию.
     */
    @Override
    public void doAction() {
        InpasLogger.info(TAG, "ExecuteState.");

        final SaPacket request = mContext.getRequest();

        if (SaPacket.isValid(request)) {
            final ICustomCommand cmd = CustomCommandFactory.getCustomCommand(
                    request, mContext.getNetworkCommunicator()
            );

            if (cmd != null) {
                try {
                    cmd.execute();
                    mContext.getStateManager().setNextState(PosState.SEND);
                } catch (Exception ex) {
                    mContext.getStateManager().setNextState(PosState.ERROR);
                    InpasLogger.error(TAG, "Command Action: Error while executing a command.");
                }

                mContext.add(cmd.getResult());
            } else {
                mContext.getStateManager().setNextState(PosState.DONE);
                InpasLogger.error(TAG, "Command State: command not found.");
                mContext.add(request);
            }
        } else {
            mContext.getStateManager().setNextState(PosState.ERROR);
            InpasLogger.error(TAG, "Execute State: SA packet is empty.");
        }
    }

}