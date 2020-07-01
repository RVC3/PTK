package ru.ppr.chit.api.stub;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

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
 * Стабовая реализация сервиса посадки пассажиров
 *
 * @author Dmitry Nevolin
 */
public class StubApi implements Api {

    private static final UUID NO_UPDATED_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Override
    public Single<PingResponse> ping(PingRequest request) {
        PingResponse response = new PingResponse();

        return Single.just(response);
    }

    @Override
    public Single<GetTicketListResponse> getTicketList() {
        GetTicketListResponse response = new GetTicketListResponse();

        return Single.just(response);
    }

    @Override
    public Single<GetTrainInfoResponse> getCurrentTrainInfo() {
        GetTrainInfoResponse response = new GetTrainInfoResponse();

        return Single.just(response);
    }

    @Override
    public Single<PushBoardingListResponse> pushBoardingList(PushBoardingListRequest request) {
        PushBoardingListResponse response = new PushBoardingListResponse();

        return Single.just(response);
    }

    @Override
    public Single<BoardingCompleteResponse> boardingComplete(BoardingCompleteRequest request) {
        BoardingCompleteResponse response = new BoardingCompleteResponse();

        return Single.just(response);
    }

    @Override
    public Single<GetBoardingListResponse> getBoardingList(GetBoardingListRequest request) {
        GetBoardingListResponse response = new GetBoardingListResponse();

        return Single.just(response);
    }

    @Override
    public Single<PreparePacketRdsResponse> preparePacketRds(PreparePacketRdsRequest request) {
        PreparePacketRdsResponse response = new PreparePacketRdsResponse();
        response.setRequestId(NO_UPDATED_UUID);

        return Single.just(response);
    }

    @Override
    public Single<PreparePacketSecurityResponse> preparePacketSecurity(PreparePacketSecurityRequest request) {
        PreparePacketSecurityResponse response = new PreparePacketSecurityResponse();
        response.setRequestId(NO_UPDATED_UUID);

        return Single.just(response);
    }

    @Override
    public Single<PreparePacketSoftwareResponse> preparePacketSoftware(PreparePacketSoftwareRequest request) {
        PreparePacketSoftwareResponse response = new PreparePacketSoftwareResponse();
        response.setRequestId(NO_UPDATED_UUID);

        return Single.just(response);
    }

    @Override
    public Single<PreparePacketSftLicenseResponse> preparePacketSftLicense(PreparePacketSftLicenseRequest request) {
        PreparePacketSftLicenseResponse response = new PreparePacketSftLicenseResponse();
        response.setRequestId(NO_UPDATED_UUID);

        return Single.just(response);
    }

    @Override
    public Single<PreparePacketSftDataResponse> preparePacketSftData(PreparePacketSftDataRequest request) {
        PreparePacketSftDataResponse response = new PreparePacketSftDataResponse();
        response.setRequestId(NO_UPDATED_UUID);

        return Single.just(response);
    }

    @Override
    public Single<GetPacketStatusResponse> getPacketStatus(GetPacketStatusRequest request) {
        GetPacketStatusResponse response = new GetPacketStatusResponse();

        return Single.just(response);
    }

    @Override
    public Single<InputStream> getPacketChunk(GetPacketChunkRequest request) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);

        return Single.just(inputStream);
    }

    @Override
    public Single<BaseResponse> unregister() {
        BaseResponse response = new BaseResponse();

        return Single.just(response);
    }

}
