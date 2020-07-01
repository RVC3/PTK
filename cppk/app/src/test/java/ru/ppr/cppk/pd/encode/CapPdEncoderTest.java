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
//import static org.junit.Assert.assertArrayEquals;
//
///**
// *
// * Created by Артем on 14.01.2016.
// */
//@RunWith(RobolectricTestRunner.class)
//@SmallTest
//public class CapPdEncoderTest {
//
//    @Test
//    public void testMakePdData() throws Exception {
//
//        PdEncoderBase pdEncoderBase = new CapPdEncoder();
//        pdEncoderBase.setSaleDatetime(new Date(1452768953123L));
//
//        byte[] actual = pdEncoderBase.makePdData();
//        byte[] expected = new byte[]{0x40, (byte) 0xb9, 0x7e, (byte) 0x97, 0x56};
//        assertArrayEquals(expected, actual);
//
//    }
//}