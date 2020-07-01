package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.TroykaReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.CoverageAreaReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ServiceDataReader;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;
import ru.ppr.core.dataCarrier.smartCard.wallet.MetroWallet;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер карт Тройка 2K со служебными данными.
 *
 * @author Aleksandr Brazhkin
 */
public class TroykaWithServiceDataReaderImpl extends TroykaReaderImpl implements TroykaWithServiceDataReader {

    private static final byte SERVICE_DATA_SECTOR = 6;
    private static final byte SERVICE_DATA_BLOCK = 0;

    private static final byte COVERAGE_AREA_SECTOR = 6;
    private static final byte COVERAGE_AREA_BLOCK = 1;
    private static final byte COVERAGE_AREA_COUNT = 1;

    private final OuterNumberReader outerNumberReader;
    private final ServiceDataReader serviceDataReader;
    private final CoverageAreaReader coverageAreaReader;

    public TroykaWithServiceDataReaderImpl(IRfid rfid,
                                           CardInfo cardInfo,
                                           OuterNumberReader outerNumberReader,
                                           StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                           SamAuthorizationStrategy samAuthorizationStrategy,
                                           MifareClassicReader mifareClassicReader,
                                           ServiceDataReader serviceDataReader,
                                           CoverageAreaReader coverageAreaReader) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader, outerNumberReader, null);
        this.outerNumberReader = outerNumberReader;
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
        return coverageAreaReader.readRawCoverageAreaList(COVERAGE_AREA_SECTOR, COVERAGE_AREA_BLOCK, COVERAGE_AREA_COUNT);
    }

    @NonNull
    @Override
    public ReadCardResult<List<CoverageArea>> readCoverageAreaList() {
        return coverageAreaReader.readCoverageAreaList(COVERAGE_AREA_SECTOR, COVERAGE_AREA_BLOCK, COVERAGE_AREA_COUNT);
    }

}
