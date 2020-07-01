package ru.ppr.chit.api;

import java.io.InputStream;

import io.reactivex.Single;
import retrofit2.http.GET;
import ru.ppr.chit.api.request.BoardingCompleteRequest;
import ru.ppr.chit.api.request.GetBoardingListRequest;
import ru.ppr.chit.api.request.GetPacketChunkRequest;
import ru.ppr.chit.api.request.GetPacketStatusRequest;
import ru.ppr.chit.api.request.PingRequest;
import ru.ppr.chit.api.request.PreparePacketRdsRequest;
import ru.ppr.chit.api.request.PreparePacketSecurityRequest;
import ru.ppr.chit.api.request.PreparePacketSftDataRequest;
import ru.ppr.chit.api.request.PreparePacketSftLicenseRequest;
import ru.ppr.chit.api.request.PreparePacketSoftwareRequest;
import ru.ppr.chit.api.request.PushBoardingListRequest;
import ru.ppr.chit.api.response.BaseResponse;
import ru.ppr.chit.api.response.BoardingCompleteResponse;
import ru.ppr.chit.api.response.GetBoardingListResponse;
import ru.ppr.chit.api.response.GetPacketStatusResponse;
import ru.ppr.chit.api.response.GetTicketListResponse;
import ru.ppr.chit.api.response.GetTrainInfoResponse;
import ru.ppr.chit.api.response.PingResponse;
import ru.ppr.chit.api.response.PreparePacketRdsResponse;
import ru.ppr.chit.api.response.PreparePacketSecurityResponse;
import ru.ppr.chit.api.response.PreparePacketSftDataResponse;
import ru.ppr.chit.api.response.PreparePacketSftLicenseResponse;
import ru.ppr.chit.api.response.PreparePacketSoftwareResponse;
import ru.ppr.chit.api.response.PushBoardingListResponse;

/**
 * Сервис посадки пассажиров, предоставляет АПИ для ПТК
 *
 * @author Dmitry Nevolin
 */
public interface Api {

    /**
     * Проверка сессии и подключения к Базовой станнции
     */
    Single<PingResponse> ping(PingRequest request);

    /**
     * Возвращает список билетов нити поезда
     */
    Single<GetTicketListResponse> getTicketList();

    /**
     * Возвращает информацию о текущей нити поезда, включая список станций и схемы вагонов
     */
    Single<GetTrainInfoResponse> getCurrentTrainInfo();

    /**
     * Отправить список посаженных пассажиров
     */
    Single<PushBoardingListResponse> pushBoardingList(PushBoardingListRequest request);

    /**
     * Отправить событие завершения посадки
     */
    Single<BoardingCompleteResponse> boardingComplete(BoardingCompleteRequest request);

    /**
     * Возвращает список пассажиров, посаженных посредством других терминалов
     */
    Single<GetBoardingListResponse> getBoardingList(GetBoardingListRequest request);

    /**
     * Подготовить пакет с базой НСИ
     */
    Single<PreparePacketRdsResponse> preparePacketRds(PreparePacketRdsRequest request);

    /**
     * Подготовить пакет с базой Безопасности
     */
    Single<PreparePacketSecurityResponse> preparePacketSecurity(PreparePacketSecurityRequest request);

    /**
     * Подготовить пакет с ПО
     */
    Single<PreparePacketSoftwareResponse> preparePacketSoftware(PreparePacketSoftwareRequest request);

    /**
     * Подготовить пакет с лицензиями СФТ
     */
    Single<PreparePacketSftLicenseResponse> preparePacketSftLicense(PreparePacketSftLicenseRequest request);

    /**
     * Подготовить пакет с конфигами и ключами СФТ
     */
    Single<PreparePacketSftDataResponse> preparePacketSftData(PreparePacketSftDataRequest request);

    /**
     * Получить состояние запроса
     */
    Single<GetPacketStatusResponse> getPacketStatus(GetPacketStatusRequest request);

    /**
     * Получить пакет по запросу
     */
    Single<InputStream> getPacketChunk(GetPacketChunkRequest request);

    /**
     * Отмена регистрации мобильного устройства
     */
    Single<BaseResponse> unregister();

}
