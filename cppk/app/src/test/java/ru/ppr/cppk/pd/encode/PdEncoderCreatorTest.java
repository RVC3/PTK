//package ru.ppr.cppk.pd.encode;
//
//import android.test.suitebuilder.annotation.SmallTest;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.robolectric.RobolectricTestRunner;
//
//import ru.ppr.nsi.entity.TicketStorageType;
//
//import static org.junit.Assert.assertTrue;
//
///**
// * Created by Артем on 14.01.2016.
// */
//@RunWith(RobolectricTestRunner.class)
//@SmallTest
//public class PdEncoderCreatorTest {
//
//    @Test
//    public void testCreate() throws Exception {
//
//        PdEncoder pdEncoderPaper = PdEncoderCreator.create(TicketStorageType.Paper);
//        PdEncoder pdEncoderFare = PdEncoderCreator.create(TicketStorageType.FarePaper);
//        PdEncoder pdEncoderOneOffShortSkmo = PdEncoderCreator.create(TicketStorageType.SKMO);
//        PdEncoder pdEncoderOneOffShortSKM = PdEncoderCreator.create(TicketStorageType.SKM);
//        PdEncoder pdEncoderOneOffShortTRK = PdEncoderCreator.create(TicketStorageType.TRK);
//        PdEncoder pdEncoderOneOffShortETT = PdEncoderCreator.create(TicketStorageType.ETT);
//        PdEncoder pdEncoderBsc = PdEncoderCreator.create(TicketStorageType.CPPK);
//        PdEncoder pdEncoderIpk = PdEncoderCreator.create(TicketStorageType.IPK);
//
//
//        assertTrue(pdEncoderPaper instanceof PaperPdEncoder);
//        assertTrue(pdEncoderFare instanceof FarePdEncoder);
//        assertTrue(pdEncoderOneOffShortETT instanceof OneOffShortPdEncoder);
//        assertTrue(pdEncoderOneOffShortSKM instanceof OneOffShortPdEncoder);
//        assertTrue(pdEncoderOneOffShortSkmo instanceof OneOffShortPdEncoder);
//        assertTrue(pdEncoderOneOffShortTRK instanceof OneOffShortPdEncoder);
//        assertTrue(pdEncoderBsc instanceof OneOffFullPdEncoder);
//        assertTrue(pdEncoderIpk instanceof OneOffFullPdEncoder);
//    }
//
//    @Test(expected = NullPointerException.class)
//    public void testNullType() {
//        PdEncoderCreator.create(null);
//    }
//
//    @Test(expected = IllegalStateException.class)
//    public void testUnknownType() throws Exception {
//        PdEncoderCreator.create(TicketStorageType.Unknown);
//    }
//}