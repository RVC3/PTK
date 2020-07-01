package ru.ppr.core.dataCarrier.smartCard;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;

import java.security.SecureRandom;

import static org.mockito.Mockito.mock;



import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;
import ru.ppr.core.dataCarrier.pd.PdVersionDetector;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.BigMoscowReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.BigMoscowReaderImpl;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;
import ru.ppr.utils.CommonUtils;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*", "*.*"})
@SmallTest
public class BigMoscowReaderTest {

    @Mock IRfid rfid;
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

    private BigMoscowReader bigMoscowReader;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        BigMoscowReaderImpl mockUrl = mock(BigMoscowReaderImpl.class);

        bigMoscowReader = new BigMoscowReaderImpl(
                rfid,
                cardInfo,
                staticKeyAuthorizationStrategy,
                samAuthorizationStrategy,
                mifareClassicReader,
                outerNumberReader,
                writePdMifareClassicReader,
                readPassageMarkMifareClassicReader,
                writePassageMarkMifareClassicReader,
                pdDecoderFactory,
                pdVersionDetector);


    }

    private void setArrays(String s6, String s15){
        PowerMockito.when(
                mifareClassicReader.readBlocks(6, 2, 1)
        ).thenReturn(
                new ReadCardResult<>(CommonUtils.hexStringToByteArray(s6))
        );

        PowerMockito.when(
                mifareClassicReader.readBlocks(15, 2, 1)
        ).thenReturn(
                new ReadCardResult<>(CommonUtils.hexStringToByteArray(s15))
        );
    }

    @Test
    public void checkBMCanFind(){
        setArrays("4D23041965C7", "4D23041965C7");
        assertEquals(bigMoscowReader.checkForPD(6,15), true);

    }

    @Test
    public void incorrectCheckSum(){

        //non correct documents
        setArrays("4D2304196BC7", "4D23041965D7");
        assertEquals(bigMoscowReader.checkForPD(6,15), false);
        assertEquals(bigMoscowReader.getPds().size(),0);

        // two correct
        setArrays("4D23041965C7", "4D16041930E9");
        assertEquals(bigMoscowReader.checkForPD(6,15), true);
        assertEquals(bigMoscowReader.getPds().size(),2);

    }

    @Test
    public void incorrectFirstSymbol(){
        setArrays("2D23041ADF96BC", "65463908463675D7");
        assertEquals(bigMoscowReader.checkForPD(6,15), false);
        assertEquals(bigMoscowReader.getPds().size(),0);
    }

    @Test
    public void shortDocs(){
        //2 false
        setArrays("4D23", "4D010719");
        assertEquals(bigMoscowReader.checkForPD(6,15), false);
        assertEquals(bigMoscowReader.getPds().size(),0);

        //first false
        setArrays("4D0103", "4D23041965C7");
        assertEquals(bigMoscowReader.checkForPD(6,15), true);
        assertEquals(bigMoscowReader.getPds().size(),1);

        //second false
        setArrays("4D23041965C7", "4D23196BC7");
        assertEquals(bigMoscowReader.checkForPD(6,15), true);
        assertEquals(bigMoscowReader.getPds().size(),1);

    }
}