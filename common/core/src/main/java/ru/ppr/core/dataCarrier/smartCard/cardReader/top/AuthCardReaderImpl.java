package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseClassicCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.CoverageAreaReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ServiceDataReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;
import ru.ppr.core.dataCarrier.smartCard.entity.AuthCardData;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;
import ru.ppr.core.dataCarrier.smartCard.parser.AuthCardDataParser;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;
import ru.ppr.utils.ByteUtils;

/**
 * Ридер авторизационных карт.
 *
 * @author Aleksandr Brazhkin
 */
public class AuthCardReaderImpl extends BaseClassicCardReader implements AuthCardReader {

    private static final String TAG = Logger.makeLogTag(AuthCardReaderImpl.class);

    private static final byte AUTH_SERVICE_DATA_SECTOR = 7;
    private static final byte AUTH_SERVICE_DATA_BLOCK = 0;

    private static final byte SERVICE_DATA_SECTOR = 1;
    private static final byte SERVICE_DATA_BLOCK = 0;

    private static final byte AUTH_CARD_DATA_SECTOR = 7;
    private static final byte AUTH_CARD_DATA_START_BLOCK = 1;
    private static final byte AUTH_CARD_DATA_COUNT_BLOCK = 22;

    private static final byte EDS_START_SECTOR = 4;
    private static final byte EDS_START_BLOCK = 1;
    private static final byte EDS_BLOCK_COUNT = 4;

    private static final byte PASSAGE_MARK_SECTOR = 5;
    private static final byte PASSAGE_MARK_BLOCK = 2;

    private static final byte COVERAGE_AREA_SECTOR_1 = 1;
    private static final byte COVERAGE_AREA_BLOCK_1 = 1;
    private static final byte COVERAGE_AREA_COUNT_1 = 2;

    private static final byte COVERAGE_AREA_SECTOR_2 = 4;
    private static final byte COVERAGE_AREA_BLOCK_2 = 0;
    private static final byte COVERAGE_AREA_COUNT_2 = 1;

    private final OuterNumberReader outerNumberReader;
    private final ReadPassageMarkMifareClassicReader readPassageMarkMifareClassicReader;
    private final WritePassageMarkMifareClassicReader writePassageMarkMifareClassicReader;
    private final ServiceDataReader serviceDataReader;
    private final CoverageAreaReader coverageAreaReader;

    public AuthCardReaderImpl(IRfid rfid,
                              CardInfo cardInfo,
                              StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                              SamAuthorizationStrategy samAuthorizationStrategy,
                              MifareClassicReader mifareClassicReader,
                              OuterNumberReader outerNumberReader,
                              ReadPassageMarkMifareClassicReader readPassageMarkMifareClassicReader,
                              WritePassageMarkMifareClassicReader writePassageMarkMifareClassicReader,
                              ServiceDataReader serviceDataReader,
                              CoverageAreaReader coverageAreaReader) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
        this.outerNumberReader = outerNumberReader;
        this.readPassageMarkMifareClassicReader = readPassageMarkMifareClassicReader;
        this.writePassageMarkMifareClassicReader = writePassageMarkMifareClassicReader;
        this.serviceDataReader = serviceDataReader;
        this.coverageAreaReader = coverageAreaReader;
    }

    @NonNull
    @Override
    public ReadCardResult<OuterNumber> readOuterNumber() {
        return outerNumberReader.readOuterNumber();
    }

    @NonNull
    @Override
    public ReadCardResult<AuthCardData> readAuthCardData() {
        ReadCardResult<byte[]> rawResult = readBlocks(AUTH_CARD_DATA_SECTOR, AUTH_CARD_DATA_START_BLOCK, AUTH_CARD_DATA_COUNT_BLOCK);

        ReadCardResult<AuthCardData> result;

        if (rawResult.isSuccess()) {
            AuthCardData authCardData = new AuthCardDataParser().parse(rawResult.getData());
            result = new ReadCardResult<>(authCardData);
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }

        return result;
    }

    @NonNull
    @Override
    public ReadCardResult<ServiceData> readAuthServiceData() {
        return serviceDataReader.readServiceData(AUTH_SERVICE_DATA_SECTOR, AUTH_SERVICE_DATA_BLOCK);
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readRawAuthServiceData() {
        return readBlock(AUTH_SERVICE_DATA_SECTOR, AUTH_SERVICE_DATA_BLOCK);
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readRawCoverageAreaListPart1() {
        return coverageAreaReader.readRawCoverageAreaList(COVERAGE_AREA_SECTOR_1, COVERAGE_AREA_BLOCK_1, COVERAGE_AREA_COUNT_1);
    }

    @NonNull
    @Override
    public ReadCardResult<List<CoverageArea>> readCoverageAreaListPart1() {
        return coverageAreaReader.readCoverageAreaList(COVERAGE_AREA_SECTOR_1, COVERAGE_AREA_BLOCK_1, COVERAGE_AREA_COUNT_1);
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readRawCoverageAreaListPart2() {
        return coverageAreaReader.readRawCoverageAreaList(COVERAGE_AREA_SECTOR_2, COVERAGE_AREA_BLOCK_2, COVERAGE_AREA_COUNT_2);
    }

    @NonNull
    @Override
    public ReadCardResult<List<CoverageArea>> readCoverageAreaListPart2() {
        return coverageAreaReader.readCoverageAreaList(COVERAGE_AREA_SECTOR_2, COVERAGE_AREA_BLOCK_2, COVERAGE_AREA_COUNT_2);
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readEds() {
        return readBlocks(EDS_START_SECTOR, EDS_START_BLOCK, EDS_BLOCK_COUNT);
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readRawServiceData() {
        return serviceDataReader.readRawServiceData(SERVICE_DATA_SECTOR, SERVICE_DATA_BLOCK);
    }

    @NonNull
    @Override
    public ReadCardResult<ServiceData> readServiceData() {
        return serviceDataReader.readServiceData(SERVICE_DATA_SECTOR, SERVICE_DATA_BLOCK);
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readRawCoverageAreaList() {
        ReadCardResult<byte[]> part1 = readRawCoverageAreaListPart1();
        if (part1.isSuccess()) {
            ReadCardResult<byte[]> part2 = readRawCoverageAreaListPart2();
            if (part2.isSuccess()) {
                byte[] rawCoverageAreaList = ByteUtils.concatArrays(part1.getData(), part2.getData());
                return new ReadCardResult<>(rawCoverageAreaList);
            } else {
                return new ReadCardResult<>(part2.getReadCardErrorType(), part2.getDescription());
            }
        } else {
            return new ReadCardResult<>(part1.getReadCardErrorType(), part1.getDescription());
        }
    }

    @NonNull
    @Override
    public ReadCardResult<List<CoverageArea>> readCoverageAreaList() {
        ReadCardResult<List<CoverageArea>> part1 = readCoverageAreaListPart1();
        if (part1.isSuccess()) {
            ReadCardResult<List<CoverageArea>> part2 = readCoverageAreaListPart2();
            if (part2.isSuccess()) {
                List<CoverageArea> coverageAreaList = new ArrayList<>();
                coverageAreaList.addAll(part1.getData());
                coverageAreaList.addAll(part2.getData());
                return new ReadCardResult<>(coverageAreaList);
            } else {
                return new ReadCardResult<>(part2.getReadCardErrorType(), part2.getDescription());
            }
        } else {
            return new ReadCardResult<>(part1.getReadCardErrorType(), part1.getDescription());
        }
    }

    @NonNull
    @Override
    public ReadCardResult<PassageMark> readPassageMark() {
        return readPassageMarkMifareClassicReader.readPassageMark(PASSAGE_MARK_SECTOR, PASSAGE_MARK_BLOCK);
    }

    @NonNull
    @Override
    public WriteCardResult writePassageMark(PassageMark passageMark) {
        return writePassageMarkMifareClassicReader.writePassageMark(passageMark, PASSAGE_MARK_SECTOR, PASSAGE_MARK_BLOCK);
    }
}
