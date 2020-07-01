package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import android.support.annotation.NonNull;
import android.util.Pair;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.findcardtask.ReadCardErrorTypeMapper;
import ru.ppr.core.dataCarrier.findcardtask.WriteCardErrorTypeMapper;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.entity.BscType;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.RfidResult;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;
import ru.ppr.rfid.WriteToCardResult;
import ru.ppr.utils.CommonUtils;

/**
 * Ридер смарт-карт Mifare Classic.
 *
 * @author Aleksandr Brazhkin
 */
public class MifareClassicReaderImpl extends BaseCardReader implements MifareClassicReader {

    private static final String TAG = Logger.makeLogTag(MifareClassicReaderImpl.class);

    private static final byte SECTOR_NUMBER = 2;
    private static final byte BLOCK_NUMBER = 0;

    /**
     * Алгоритм авторизации в секторах карт Mifare Classic c использованием конкретных ключей.
     */
    protected final StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy;
    /**
     * Алгоритм авторизации в секторах карт Mifare Classic c использованием SAM-модуля.
     */
    protected final SamAuthorizationStrategy samAuthorizationStrategy;

    private boolean useSam;

    public MifareClassicReaderImpl(IRfid rfid,
                                   CardInfo cardInfo,
                                   StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                   SamAuthorizationStrategy samAuthorizationStrategy) {
        super(rfid, cardInfo);
        this.staticKeyAuthorizationStrategy = staticKeyAuthorizationStrategy;
        this.samAuthorizationStrategy = samAuthorizationStrategy;
        useSam = true;
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readBlock(int sectorNumber, int blockNumber) {
        return readBlocks(sectorNumber, blockNumber, 1);
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readBlocks(int startSectorNumber, int startBlockNumber, int blockCount) {

        Logger.trace(TAG, "readBlocks, cardUid = " + CommonUtils.bytesToHexWithoutSpaces(cardInfo.getCardUid()) + " START");
        ReadCardResult<byte[]> result;

        RfidResult<byte[]> rfidResult;
        if (samAuthorizationStrategy == null || !useSam) {
            rfidResult = rfid.readFromClassic(startSectorNumber, startBlockNumber, blockCount, staticKeyAuthorizationStrategy);
        } else {
            rfidResult = rfid.readFromClassic(startSectorNumber, startBlockNumber, blockCount, samAuthorizationStrategy);
            if(!rfidResult.isOk()){
                rfidResult = rfid.readFromClassic(startSectorNumber, startBlockNumber, blockCount, staticKeyAuthorizationStrategy);
                if(rfidResult.isOk()){
                    useSam = false;
                }
            }
        }
        if (rfidResult.isOk()) {
            Logger.trace(TAG, "readBlocks OK - " + CommonUtils.bytesToHexWithoutSpaces(rfidResult.getResult()));
            result = new ReadCardResult<>(rfidResult.getResult());
        } else {
            result = new ReadCardResult<>(ReadCardErrorTypeMapper.map(rfidResult.getErrorType()), rfidResult.getErrorMessage());
        }

        Logger.trace(TAG, "readBlocks, cardUid = " + CommonUtils.bytesToHexWithoutSpaces(cardInfo.getCardUid()) + " FINISH res: " + result.getDescription());

        return result;
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readBlockWithCardType() {
        return readBlock(SECTOR_NUMBER, BLOCK_NUMBER);
    }

    @NonNull
    @Override
    public WriteCardResult writeBlock(byte[] data, int sectorNumber, int blockNumber) {
        return writeBlocks(data, sectorNumber, blockNumber);
    }

    @NonNull
    @Override
    public WriteCardResult writeBlocks(byte[] data, int startSectorNumber, int startBlockNumber) {
        Logger.trace(TAG, "writeBlocks: cardUID - " + CommonUtils.bytesToHexWithoutSpaces(cardInfo.getCardUid()) +
                " data - " + CommonUtils.bytesToHexWithoutSpaces(data) +
                " startSectorNumber - " + startSectorNumber +
                ", startBlockNumber " + startBlockNumber + " START");

        WriteCardResult result;

        WriteToCardResult rfidResult;
        if (samAuthorizationStrategy == null  || !useSam) {
            rfidResult = rfid.writeToClassic(startSectorNumber, startBlockNumber, data, cardInfo.getCardUid(), staticKeyAuthorizationStrategy);
        } else {
            rfidResult = rfid.writeToClassic(startSectorNumber, startBlockNumber, data, cardInfo.getCardUid(), samAuthorizationStrategy);
        }
        if (rfidResult.isOk()) {
            Logger.trace(TAG, "writeBlocks OK - " + rfidResult);
            result = new WriteCardResult();
        } else {
            result = new WriteCardResult(WriteCardErrorTypeMapper.map(rfidResult));
        }

        Logger.trace(TAG, "writeBlocks: cardUID - " + CommonUtils.bytesToHexWithoutSpaces(cardInfo.getCardUid()) +
                " data - " + CommonUtils.bytesToHexWithoutSpaces(data) +
                " startSectorNumber - " + startSectorNumber +
                ", startBlockNumber " + startBlockNumber + " FINISH res: " + result);

        return result;
    }

    @NonNull
    @Override
    public ReadCardResult<Pair<BscType, byte[]>> readBscTypeWithRawData() {
        ReadCardResult<byte[]> prevResult = readBlockWithCardType();
        if (prevResult.isSuccess()) {
            byte[] bscTypeBytes = getBytesFromData(prevResult.getData(), TYPE_BSC_START_INDEX, TYPE_BSC_BYTES_COUNT);
            BscType bscType = BscType.getByRawCode(bscTypeBytes[0]);
            Pair<BscType, byte[]> pair = new Pair<>(bscType, prevResult.getData());
            return new ReadCardResult<>(pair, prevResult.getDescription());
        } else {
            return new ReadCardResult<>(prevResult.getReadCardErrorType(), prevResult.getDescription());
        }
    }

}
