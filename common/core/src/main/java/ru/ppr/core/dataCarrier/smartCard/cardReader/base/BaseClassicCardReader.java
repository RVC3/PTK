package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import android.support.annotation.NonNull;
import android.util.Pair;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.entity.BscType;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Базовый класс для ридеров смарт-карт Mifare Classic.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BaseClassicCardReader extends BaseCardReader implements MifareClassicReader {

    /**
     * Алгоритм авторизации в секторах карт Mifare Classic c использованием конкретных ключей.
     */
    protected final StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy;
    /**
     * Алгоритм авторизации в секторах карт Mifare Classic c использованием SAM-модуля.
     */
    protected final SamAuthorizationStrategy samAuthorizationStrategy;

    private final MifareClassicReader mifareClassicReader;

    public BaseClassicCardReader(IRfid rfid,
                                 CardInfo cardInfo,
                                 StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                 SamAuthorizationStrategy samAuthorizationStrategy,
                                 MifareClassicReader mifareClassicReader) {
        super(rfid, cardInfo);
        this.staticKeyAuthorizationStrategy = staticKeyAuthorizationStrategy;
        this.samAuthorizationStrategy = samAuthorizationStrategy;
        this.mifareClassicReader = mifareClassicReader;

        if (samAuthorizationStrategy == null && staticKeyAuthorizationStrategy == null) {
            throw new IllegalArgumentException(" One of samAuthorizationStrategy, staticKeyAuthorizationStrategy shouldn't be null");
        }
    }


    @NonNull
    @Override
    public ReadCardResult<byte[]> readBlock(int sectorNumber, int blockNumber) {
        return mifareClassicReader.readBlock(sectorNumber, blockNumber);
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readBlocks(int startSectorNumber, int startBlockNumber, int blockCount) {
        return mifareClassicReader.readBlocks(startSectorNumber, startBlockNumber, blockCount);
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readBlockWithCardType() {
        return mifareClassicReader.readBlockWithCardType();
    }

    @NonNull
    @Override
    public WriteCardResult writeBlock(byte[] data, int sectorNumber, int blockNumber) {
        return mifareClassicReader.writeBlock(data, sectorNumber, blockNumber);
    }

    @NonNull
    @Override
    public WriteCardResult writeBlocks(byte[] data, int startSectorNumber, int startBlockNumber) {
        return mifareClassicReader.writeBlocks(data, startSectorNumber, startBlockNumber);
    }

    @NonNull
    @Override
    public ReadCardResult<Pair<BscType, byte[]>> readBscTypeWithRawData() {
        return mifareClassicReader.readBscTypeWithRawData();
    }
}
