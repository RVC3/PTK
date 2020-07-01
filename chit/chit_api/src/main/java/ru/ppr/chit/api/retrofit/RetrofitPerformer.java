package ru.ppr.chit.api.retrofit;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
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
 * Фактическая реализация сервиса посадки пассажиров, не может быть унаследована явно от {@link ru.ppr.chit.api.Api},
 * ввиду особенностей ретрофита, документацию к методам смотреть в {@link ru.ppr.chit.api.Api}, копировать её сюда смысла нет
 *
 * @author Dmitry Nevolin
 */
interface RetrofitPerformer {

    @GET("terminal/ping")
    Single<PingResponse> ping();

    @GET("terminal/getcurrenttraininfo")
    Single<GetTrainInfoResponse> getCurrentTrainInfo();

    @GET("terminal/getticketlist")
    Single<GetTicketListResponse> getTicketList();

    @POST("terminal/pushboardinglist")
    Single<PushBoardingListResponse> pushBoardingList(@Body PushBoardingListRequest request);

    @POST("terminal/boardingcompleted")
    Single<BoardingCompleteResponse> boardingComplete(@Body BoardingCompleteRequest request);

    @POST("terminal/getboardinglist")
    Single<GetBoardingListResponse> getBoardingList(@Body GetBoardingListRequest request);

    @POST("terminal/preparepacketrds")
    Single<PreparePacketRdsResponse> preparePacketRds(@Body PreparePacketRdsRequest request);

    @POST("terminal/preparepacketsecurity")
    Single<PreparePacketSecurityResponse> preparePacketSecurity(@Body PreparePacketSecurityRequest request);

    @POST("terminal/preparepacketsoftware")
    Single<PreparePacketSoftwareResponse> preparePacketSoftware(@Body PreparePacketSoftwareRequest request);

    @POST("terminal/preparepacketsftlicense")
    Single<PreparePacketSftLicenseResponse> preparePacketSftLicense(@Body PreparePacketSftLicenseRequest request);

    @POST("terminal/preparepacketsftdata")
    Single<PreparePacketSftDataResponse> preparePacketSftData(@Body PreparePacketSftDataRequest request);

    @POST("terminal/getpacketstatus")
    Single<GetPacketStatusResponse> getPacketStatus(@Body GetPacketStatusRequest request);

    @POST("terminal/getpacketchunk")
    @Streaming
    Single<ResponseBody> getPacketChunk(@Body GetPacketChunkRequest request);

    @POST("terminal/unregister")
    Single<BaseResponse> unregister();

}
