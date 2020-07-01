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
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.Skm1kReadWritePdReader;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер карт СКМ 1k.
 *
 * @author Aleksandr Brazhkin
 */
public class Skm1kReader extends SkmSkmoIpkReaderImpl implements SkmReader {

    private final Skm1kReadWritePdReader skm1kReadWritePdReader;

    public Skm1kReader(IRfid rfid,
                       CardInfo cardInfo,
                       StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                       SamAuthorizationStrategy samAuthorizationStrategy,
                       BscInformationReader bscInformationReader,
                       EmissionDataReader emissionDataReader,
                       PersonalDataReader personalDataReader,
                       MifareClassicReader mifareClassicReader,
                       Skm1kReadWritePdReader skm1kReadWritePdReader) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader, bscInformationReader, emissionDataReader, personalDataReader);
        this.skm1kReadWritePdReader = skm1kReadWritePdReader;
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readEds() {
        return skm1kReadWritePdReader.readEds();
    }

    @NonNull
    @Override
    public WriteCardResult writeEds(byte[] eds) {
        return skm1kReadWritePdReader.writeEds(eds);
    }

    @NonNull
    @Override
    public ReadCardResult<List<Pd>> readPdList() {
        return skm1kReadWritePdReader.readPdList();
    }

    @NonNull
    @Override
    public WriteCardResult writePdList(List<Pd> pdList, boolean[] forWriteIndexes) {
        return skm1kReadWritePdReader.writePdList(pdList, forWriteIndexes);
    }

    @Override
    public int getMaxPdCount() {
        return skm1kReadWritePdReader.getMaxPdCount();
    }
}
