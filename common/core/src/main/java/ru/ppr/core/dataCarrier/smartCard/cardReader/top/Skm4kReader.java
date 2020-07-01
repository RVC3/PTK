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
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.Skm4kReadWritePdReader;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер карт СКМ 4k.
 *
 * @author Aleksandr Brazhkin
 */
public class Skm4kReader extends SkmSkmoIpkReaderImpl implements SkmReader {

    private final Skm4kReadWritePdReader skm4kReadWritePdReader;

    public Skm4kReader(IRfid rfid,
                       CardInfo cardInfo,
                       StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                       SamAuthorizationStrategy samAuthorizationStrategy,
                       BscInformationReader bscInformationReader,
                       EmissionDataReader emissionDataReader,
                       PersonalDataReader personalDataReader,
                       MifareClassicReader mifareClassicReader,
                       Skm4kReadWritePdReader skm4kReadWritePdReader) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader, bscInformationReader, emissionDataReader, personalDataReader);
        this.skm4kReadWritePdReader = skm4kReadWritePdReader;
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readEds() {
        return skm4kReadWritePdReader.readEds();
    }

    @NonNull
    @Override
    public WriteCardResult writeEds(byte[] eds) {
        return skm4kReadWritePdReader.writeEds(eds);
    }

    @NonNull
    @Override
    public ReadCardResult<List<Pd>> readPdList() {
        return skm4kReadWritePdReader.readPdList();
    }

    @NonNull
    @Override
    public WriteCardResult writePdList(List<Pd> pdList, boolean[] forWriteIndexes) {
        return skm4kReadWritePdReader.writePdList(pdList, forWriteIndexes);
    }

    @Override
    public int getMaxPdCount() {
        return skm4kReadWritePdReader.getMaxPdCount();
    }
}
