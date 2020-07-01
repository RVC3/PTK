package ru.ppr.cppk.device.barcode.file;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import ru.ppr.barcode.IBarcodeReader;
import ru.ppr.barcode.file.BarcodeReaderFile;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by Артем on 11.03.2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Log.class)
public class FileBarcodeReaderTest {

    private static final String BARCODE_IMAGE = "barcode.bin";

    private IBarcodeReader barcodeReader;

    @Before
    public void setUp() {
        BarcodeReaderFile.Config config = new BarcodeReaderFile.Config(new File(getClass().getResource("/image/" + BARCODE_IMAGE).getFile()));
        barcodeReader = new BarcodeReaderFile(config);
    }

    @Test
    public void testStartScan() {
        PowerMockito.mockStatic(Log.class);

        final byte[] expected = new byte[]{0x05, 0x47, 0x00, 0x00, 0x38, 0x53, 0x4B, 0x56, (byte) 0xEA,
                0x65, 0x00, 0x00, 0x00, 0x00, 0x5F, 0x02};

        byte[] barcodeData = barcodeReader.scan();
        assertArrayEquals(expected, barcodeData);
    }
}