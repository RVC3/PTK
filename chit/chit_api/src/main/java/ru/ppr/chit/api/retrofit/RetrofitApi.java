package ru.ppr.chit.api.retrofit;

import java.io.InputStream;

import io.reactivex.Single;
import ru.ppr.chit.api.Api;
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
 * Реализация сервиса посадки пассажиров с использованием ретрофит и реального сервера,
 * ввиду особенностей ретрофита является просей для RetrofitPerformer
 *
 * @author Dmitry Nevolin
 */
class RetrofitApi implements Api {

    /**
     * Фактическая реализация
     */
    private final RetrofitPerformer api;

    RetrofitApi(RetrofitPerformer api) {
        this.api = api;
    }

    @Override
    public Single<PingResponse> ping(PingRequest request) {
        return api.ping();
    }

    @Override
    public Single<GetTicketListResponse> getTicketList() {
        return api.getTicketList();
    }

    @Override
    public Single<GetTrainInfoResponse> getCurrentTrainInfo() {
        return api.getCurrentTrainInfo();
    }

    @Override
    public Single<PushBoardingListResponse> pushBoardingList(PushBoardingListRequest request) {
        return api.pushBoardingList(request);
    }

    @Override
    public Single<BoardingCompleteResponse> boardingComplete(BoardingCompleteRequest request) {
        return api.boardingComplete(request);
    }

    @Override
    public Single<GetBoardingListResponse> getBoardingList(GetBoardingListRequest request) {
        return api.getBoardingList(request);
    }

    @Override
    public Single<PreparePacketRdsResponse> preparePacketRds(PreparePacketRdsRequest request) {
        return api.preparePacketRds(request);
    }

    @Override
    public Single<PreparePacketSecurityResponse> preparePacketSecurity(PreparePacketSecurityRequest request) {
        return api.preparePacketSecurity(request);
    }

    @Override
    public Single<PreparePacketSoftwareResponse> preparePacketSoftware(PreparePacketSoftwareRequest request) {
        return api.preparePacketSoftware(request);
    }

    @Override
    public Single<PreparePacketSftLicenseResponse> preparePacketSftLicense(PreparePacketSftLicenseRequest request) {
        return api.preparePacketSftLicense(request);
    }

    @Override
    public Single<PreparePacketSftDataResponse> preparePacketSftData(PreparePacketSftDataRequest request) {
        return api.preparePacketSftData(request);
    }

    @Override
    public Single<GetPacketStatusResponse> getPacketStatus(GetPacketStatusRequest request) {
        return api.getPacketStatus(request);
    }

    @Override
    public Single<InputStream> getPacketChunk(GetPacketChunkRequest request) {
        return api.getPacketChunk(request)
                .flatMap(responseBody -> Single.just(responseBody.byteStream()));
    }

    @Override
    public Single<BaseResponse> unregister() {
        return api.unregister();
    }

}
