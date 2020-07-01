package ru.ppr.cppk.device.rfid.image;

import android.test.suitebuilder.annotation.SmallTest;

import com.google.common.io.Files;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;

import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.NsiDataProvider;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.NsiSamAuthorizationStrategy;
import ru.ppr.cppk.dataCarrier.smartCard.findcardtask.NsiDataProviderImpl;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.dao.AccessSchemeDao;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.rfid.CardData;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.MifareCardType;
import ru.ppr.rfid.RfidResult;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.WriteToCardResult;
import ru.ppr.rfid.image.RfidImage;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Артем on 10.03.2016.
 */
@RunWith(RobolectricTestRunner.class)
@SmallTest
public class RfidImageTest {
    
    private static final String DEFAULT_IMAGE_TRK = "rfid_image_troyka.bin";
    private static final String DEFAULT_IMAGE_IPK = "rfid_image_IPK.bin";
    private static final String IMAGE_IPK_AFTER_WRITE = "rfid_image_IPK_after_write.bin";
    private static final String IMAGE_AFTER_WRITE_FOR_CLASSICK = "rfid_image_troyka_after_write.bin";
    private static final String IMAGE_AFTER_DELETE_TRK = "rfid_image_troyka_after_delete.bin";
    private static final String DEFAULT_IMAGE_ULTRALIGHT_C = "rfid_image_UltraLight.bin";
    private static final String IMAGE_EMPTY_IPK = "rfid_image_IPK_empty.bin";
    
    @Test
    public void testIsOpened() throws Exception {
        RfidImage.Config config = new RfidImage.Config(new File(getClass().getResource("/image/" + DEFAULT_IMAGE_TRK).getFile()));
        IRfid iRfid = new RfidImage(config);
        assertTrue(iRfid.isOpened());
    }

    @Test
    public void testGetFWVersion() throws Exception {
        RfidImage.Config config = new RfidImage.Config(new File(getClass().getResource("/image/" + DEFAULT_IMAGE_TRK).getFile()));
        IRfid iRfid = new RfidImage(config);
        String[] strings = new String[1];
        boolean result = iRfid.getFWVersion(strings);
        assertTrue(result);
        assertEquals("FileVersion", strings[0]);
    }

    @Test
    public void testGetRfidAtr() throws Exception {
        RfidImage.Config config = new RfidImage.Config(new File(getClass().getResource("/image/" + DEFAULT_IMAGE_TRK).getFile()));
        IRfid iRfid = new RfidImage(config);

        CardData expected = new CardData();
        expected.setAtqa(new byte[2]);
        expected.setCom(new byte[1]);
        expected.setSak(new byte[1]);
        expected.setMifareUlIdentifyType((byte) 0);
        expected.setRfidAttr(new byte[]{0x01,0x07, 0x04, 0x74, 0x3A, 0x12, 0x29, 0x31, (byte) 0x80});
        expected.setCardUID(new byte[]{0x04, 0x74, 0x3A, 0x12, 0x29, 0x31, (byte) 0x80});
        expected.setMifareCardType(MifareCardType.Mifare_Classic_1K);

        CardData actual = iRfid.getRfidAtr();
        assertEquals(expected, actual);
    }

    @Test
    public void testReadFromClassic() throws Exception {
        RfidImage.Config config = new RfidImage.Config(new File(getClass().getResource("/image/" + DEFAULT_IMAGE_TRK).getFile()));
        IRfid iRfid = new RfidImage(config);
        final byte[] expected = new byte[]{0x03, 0x21, 0x00, 0x00, 0x18, (byte) 0x8A, (byte) 0xD9,
                0x56, (byte) 0xF6, (byte) 0xC7, 0x0C, 0x00, 0x62, 0x00, 0x00, 0x00};
        // читаем 1й билет с карты типа "тройка"
        RfidResult<byte[]> rfidResult = iRfid.readFromClassic((byte) 6, (byte) 0, null, true);
        assertTrue(rfidResult.isOk());
        assertArrayEquals(expected, rfidResult.getResult());
    }

    @Test
    public void testWriteToClassic() throws Exception {

        // Таким файл должен быть после записи
        File expectedFile = new File(getClass().getResource("/image/" + IMAGE_AFTER_WRITE_FOR_CLASSICK).getFile());
        // с этим файлом будем работать, за основу возьмем обычный образ тройки
        File troykaImage = new File(getClass().getResource("/image/" + DEFAULT_IMAGE_TRK).getFile());
        File troykaForWrite = new File(troykaImage.getParent() + "/troyka_delete_tmp.bin");
        if(troykaForWrite.exists()) {
            assertTrue("Error delete file" ,troykaForWrite.delete());
        }
        assertFalse(troykaForWrite.exists());
        assertTrue("File not created", troykaForWrite.createNewFile());
        Files.copy(troykaImage, troykaForWrite);
        byte[] expectedBeforeWrite = Files.toByteArray(troykaImage);
        byte[] actualBeforeWrite = Files.toByteArray(troykaForWrite);
        assertArrayEquals(expectedBeforeWrite, actualBeforeWrite);
        RfidImage.Config config = new RfidImage.Config(new File(troykaForWrite.getPath()));
        IRfid rfid = new RfidImage(config);
        final byte[] data = new byte[]{0x03, 0x17, 0x00, 0x00, (byte) 0xE0, 0x24, (byte) 0xC3, 0x55,
                0x12, (byte) 0x94, 0x03, 0x00, 0x2D, 0x01, 0x00, 0x00};
        WriteToCardResult result = rfid.writeToClassic(data, null, 6, 1, null, false);
        assertEquals(WriteToCardResult.SUCCESS, result);
        assertArrayEquals(Files.toByteArray(expectedFile), Files.toByteArray(troykaForWrite));
        troykaForWrite.delete();
    }

    @Test
    public void testReadFromUltralight() throws Exception {
        RfidImage.Config config = new RfidImage.Config(new File(getClass().getResource("/image/" + DEFAULT_IMAGE_ULTRALIGHT_C).getFile()));
        IRfid rfid = new RfidImage(config);
        //читаем билет с абонемента на количество поездок
        RfidResult<byte[]> rfidResult = rfid.readFromUltralight((byte) 8, (byte) 0, (byte) 16);
        byte[] expected = new byte[]{0x07, 0x52, 0x00, 0x00, (byte) 0xBC, 0x18, (byte) 0xA7, 0x56,
                (byte) 0xB1, 0x6A, 0x00, 0x00, 0x2B, 0x00, 0x34, 0x00};

        assertArrayEquals(expected, rfidResult.getResult());
    }

    @Test
    public void testWriteToUltralight() throws Exception {

    }

    @Test
    public void testDeleteDataFromClassic() throws Exception {
        // Таким файл должен быть после записи
        File expectedFile = new File(getClass().getResource("/image/" + IMAGE_AFTER_DELETE_TRK).getFile());
        // с этим файлом будем работать, за основу возьмем обычный образ ИПК
        File ipkImage = new File(getClass().getResource("/image/" + DEFAULT_IMAGE_TRK).getFile());
        File ipkForWrite = new File(ipkImage.getParent() + "/trk_tmp.bin");
        if(ipkForWrite.exists()) {
            assertTrue("Error delete file", ipkForWrite.delete());
        }
        assertFalse(ipkForWrite.exists());
        assertTrue("File not created", ipkForWrite.createNewFile());
        Files.copy(ipkImage, ipkForWrite);
        byte[] expectedBeforeWrite = Files.toByteArray(ipkImage);
        byte[] actualBeforeWrite = Files.toByteArray(ipkForWrite);
        assertArrayEquals(expectedBeforeWrite, actualBeforeWrite);
        RfidImage.Config config = new RfidImage.Config(new File(ipkForWrite.getPath()));
        IRfid rfid = new RfidImage(config);

        AccessSchemeDao accessSchemeDao = mock(AccessSchemeDao.class);
        NsiDaoSession nsiDaoSession = mock(NsiDaoSession.class);
        when(nsiDaoSession.getAccessSchemeDao()).thenReturn(accessSchemeDao);

        NsiVersionManager nsiVersionManager = mock(NsiVersionManager.class);

        NsiDataProvider nsiDataProvider = new NsiDataProviderImpl(nsiDaoSession, nsiVersionManager);
        SamAuthorizationStrategy samAuthorizationStrategy = new NsiSamAuthorizationStrategy(nsiDataProvider, TicketStorageType.TRK.getDBCode());
        WriteToCardResult result = rfid.deleteDataFromClassic(null, (byte)6, (byte)0, (byte)3, samAuthorizationStrategy, false);
        assertEquals(WriteToCardResult.SUCCESS, result);
        assertArrayEquals(Files.toByteArray(expectedFile), Files.toByteArray(ipkForWrite));
        ipkForWrite.delete();
    }

    @Test
    public void testWriteMoreThanOneBlock() throws Exception {
        // Таким файл должен быть после записи
        File expectedFile = new File(getClass().getResource("/image/" + IMAGE_IPK_AFTER_WRITE).getFile());
        // с этим файлом будем работать, за основу возьмем обычный образ ИПК
        File ipkImage = new File(getClass().getResource("/image/" + DEFAULT_IMAGE_IPK).getFile());
        File ipkForWrite = new File(ipkImage.getParent() + "/ipk_tmp.bin");
        if(ipkForWrite.exists()) {
            assertTrue("Error delete file" ,ipkForWrite.delete());
        }
        assertFalse(ipkForWrite.exists());
        assertTrue("File not created", ipkForWrite.createNewFile());
        Files.copy(ipkImage, ipkForWrite);
        byte[] expectedBeforeWrite = Files.toByteArray(ipkImage);
        byte[] actualBeforeWrite = Files.toByteArray(ipkForWrite);
        assertArrayEquals(expectedBeforeWrite, actualBeforeWrite);
        RfidImage.Config config = new RfidImage.Config(new File(ipkForWrite.getPath()));
        IRfid rfid = new RfidImage(config);
        final byte[] data = new byte[]{0x05, 0x47, 0x00, 0x00, 0x38, 0x53, 0x4b, 0x56, (byte) 0xEA,
                0x65, 0x00, 0x00, 0x00, 0x00, 0x5f, 0x02, 0x00, (byte) 0xc0};
        WriteToCardResult result = rfid.writeToClassic(data, null, 7, 2, null, false);
        assertEquals(WriteToCardResult.SUCCESS, result);
        assertArrayEquals(Files.toByteArray(expectedFile), Files.toByteArray(ipkForWrite));
        ipkForWrite.delete();
    }

    @Test
    public void testWriteThreeSectors() throws Exception {

        // Таким файл должен быть после записи
        File expectedFile = new File(getClass().getResource("/image/" + DEFAULT_IMAGE_IPK).getFile());
        // с этим файлом будем работать, за основу возьмем образ ИПК без пд и эцп
        File ipkImage = new File(getClass().getResource("/image/" + IMAGE_EMPTY_IPK).getFile());
        File ipkForWrite = new File(ipkImage.getParent() + "/image_tmp.bin");
        if(ipkForWrite.exists()) {
            assertTrue("Error delete file" ,ipkForWrite.delete());
        }
        assertFalse(ipkForWrite.exists());
        assertTrue("File not created", ipkForWrite.createNewFile());
        Files.copy(ipkImage, ipkForWrite);
        byte[] expectedBeforeWrite = Files.toByteArray(ipkImage);
        byte[] actualBeforeWrite = Files.toByteArray(ipkForWrite);
        assertArrayEquals(expectedBeforeWrite, actualBeforeWrite);
        RfidImage.Config config = new RfidImage.Config(new File(ipkForWrite.getPath()));
        IRfid rfid = new RfidImage(config);
        final byte[] data = new byte[]{0x05, 0x47, 0x00, 0x00, 0x38, 0x53, 0x4B, 0x56, (byte) 0xEA,
                0x65, 0x00, 0x00, 0x00, 0x00, 0x5F, 0x02, 0x00, (byte) 0xC0, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                (byte) 0x9B, 0x51, (byte) 0xFD, (byte) 0xAA, 0x0B, (byte) 0xC0, (byte) 0xD3, (byte) 0xE8,
                (byte) 0xC2, 0x5D, 0x43, 0x31, 0x17, 0x62, 0x1D, 0x0F, (byte) 0xB1, 0x39, 0x02, (byte) 0xBA,
                0x3E, 0x30, (byte) 0xB1, 0x19, (byte) 0xC1, (byte) 0x8A, 0x46, 0x03, 0x14, 0x20, 0x4C,
                0x05, (byte) 0xD9, 0x3B, (byte) 0xF4, (byte) 0xA6, (byte) 0x96, 0x30, (byte) 0x85, (byte) 0xB9,
                (byte) 0xAC, (byte) 0x9A, 0x75, 0x17, (byte) 0xEA, 0x7D, (byte) 0xAC, 0x4B, (byte) 0xD7,
                0x60, (byte) 0x9F, 0x40, (byte) 0xDC, (byte) 0xBD, (byte) 0xDC, (byte) 0xFD, 0x0C, (byte) 0xDD,
                0x40, (byte) 0xA0, 0x3D, (byte) 0xBE, 0x20, (byte) 0xF8, 0x04, 0x16, 0x00, 0x00, 0x00,
                0x24, (byte) 0x85, 0x1E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        WriteToCardResult result = rfid.writeToClassic(data, null, 7, 0, null, false);
        assertEquals(WriteToCardResult.SUCCESS, result);
        assertArrayEquals(Files.toByteArray(expectedFile), Files.toByteArray(ipkForWrite));
        ipkForWrite.delete();

    }
}