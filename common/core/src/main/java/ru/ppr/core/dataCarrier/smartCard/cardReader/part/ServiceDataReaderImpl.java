package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseClassicCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataDecoder;
import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер служебных данных.
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceDataReaderImpl extends BaseClassicCardReader implements ServiceDataReader {

    private static final String TAG = Logger.makeLogTag(ServiceDataReaderImpl.class);

    private final ServiceDataDecoderFactory serviceDataDecoderFactory;

    /**
     * Кеш.
     * Используется {@link Map}, потому что на авторизационных картах может быть записано 2 блока служебных данных одновременно.
     */
    private Map<Pair<Byte, Byte>, byte[]> cachedServiceDataMap = new HashMap<>();

    public ServiceDataReaderImpl(IRfid rfid,
                                 CardInfo cardInfo,
                                 StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                 SamAuthorizationStrategy samAuthorizationStrategy,
                                 MifareClassicReader mifareClassicReader,
                                 ServiceDataDecoderFactory serviceDataDecoderFactory) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
        this.serviceDataDecoderFactory = serviceDataDecoderFactory;
    }

    @NonNull
    @Override
    public ReadCardResult<ServiceData> readServiceData(byte sectorNumber, byte blockNumber) {

        ReadCardResult<byte[]> rawResult = readRawServiceData(sectorNumber, blockNumber);

        ReadCardResult<ServiceData> result;

        if (rawResult.isSuccess()) {
            ServiceDataDecoder serviceDataDecoder = serviceDataDecoderFactory.create(rawResult.getData());
            ServiceData serviceData = serviceDataDecoder.decode(rawResult.getData());
            result = new ReadCardResult<>(serviceData);
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }

        return result;
    }

    @Override
    public ReadCardResult<byte[]> readRawServiceData(byte sectorNumber, byte blockNumber) {

        Pair<Byte, Byte> key = new Pair<>(sectorNumber, blockNumber);
        byte[] cachedServiceData = cachedServiceDataMap.get(key);
        if (cachedServiceData != null) {
            return new ReadCardResult<>(cachedServiceData);
        }

        ReadCardResult<byte[]> rawResult = readBlock(sectorNumber, blockNumber);

        ReadCardResult<byte[]> result;

        if (rawResult.isSuccess()) {
            cachedServiceData = rawResult.getData();
            cachedServiceDataMap.put(key, cachedServiceData);
            result = new ReadCardResult<>(cachedServiceData);
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }

        return result;
    }
}
