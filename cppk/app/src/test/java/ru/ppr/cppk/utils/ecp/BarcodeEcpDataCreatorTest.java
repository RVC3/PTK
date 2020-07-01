package ru.ppr.cppk.utils.ecp;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Артем on 19.01.2016.
 */
public class BarcodeEcpDataCreatorTest {

    @Test
    public void testCreate() throws Exception {

        byte[] expected = new byte[]{0x01, 0x34, 0x00, 0x00, 0x1b, (byte) 0xf0, (byte) 0x9c, 0x56,
                (byte) 0xb7, (byte) 0xc7, 0x0c, 0x00, 0x01, 0x00};

        EcpDataCreator ecpDataCreator = new BarcodeEcpDataCreator.Builder(expected).build();
        assertNotNull(ecpDataCreator);

        byte[] actual = ecpDataCreator.create();
        assertArrayEquals(expected, actual);
    }
}