package ru.ppr.inpas.lib.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.Queue;

import ru.ppr.inpas.lib.packer.MessageConstructor;
import ru.ppr.inpas.lib.protocol.SaPacket;
import ru.ppr.inpas.lib.state.StateManager;

/**
 * Контекст POS терминала.
 */
public class PosContext {

    private final MessageConstructor mMessageConstructor = new MessageConstructor();
    private final Queue<SaPacket> mQueue = new LinkedList<>();

    private final PosCommunicator mPosCommunicator;
    private final NetworkCommunicator mNetworkCommunicator;
    private StateManager mStateManager;

    public PosContext(@NonNull final PosCommunicator posCommunicator,
                      @NonNull final NetworkCommunicator networkCommunicatorCommunicator) {
        mPosCommunicator = posCommunicator;
        mNetworkCommunicator = networkCommunicatorCommunicator;
        mStateManager = new StateManager(this);
    }

    @NonNull
    public MessageConstructor getMessageConstructor() {
        return mMessageConstructor;
    }

    /**
     * Метод возвращает коммуникатор POS терминала.
     *
     * @return коммуникатор POS терминала.
     * @see PosCommunicator
     */
    @NonNull
    public PosCommunicator getPosCommunicator() {
        return mPosCommunicator;
    }

    /**
     * Метод возвращает коммуникатор сетевой части.
     *
     * @return коммуникатор сетевой части.
     * @see NetworkCommunicator
     */
    @NonNull
    public NetworkCommunicator getNetworkCommunicator() {
        return mNetworkCommunicator;
    }

    /**
     * Метод для получения менеджера состояний терминала.
     *
     * @return менеджер состояний.
     */
    @NonNull
    public StateManager getStateManager() {
        return mStateManager;
    }

    /**
     * Метод для добавления SA пакета.
     *
     * @param packet добавляемый SA пакет.
     * @see SaPacket
     */
    public void add(@NonNull final SaPacket packet) {
        mQueue.add(packet);
    }

    /**
     * Метод возращает текущий запрос.
     *
     * @return текущий запрос.
     */
    @Nullable
    public SaPacket getRequest() {
        return mQueue.poll();
    }

    /**
     * Метод возвращает текущий результат выполнения запроса.
     *
     * @return результат выполнения запроса.
     * @see SaPacket
     */
    @Nullable
    public SaPacket getResult() {
        return mQueue.poll();
    }

}