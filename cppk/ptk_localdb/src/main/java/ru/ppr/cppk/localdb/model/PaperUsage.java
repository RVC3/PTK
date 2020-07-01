package ru.ppr.cppk.localdb.model;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

/**
 * Информация о расходе билетной ленты.
 *
 * @author Aleksandr Brazhkin
 */
public class PaperUsage implements LocalModelWithId<Long> {
    /**
     * Id среза по смене
     */
    public static final long ID_SHIFT = 0;
    /**
     * Id среза по бобине
     */
    public static final long ID_TAPE = 1;

    /**
     * Id записи (среза)
     */
    private Long id;
    /**
     * Предыдущие показания одометра
     */
    private long prevOdometerValue;
    /**
     * Длина израсходованной ленты по срезу
     */
    private long paperLength;
    /**
     * Флаг, что счетчик был сброшен
     */
    private boolean isRestarted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getPrevOdometerValue() {
        return prevOdometerValue;
    }

    public void setPrevOdometerValue(long prevOdometerValue) {
        this.prevOdometerValue = prevOdometerValue;
    }

    public long getPaperLength() {
        return paperLength;
    }

    public void setPaperLength(long paperLength) {
        this.paperLength = paperLength;
    }

    public boolean isRestarted() {
        return isRestarted;
    }

    public void setRestarted(boolean restarted) {
        isRestarted = restarted;
    }

    @Override
    public String toString() {
        return "PaperUsage{" +
                "id=" + id +
                ", prevOdometerValue=" + prevOdometerValue +
                ", paperLength=" + paperLength +
                ", isRestarted=" + isRestarted +
                '}';
    }
}
