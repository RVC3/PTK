package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.SkmSkmoIpkReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.BscInformationReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.EmissionDataReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.PersonalDataReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.SkmoReadWritePdReader;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер карт СКМО.
 *
 * @author Aleksandr Brazhkin
 */
public class SkmoReaderImpl extends SkmSkmoIpkReaderImpl implements SkmoReader {

    private final SkmoReadWritePdReader skmoReadWritePdReader;

    public SkmoReaderImpl(IRfid rfid,
                          CardInfo cardInfo,
                          StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                          SamAuthorizationStrategy samAuthorizationStrategy,
                          BscInformationReader bscInformationReader,
                          EmissionDataReader emissionDataReader,
                          PersonalDataReader personalDataReader,
                          MifareClassicReader mifareClassicReader,
                          SkmoReadWritePdReader skmoReadWritePdReader) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader, bscInformationReader, emissionDataReader, personalDataReader);
        this.skmoReadWritePdReader = skmoReadWritePdReader;
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readEds() {
        return skmoReadWritePdReader.readEds();
    }

    @NonNull
    @Override
    public WriteCardResult writeEds(byte[] eds) {
        return skmoReadWritePdReader.writeEds(eds);
    }

    @NonNull
    @Override
    public ReadCardResult<List<Pd>> readPdList() {
        return skmoReadWritePdReader.readPdList();
    }

    @NonNull
    @Override
    public WriteCardResult writePdList(List<Pd> pdList, boolean[] forWriteIndexes) {
        return skmoReadWritePdReader.writePdList(pdList, forWriteIndexes);
    }

    @Override
    public int getMaxPdCount() {
        return skmoReadWritePdReader.getMaxPdCount();
    }

}
