package ru.ppr.chit.ui.activity.readbsqrcode.authInfoReader.barcode;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import javax.inject.Inject;

import io.reactivex.Single;
import ru.ppr.barcodereal.BarcodeReaderMDI3100;
import ru.ppr.chit.barcode.ReadBarcodeInteractor;
import ru.ppr.chit.domain.model.local.AuthInfo;
import ru.ppr.chit.ui.activity.readbsqrcode.authInfoReader.AuthInfoReader;
import ru.ppr.core.dataCarrier.paper.barcodeReader.ReadBarcodeResult;
import ru.ppr.core.dataCarrier.readbarcodetask.BarcodeNotReadException;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;

/**
 * Ридер аутентификационных данных.
 * Стандартная реализация, получающая данные из ШК.
 *
 * @author Aleksandr Brazhkin
 */
public class BarcodeAuthInfoReader implements AuthInfoReader {

    private static final String TAG = Logger.makeLogTag(BarcodeAuthInfoReader.class);

    private final ReadBarcodeInteractor readBarcodeInteractor;

    @Inject
    BarcodeAuthInfoReader(ReadBarcodeInteractor readBarcodeInteractor) {
        this.readBarcodeInteractor = readBarcodeInteractor;
    }

    @Override
    public Single<AuthInfo> readAuthInfo() {
        return readBarcodeInteractor
                .readBarcode()
                .flatMap(barcodeReader -> Single.fromCallable(() -> {
                    ReadBarcodeResult<byte[]> readBarcodeResult = barcodeReader.readData();
                    Logger.trace(TAG, "readBarcodeResult = " + readBarcodeResult);
                    AuthInfo authInfo = null;
                    if (readBarcodeResult.isSuccess() && readBarcodeResult.getData() != null) {
                        try {
                            authInfo = decode(readBarcodeResult.getData());
                        } catch (Exception e) {
                            throw new BarcodeNotReadException(e);
                        }
                    }
                    if (authInfo == null) {
                        throw new BarcodeNotReadException("AuthInfo is null");
                    } else {
                        return authInfo;
                    }
                }));
    }

    private AuthInfo decode(byte[] data) throws JSONException, UnsupportedEncodingException {
        Logger.trace(TAG, "decode, data = " + CommonUtils.bytesToHexWithoutSpaces(data));

        String jsonObject = fixData(new String(data));
        Logger.trace(TAG, "decode, jsonObject = " + jsonObject);

        AuthInfo authInfo = new AuthInfo();

        JSONObject json = new JSONObject(jsonObject);
        authInfo.setTerminalId(json.getLong("Id"));
        authInfo.setBaseStationId(json.getString("BaseStationId"));
        authInfo.setBaseUri(json.getString("Uri"));
        authInfo.setAuthorizationCode(json.getString("AuthorizationCode"));
        authInfo.setClientId(json.getString("ClientId"));
        authInfo.setClientSecret(json.getString("ClientSecret"));
        authInfo.setSerialNumber(json.getString("SN"));
        authInfo.setThumbprint(json.getString("TP"));

        return authInfo;
    }

    /**
     * Добавляет закрывающую скобку в конец строки, если её нет.
     * Проблема порождена нижними слоями сканера ШК.
     * Метод {@link BarcodeReaderMDI3100#sendData(byte[])} выполняет обрезку крайнего правого байта.
     * Нужно это для того, чтобы получать валидный ШК Pdf417.
     * Но, с QR-кодом выяснилось, что для него Coppernic лишнего байта не добавляет.
     * Так потерялась фигурная скобка.
     * Нужно выяснить, сможет ли Coppernic пофиксить проблему у себя, тогда сможем просто удалить оба костыля.
     *
     * @param barcodeJson Json с ШК
     * @return Скорректированный Json
     */
    private String fixData(@NonNull String barcodeJson) {
        if (barcodeJson.isEmpty() || barcodeJson.endsWith("}")) {
            return barcodeJson;
        } else {
            return barcodeJson + "}";
        }
    }
}
