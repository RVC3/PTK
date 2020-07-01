package ru.ppr.core.dataCarrier.smartCard;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;
import ru.ppr.core.dataCarrier.pd.PdVersionDetector;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkReaderBMImpl;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;
import ru.ppr.utils.CommonUtils;

import static org.junit.Assert.*;

public class CppkReaderBMTest {

    @Mock
    IRfid rfid;
    @Mock CardInfo cardInfo;
    @Mock StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy;
    @Mock SamAuthorizationStrategy samAuthorizationStrategy;
    @Mock MifareClassicReader mifareClassicReader;
    @Mock OuterNumberReader outerNumberReader;
    @Mock WritePdMifareClassicReader writePdMifareClassicReader;
    @Mock ReadPassageMarkMifareClassicReader readPassageMarkMifareClassicReader;
    @Mock WritePassageMarkMifareClassicReader writePassageMarkMifareClassicReader;
    @Mock public PdDecoderFactory pdDecoderFactory;
    @Mock public PdVersionDetector pdVersionDetector;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }



    public CppkReaderBMImpl createBM(List<byte []> pdBytes){



    return new CppkReaderBMImpl(rfid,
                cardInfo,
                staticKeyAuthorizationStrategy,
                samAuthorizationStrategy,
                mifareClassicReader,
                outerNumberReader,
                writePdMifareClassicReader,
                readPassageMarkMifareClassicReader,
                writePassageMarkMifareClassicReader,
                pdDecoderFactory,
                pdVersionDetector,
            pdBytes
    );
    }

    @Test
    public void readPdList() {
        System.out.println("fdsadfsda");

        CppkReaderBMImpl impl = createBM(
                Arrays.asList(CommonUtils.hexStringToByteArray("4D23041965C7"))
        );
        System.out.println(
                impl.getTicketFromBM(CommonUtils.hexStringToByteArray("4D23041965C7")).length
        );
    }


    @Test
    public void readPdThreeMonth() {
        System.out.println("fdsadfsda");

        CppkReaderBMImpl impl = createBM(
                Arrays.asList(CommonUtils.hexStringToByteArray("4D23071965C7"))
        );
        System.out.println(
                impl.getTicketFromBM(CommonUtils.hexStringToByteArray("4D23041965C7")).length
        );
    }




    @Test
    public void readEds() {
    }
}