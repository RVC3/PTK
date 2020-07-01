package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdMifareClassicReader;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;
import ru.ppr.utils.CommonUtils;

public class TroykaWithBMPdReaderImpl extends TroykaWithPdReaderImpl {

    public TroykaWithBMPdReaderImpl(IRfid rfid,
                                    CardInfo cardInfo,
                                    StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                    SamAuthorizationStrategy samAuthorizationStrategy,
                                    MifareClassicReader mifareClassicReader,
                                    OuterNumberReader outerNumberReader,
                                    ReadPdMifareClassicReader readPdMifareClassicReader,
                                    WritePdMifareClassicReader writePdMifareClassicReader,
                                    PdDecoderFactory pdDecoderFactory,
                                    List<byte[]> pdBytes,
                                    ReadPassageMarkMifareClassicReader readPassageMarkMifareClassicReader, WritePassageMarkMifareClassicReader writePassageMarkMifareClassicReader) {
        super(rfid,
                cardInfo,
                staticKeyAuthorizationStrategy,
                samAuthorizationStrategy,
                mifareClassicReader,
                outerNumberReader,
                readPdMifareClassicReader,
                writePdMifareClassicReader, readPassageMarkMifareClassicReader);
        this.pdBytes = pdBytes;
        this.pdDecoderFactory = pdDecoderFactory;
    }

    @NonNull
    @Override
    public ReadCardResult<List<Pd>> readPdList(){

        ReadCardResult<List<Pd>> readCardResult = super.readPdList();
        oldPDS = readCardResult.getData();

        for(byte [] b_ticket: pdBytes) {
            Logger.trace(TAG, "Тариф Большая Москва " + Arrays.toString(b_ticket));

            PdDecoder pdDecoder = pdDecoderFactory.create(getTicketFromBM(b_ticket));
            Pd pd = pdDecoder.decode(getTicketFromBM(b_ticket));
            readCardResult.getData().add(pd);
        }

        return readCardResult;
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readEds() {
        if(oldPDS != null && oldPDS.size() > 0)
            return super.readEds();

        else {
            return new ReadCardResult<>(new byte[64]);
        }
    }

    public byte [] getTicketFromBM(byte[] bm_ticket){
        byte[] b = CommonUtils.hexStringToByteArray("048F8F0018E3985C2F2F2800D10400C0");

        String s = CommonUtils.bytesToHexWithoutSpaces(Arrays.copyOfRange(bm_ticket,1,4));

        Calendar calendar = Calendar.getInstance(); // this would default to now

        String pattern = "ddMMyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        try {
            Date date = simpleDateFormat.parse(s);

            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH, -29);

            long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(calendar.getTime());
            DataCarrierUtils.writeLong(
                    saleDateTimeLong,
                    b,
                    4,
                    4,
                    ByteOrder.LITTLE_ENDIAN);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return b;
    }
    private PdDecoderFactory pdDecoderFactory;
    private List<Pd> oldPDS;
    private List<byte []> pdBytes;
    private static final String TAG = Logger.makeLogTag(TroykaWithBMPdReaderImpl.class);

}
