package ru.ppr.chit.api.response;

import ru.ppr.chit.api.entity.TrainInfoEntity;

/**
 * Ответ на запрос получения информации о нити поезда
 *
 * @author Dmitry Nevolin
 */
public class GetTrainInfoResponse extends BaseResponse {

    /**
     * Информация о ните поезда
     */
    private TrainInfoEntity trainInfo;

    public TrainInfoEntity getTrainInfo() {
        return trainInfo;
    }

    public void setTrainInfo(TrainInfoEntity trainInfo) {
        this.trainInfo = trainInfo;
    }

}
