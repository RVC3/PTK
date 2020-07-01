package ru.ppr.edssft.model;

import ru.ppr.edssft.SftEdsChecker;

/**
 * Результат запроса состония SFT.
 *
 * @author Aleksandr Brazhkin
 */
public class GetStateResult {
    /**
     * Флаг успешности выполнения операции
     */
    private boolean successful;
    /**
     * Описание результата
     */
    @SftEdsChecker.State
    private int state;

    public GetStateResult() {

    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    @SftEdsChecker.State
    public int getState() {
        return state;
    }

    public void setState(@SftEdsChecker.State int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "GetStateResult{" +
                "successful=" + successful +
                ", state=" + state +
                '}';
    }
}
