package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import android.support.annotation.NonNull;

import java.util.Arrays;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.TroykaDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.pdTrip.PdTicketDecoder;
import ru.ppr.core.dataCarrier.smartCard.pdTrip.PdTicketDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.pdTrip.TicketMetroPd;
import ru.ppr.core.dataCarrier.smartCard.pdTroyka.PdTroykaDecoder;
import ru.ppr.core.dataCarrier.smartCard.pdTroyka.PdTroykaDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.pdTroyka.MetroPd;
import ru.ppr.core.dataCarrier.smartCard.wallet.MetroWallet;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;
import ru.ppr.utils.ByteUtils;

/**
 * Ридер карт Тройка.
 *
 * @author Aleksandr Brazhkin
 */
public class TroykaReaderImpl extends BaseClassicCardReader implements TroykaReader {

    private static final String TAG = Logger.makeLogTag(TroykaReaderImpl.class);

    private static final byte PAYLOAD_SECTOR = 6;
    private static final byte PAYLOAD_BLOCK = 0;

    private static final byte EDS_SIZE = 64;
    private static final byte BLOCK_SIZE = 16;

    private static final byte EDS_PART_1_START_SECTOR = 6;
    private static final byte EDS_PART_1_START_BLOCK = 2;
    private static final byte EDS_PART_1_BLOCK_COUNT = 1;

    private static final byte EDS_PART_2_START_SECTOR = 15;
    private static final byte EDS_PART_2_START_BLOCK = 0;
    private static final byte EDS_PART_2_BLOCK_COUNT = 3;

    private static final byte PASSAGE_MARK_SECTOR = 9;
    private static final byte PASSAGE_MARK_BLOCK = 0;

    private static final byte TICKET_SECTOR = 7;

    private static final byte WALLET_CARD_SECTOR = 8;

    private final OuterNumberReader outerNumberReader;

    public TroykaReaderImpl(IRfid rfid,
                            CardInfo cardInfo,
                            StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                            SamAuthorizationStrategy samAuthorizationStrategy,
                            MifareClassicReader mifareClassicReader,
                            OuterNumberReader outerNumberReader, PdDecoderFactory pdDecoderFactory) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
        this.outerNumberReader = outerNumberReader;
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readFirstPayloadBlock() {
        return readBlock(PAYLOAD_SECTOR, PAYLOAD_BLOCK);
    }

    @NonNull
    @Override
    public ReadCardResult<PassageMark> readPassageMark() {
        final ReadCardResult<byte[]> rawResult = readBlock(PASSAGE_MARK_SECTOR, PASSAGE_MARK_BLOCK);
        ReadCardResult<PassageMark> result;
        if (rawResult.isSuccess()) {
            Logger.trace(TAG, "Чтение метки прохода readPassageMark sector:" + PASSAGE_MARK_SECTOR +  " data:bytes - " + Arrays.toString(rawResult.getData()) + " hex:" + DataCarrierUtils.byteArrayToHex(rawResult.getData()));
            PassageMarkDecoder passageMarkDecoder = new TroykaDecoderFactory((int) PASSAGE_MARK_SECTOR).create(rawResult.getData());
            PassageMark passageMark = passageMarkDecoder.decode(rawResult.getData());
            result = new ReadCardResult<>(passageMark);
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }
        Logger.trace(TAG, "Чтение метки прохода getPassageMarkReadCardResult result:" + result);
        return result;
    }

    @NonNull
    @Override
    public ReadCardResult<MetroWallet> readWalletData() {
        int sector = WALLET_CARD_SECTOR;
        final ReadCardResult<byte[]> rawResult = readBlocks(sector, 0, 3);
        ReadCardResult<MetroWallet> result;
        if (rawResult.isSuccess()) {
            Logger.trace(TAG, "Чтение кошелька readWalletData sector:" + sector + " data:bytes - " + Arrays.toString(rawResult.getData()) + " hex:" + DataCarrierUtils.byteArrayToHex(rawResult.getData()));
            final PassageMarkDecoder decoder = new TroykaDecoderFactory(sector).create(rawResult.getData());
            final MetroWallet decode = (MetroWallet) decoder.decode(rawResult.getData());
            result = new ReadCardResult<>(decode);
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }
        Logger.trace(TAG, "Чтение кошелька ReadCardResult:" + sector + " result:" + result);
        return result;
    }

    @NonNull
    @Override
    public ReadCardResult<MetroWallet> writeWalletData() {
        // TODO реализовать запись данных кошелька на 8 сектор

        return null;
    }


    @NonNull
    @Override
    public ReadCardResult<OuterNumber> readOuterNumber() {
        return outerNumberReader.readOuterNumber();
    }

    @NonNull
    @Override
    public ReadCardResult<MetroPd> readTicketPd() {
        ReadCardResult<MetroPd> result;
        ReadCardResult<byte[]> rawResult = readBlock(TICKET_SECTOR, 0);
        if (rawResult.isSuccess()) {
            PdTroykaDecoder pdTroykaDecoder = new PdTroykaDecoderFactory().create(rawResult.getData());
            MetroPd troykaPd = pdTroykaDecoder.decode(rawResult.getData());
            result = new ReadCardResult<>(troykaPd);
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }
        return result;
    }

    @NonNull
    @Override
    public ReadCardResult<TicketMetroPd> readInformationPd(byte block) {
        ReadCardResult<TicketMetroPd> result;
        ReadCardResult<byte[]> rawResult = readBlock(TICKET_SECTOR, block);
        if (rawResult.isSuccess()) {
            PdTicketDecoder pdTicketDecoder = new PdTicketDecoderFactory().create(rawResult.getData());
            TicketMetroPd ticketPd = pdTicketDecoder.decode(rawResult.getData());
            result = new ReadCardResult<>(ticketPd);
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }
        return result;
    }


    @NonNull
    @Override
    public ReadCardResult<byte[]> readEds() {
        ReadCardResult<byte[]> part1Result = readBlocks(EDS_PART_1_START_SECTOR, EDS_PART_1_START_BLOCK, EDS_PART_1_BLOCK_COUNT);
        ReadCardResult<byte[]> result;
        if (part1Result.isSuccess()) {
            ReadCardResult<byte[]> part2Result = readBlocks(EDS_PART_2_START_SECTOR, EDS_PART_2_START_BLOCK, EDS_PART_2_BLOCK_COUNT);
            if (part2Result.isSuccess()) {
                byte[] edsData = ByteUtils.concatArrays(part1Result.getData(), part2Result.getData());
                result = new ReadCardResult<>(edsData);
            } else {
                result = new ReadCardResult<>(part2Result.getReadCardErrorType(), part2Result.getDescription());
            }
        } else {
            result = new ReadCardResult<>(part1Result.getReadCardErrorType(), part1Result.getDescription());
        }

        return result;
    }

    @NonNull
    @Override
    public WriteCardResult writeEds(byte[] eds) {
        byte[] edsPart1 = DataCarrierUtils.subArray(eds, 0, BLOCK_SIZE);
        WriteCardResult part1Result = writeBlocks(edsPart1, EDS_PART_1_START_SECTOR, EDS_PART_1_START_BLOCK);
        WriteCardResult result;
        if (part1Result.isSuccess()) {
            byte[] edsPart2 = DataCarrierUtils.subArray(eds, BLOCK_SIZE, EDS_SIZE - BLOCK_SIZE);
            result = writeBlocks(edsPart2, EDS_PART_2_START_SECTOR, EDS_PART_2_START_BLOCK);
        } else {
            result = part1Result;
        }
        return result;
    }

}
