package ru.ppr.chit.api.request;

/**
 * @author Dmitry Nevolin
 */
public class BoardingCompleteRequest extends BaseRequest {

    /**
     * Текущая станция проверки
     */
    private long controlStationCode;
    /**
     * Идентификатор нити поезда
     */
    private String trainThreadId;

    public long getControlStationCode() {
        return controlStationCode;
    }

    public void setControlStationCode(long controlStationCode) {
        this.controlStationCode = controlStationCode;
    }

    public String getTrainThreadId() {
        return trainThreadId;
    }

    public void setTrainThreadId(String trainThreadId) {
        this.trainThreadId = trainThreadId;
    }

}
