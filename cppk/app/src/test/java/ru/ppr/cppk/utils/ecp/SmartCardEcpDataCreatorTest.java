package ru.ppr.cppk.utils.ecp;

import android.test.suitebuilder.annotation.SmallTest;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;

import ru.ppr.core.dataCarrier.pd.DefaultPdEncoderFactory;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.di.Dagger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Артем on 19.01.2016.
 */
@RunWith(RobolectricTestRunner.class)
@SmallTest
public class SmartCardEcpDataCreatorTest {

    private BscInformation bscInformation;
    // номер ключа эцп в виде числа
    private final long ecpKey = 644156758;
    //номер ключа эцп в LittleEndian
    private final byte[] ecpKeyBytes = new byte[]{0x56, 0x0D, 0x65, 0x26};
    //данные существующего билета без номера ключа эцп
    private final byte[] existPdBytes = new byte[]{0x01, 0x14, 0x00, 0x02, (byte) 0xb9, 0x7e, (byte) 0x97,
            0x56, (byte) 0xb7, (byte) 0xc7, 0x0c, 0x00, 0x15, 0x0E};

    //данные билета, который продаем
    private final byte[] newPd = new byte[]{0x01, 0x34, 0x00, 0x00, 0x1b, (byte) 0xf0, (byte) 0x9c,
            0x56, (byte) 0xb7, (byte) 0xc7, 0x0c, 0x00, 0x01, 0x00};

    //внешний номер карты
    private final byte[] outerNumber = new byte[]{0x04, 0x05, 0x06, 0x07};
    //ид карты
    private final byte[] crystalSerialNumber = new byte[]{0x01, 0x02, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00};

    private ByteArrayOutputStream byteArrayOutputStream;

    @Before
    public void setUp() throws Exception {

        byteArrayOutputStream = new ByteArrayOutputStream();

        bscInformation = mock(BscInformation.class);
        when(bscInformation.getCrystalSerialNumber()).thenReturn(crystalSerialNumber);
        when(bscInformation.getOuterNumberBytes()).thenReturn(outerNumber);

        AppComponent appComponent = mock(AppComponent.class);
        when(appComponent.pdEncoderFactory()).thenReturn(new DefaultPdEncoderFactory());
        Dagger.setAppComponent(appComponent);
    }

    @After
    public void tearDown() throws Exception {
        byteArrayOutputStream.close();
        byteArrayOutputStream = null;
    }

    @Test
    public void testCreateDataForOnePd() throws Exception {

        byteArrayOutputStream.write(newPd);
        byteArrayOutputStream.write(crystalSerialNumber);
        byteArrayOutputStream.write(outerNumber);

        byte[] expected = byteArrayOutputStream.toByteArray();

        EcpDataCreator creator = new SmartCardEcpDataCreator.Builder(newPd, bscInformation)
                .build();

        assertNotNull(creator);

        byte[] actual = creator.create();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testCreateDataForWithExistPdOnIndexOne() throws Exception {

        byteArrayOutputStream.write(existPdBytes);
        byteArrayOutputStream.write(ecpKeyBytes);
        byteArrayOutputStream.write(newPd);
        byteArrayOutputStream.write(crystalSerialNumber);
        byteArrayOutputStream.write(outerNumber);

        byte[] expected = byteArrayOutputStream.toByteArray();

        PD existPd = new PD(1, 18);
        existPd.orderNumberPdOnCard = 0;
        existPd.term = 0;
        existPd.wayType = TicketWayType.OneWay;
        existPd.setIssBankPaymentType(true);
        existPd.numberPD = 20;
        existPd.saleDatetimePD = new Date(1452768953000L);
        existPd.tariffCodePD = 837559L;
        existPd.exemptionCode = 3605;
        existPd.ecpNumberPD = ecpKey;

        EcpDataCreator creator = new SmartCardEcpDataCreator.Builder(newPd, bscInformation).setExistPd(existPd).build();

        assertNotNull(creator);

        byte[] actual = creator.create();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testCreateDataForWithExistPdOnIndexTwo() throws Exception {

        byteArrayOutputStream.write(newPd);
        byteArrayOutputStream.write(existPdBytes);
        byteArrayOutputStream.write(ecpKeyBytes);
        byteArrayOutputStream.write(crystalSerialNumber);
        byteArrayOutputStream.write(outerNumber);

        byte[] expected = byteArrayOutputStream.toByteArray();

        PD existPd = new PD(1, 18);
        existPd.orderNumberPdOnCard = 1;
        existPd.term = 0;
        existPd.wayType = TicketWayType.OneWay;
        existPd.setIssBankPaymentType(true);
        existPd.numberPD = 20;
        existPd.saleDatetimePD = new Date(1452768953000L);
        existPd.tariffCodePD = 837559L;
        existPd.exemptionCode = 3605;
        existPd.ecpNumberPD = ecpKey;

        EcpDataCreator creator = new SmartCardEcpDataCreator.Builder(newPd, bscInformation).setExistPd(existPd).build();

        assertNotNull(creator);

        byte[] actual = creator.create();
        assertArrayEquals(expected, actual);
    }
}