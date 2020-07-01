//package ru.ppr.cppk.pd.encode;
//
//import android.test.suitebuilder.annotation.SmallTest;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.robolectric.RobolectricTestRunner;
//
//import java.util.Date;
//
//import ru.ppr.cppk.localdb.model.TicketWayType;
//import ru.ppr.cppk.localdb.model.PaymentType;
//
//import static org.junit.Assert.assertArrayEquals;
//
///**
// * Created by Артем on 14.01.2016.
// */
//@RunWith(RobolectricTestRunner.class)
//@SmallTest
//public class PaperPdEncoderTest {
//
//    @Test
//    public void testMakePdData() throws Exception {
//
//        PdEncoderBase pdEncoderBase = new PaperPdEncoder();
//        pdEncoderBase.setDirection(TicketWayType.OneWay);
//        pdEncoderBase.setOrderNumber(20);
//        pdEncoderBase.setTerm(0);
//        pdEncoderBase.setIsBankPayment(PaymentType.INDIVIDUAL_BANK_CARD);
//        pdEncoderBase.setSaleDatetime(new Date(1452768953123L));
//        pdEncoderBase.setTariffCode(837559);
//        pdEncoderBase.setExemptionCode(3605);
//
//        byte[] expected = new byte[]{0x01, 0x14, 0x00, 0x02, (byte) 0xb9, 0x7e, (byte) 0x97,
//                0x56, (byte) 0xb7, (byte) 0xc7, 0x0c, 0x00, 0x15, 0x0E};
//        byte[] actual = pdEncoderBase.makePdData();
//        assertArrayEquals(expected, actual);
//    }
//}