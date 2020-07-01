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
//public class FarePdEncoderTest {
//
//    @Test
//    public void testMakePdData_There_BankCard() throws Exception {
//
//        PdEncoderBase pdEncoderBase = new FarePdEncoder();
//        pdEncoderBase.setIsBankPayment(PaymentType.INDIVIDUAL_BANK_CARD);
//        pdEncoderBase.setDirection(TicketWayType.OneWay);
//        pdEncoderBase.setOrderNumber(20);
//        pdEncoderBase.setOrderNumberSourcePd(40);
//        pdEncoderBase.setSaleTimeSourcePd(new Date(1452768953123L));
//        pdEncoderBase.setSaleDatetime(new Date(1452858820123L));
//        pdEncoderBase.setTariffCode(837559);
//        pdEncoderBase.setIdDeviceSourcePd(1465727523);
//
//        byte[] actual = pdEncoderBase.makePdData();
//        byte[] expected = new byte[]{0x02, 0x14, 0x00, 0x02, 0x28, 0x00, 0x00, (byte) 0xC4,
//                (byte) 0xDD, (byte) 0x98, 0x56, (byte) 0xb9, 0x7e, (byte) 0x97, 0x56, (byte) 0xb7,
//                (byte) 0xc7, 0x0c, 0x00, 0x23, 0x3A, 0x5D, 0x57};
//        assertArrayEquals(expected, actual);
//    }
//
//    @Test
//    public void testMakePdData_Back_BankCard() throws Exception {
//
//        PdEncoderBase pdEncoderBase = new FarePdEncoder();
//        pdEncoderBase.setIsBankPayment(PaymentType.INDIVIDUAL_BANK_CARD);
//        pdEncoderBase.setDirection(TicketWayType.TwoWay);
//        pdEncoderBase.setOrderNumber(20);
//        pdEncoderBase.setOrderNumberSourcePd(40);
//        pdEncoderBase.setSaleTimeSourcePd(new Date(1452768953123L));
//        pdEncoderBase.setSaleDatetime(new Date(1452858820123L));
//        pdEncoderBase.setTariffCode(837559);
//        pdEncoderBase.setIdDeviceSourcePd(1465727523);
//
//        byte[] actual = pdEncoderBase.makePdData();
//        byte[] expected = new byte[]{0x02, 0x14, 0x00, 0x03, 0x28, 0x00, 0x00, (byte) 0xC4,
//                (byte) 0xDD, (byte) 0x98, 0x56, (byte) 0xb9, 0x7e, (byte) 0x97, 0x56, (byte) 0xb7,
//                (byte) 0xc7, 0x0c, 0x00, 0x23, 0x3A, 0x5D, 0x57};
//        assertArrayEquals(expected, actual);
//    }
//
//    @Test
//    public void testMakePdData_There_IndividualCash() throws Exception {
//
//        PdEncoderBase pdEncoderBase = new FarePdEncoder();
//        pdEncoderBase.setIsBankPayment(PaymentType.INDIVIDUAL_CASH);
//        pdEncoderBase.setDirection(TicketWayType.OneWay);
//        pdEncoderBase.setOrderNumber(20);
//        pdEncoderBase.setOrderNumberSourcePd(40);
//        pdEncoderBase.setSaleTimeSourcePd(new Date(1452768953123L));
//        pdEncoderBase.setSaleDatetime(new Date(1452858820123L));
//        pdEncoderBase.setTariffCode(837559);
//        pdEncoderBase.setIdDeviceSourcePd(1465727523);
//
//        byte[] actual = pdEncoderBase.makePdData();
//        byte[] expected = new byte[]{0x02, 0x14, 0x00, 0x00, 0x28, 0x00, 0x00, (byte) 0xC4,
//                (byte) 0xDD, (byte) 0x98, 0x56, (byte) 0xb9, 0x7e, (byte) 0x97, 0x56, (byte) 0xb7,
//                (byte) 0xc7, 0x0c, 0x00, 0x23, 0x3A, 0x5D, 0x57};
//        assertArrayEquals(expected, actual);
//    }
//
//    @Test
//    public void testMakePdData_Back_IndividualCash() throws Exception {
//
//        PdEncoderBase pdEncoderBase = new FarePdEncoder();
//        pdEncoderBase.setIsBankPayment(PaymentType.INDIVIDUAL_CASH);
//        pdEncoderBase.setDirection(TicketWayType.TwoWay);
//        pdEncoderBase.setOrderNumber(20);
//        pdEncoderBase.setOrderNumberSourcePd(40);
//        pdEncoderBase.setSaleTimeSourcePd(new Date(1452768953123L));
//        pdEncoderBase.setSaleDatetime(new Date(1452858820123L));
//        pdEncoderBase.setTariffCode(837559);
//        pdEncoderBase.setIdDeviceSourcePd(1465727523);
//
//        byte[] actual = pdEncoderBase.makePdData();
//        byte[] expected = new byte[]{0x02, 0x14, 0x00, 0x01, 0x28, 0x00, 0x00, (byte) 0xC4,
//                (byte) 0xDD, (byte) 0x98, 0x56, (byte) 0xb9, 0x7e, (byte) 0x97, 0x56, (byte) 0xb7,
//                (byte) 0xc7, 0x0c, 0x00, 0x23, 0x3A, 0x5D, 0x57};
//        assertArrayEquals(expected, actual);
//    }
//}