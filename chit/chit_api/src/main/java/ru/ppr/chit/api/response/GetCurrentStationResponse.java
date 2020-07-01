package ru.ppr.chit.api.response;

/**
 * @author Dmitry Nevolin
 */
public class GetCurrentStationResponse extends BaseResponse {

    /**
     * Текущая станция стоянки поезда. Если поезд в пути то null
     */
    private Long currentStationCode;

    public Long getCurrentStationCode() {
        return currentStationCode;
    }

    public void setCurrentStationCode(Long currentStationCode) {
        this.currentStationCode = currentStationCode;
    }

}
