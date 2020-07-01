package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;
import ru.ppr.core.dataCarrier.pd.PdVersionDetector;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseClassicCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdMifareClassicReader;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;
import ru.ppr.utils.CommonUtils;

public class BigMoscowReaderImpl extends BaseClassicCardReader implements BigMoscowReader {


    private final OuterNumberReader outerNumberReader;
    private final WritePdMifareClassicReader writePdMifareClassicReader;
    private final ReadPassageMarkMifareClassicReader readPassageMarkMifareClassicReader;
    private final WritePassageMarkMifareClassicReader writePassageMarkMifareClassicReader;
    private final PdDecoderFactory pdDecoderFactory;
    private final PdVersionDetector pdVersionDetector;

    public BigMoscowReaderImpl(IRfid rfid,
                          CardInfo cardInfo,
                          StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                          SamAuthorizationStrategy samAuthorizationStrategy,
                          MifareClassicReader mifareClassicReader,
                          OuterNumberReader outerNumberReader,
                          WritePdMifareClassicReader writePdMifareClassicReader,
                          ReadPassageMarkMifareClassicReader readPassageMarkMifareClassicReader,
                          WritePassageMarkMifareClassicReader writePassageMarkMifareClassicReader,
                          PdDecoderFactory pdDecoderFactory,
                          PdVersionDetector pdVersionDetector) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
        this.outerNumberReader = outerNumberReader;
        this.writePdMifareClassicReader = writePdMifareClassicReader;
        this.readPassageMarkMifareClassicReader = readPassageMarkMifareClassicReader;
        this.writePassageMarkMifareClassicReader = writePassageMarkMifareClassicReader;
        this.pdDecoderFactory = pdDecoderFactory;
        this.pdVersionDetector = pdVersionDetector;

        pds = new LinkedList<>();
    }

    @Override
    public boolean checkForPD(int sector_one, int sector_two) {
        boolean result_one = false;
        boolean result_two = false;

        ReadCardResult<byte[]> rawResult = readBlocks(/*PD_START_SECTOR*/sector_one, 2, 1);

        byte [] bytes = rawResult.getData();
        Logger.trace(TAG, "checkForPD: sector " + sector_one + " " + CommonUtils.bytesToHexWithoutSpaces(bytes));

        if(bytes !=null) {
            Logger.trace(TAG, "checkForPD: ====== a == " + (char) (bytes[0] & 0xff));

            result_one = checkByteSet(bytes);

            pds.clear();
            if (result_one)
                pds.add(Arrays.copyOfRange(bytes, 0, 6));
        }

        rawResult = readBlocks(/*PD_START_SECTOR*/sector_two, 2, 1);
        bytes = rawResult.getData();
        if(bytes!=null) {
            Logger.trace("========", "checkForPD: sector " + sector_two + "  " + CommonUtils.bytesToHexWithoutSpaces(bytes));

            Logger.trace(TAG, "checkForPD: ====== a==" + (char) (bytes[0] & 0xff));

            result_two = checkByteSet(bytes);

            if (result_two)
                pds.add(Arrays.copyOfRange(bytes, 0, 6));
        }
        return result_one || result_two;
    }

    private boolean checkByteSet(byte [] bytes){
        if(bytes[0] != 0x4d)
            return false;
        if(bytes.length < 6)
            return false;

        byte [] t = Arrays.copyOfRange(bytes, 0, 4);

        final int sm = getSign(t);
        Logger.trace(TAG, String.format("0x%x\n", sm));

        byte [] t_sum = Arrays.copyOfRange(bytes, 4, 6);

        Logger.trace(TAG, String.format("sum size is %d\n", t_sum.length));

        final int sm_ticket = (t_sum[1] & 0xff) << 8 | (t_sum[0] & 0xff);
        Logger.trace(TAG, String.format("sum from ticket 0x%x\n", sm_ticket));


        // проверка контрольной суммы
        if(sm_ticket != getSign(t))
            return false;


        return true;
    }

    @Override
    public List<byte[]> getPds() {
        return pds;
    }


    public static int getSign(byte [] signArray){
        int result = 0x6363;

        for (byte oneByte : signArray) {
            result ^= oneByte << 8;

            for (int j = 0; j < 8; j++) {
                result <<= 1;
                if ((result & 0x10000) != 0)
                    result ^= 0x1021;
            }
        }
        return result & 0xffff;
    }

    private static final String TAG = Logger.makeLogTag(BigMoscowReaderImpl.class);
    private List<byte []> pds;
}
