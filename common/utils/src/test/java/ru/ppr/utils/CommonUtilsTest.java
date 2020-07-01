package ru.ppr.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Артем on 11.01.2016.
 */
public class CommonUtilsTest {

    private final HashMap<Long, byte[]> byteToLongData = new HashMap<>();
    private final HashMap<Integer, byte[]> byteToIntData = new HashMap<>();
    private final Map<String, byte[]> hexStringToByteArrayData = new HashMap<>();
    private final Map<String, byte[]> byteArrayToString = new HashMap<>();
    private final Map<String, Boolean> snilsTestCase = new HashMap<>();
    private final Map<String, byte[]> bcdToStringTestData = new HashMap<>();
    private final Map<BitSet, byte[]> bitSetData = new HashMap<>();

    @Before
    public void setUp() throws Exception {

        byteToLongData.put(383L, new byte[]{127, 01});
        byteToLongData.put(40054L, new byte[]{118, (byte) 156});
        byteToLongData.put(360663041L, new byte[]{01, 72, 127, 21});

        byteToIntData.put(383, new byte[]{127, 01});
        byteToIntData.put(40054, new byte[]{118, (byte) 156});

        hexStringToByteArrayData.put("2a573c085ee829958782de93d83f8e831994d758",
                new byte[]{42, 87, 60, 8, 94, -24, 41, -107, -121, -126, -34, -109, -40, 63, -114, -125, 25, -108, -41, 88});
        hexStringToByteArrayData.put("373c57e85ee82995f5f0de6dd8428e0019f6d758",
                new byte[]{55, 60, 87, -24, 94, -24, 41, -107, -11, -16, -34, 109, -40, 66, -114, 0, 25, -10, -41, 88});
        hexStringToByteArrayData.put("B9 91 99 C5 00 00 00 00",
                new byte[]{(byte) 0xB9, (byte) 0x91, (byte) 0x99, (byte) 0xC5, 0x00, 0x00, 0x00, 0x00});

        byteArrayToString.put("07 07 00 00 54 84 3e 54 57 0d 03 00 00 00 00 00 59 47 00 00 00 00 00 00 ",
                new byte[]{7, 7, 0, 0, 84, -124, 62, 84, 87, 13, 3, 0, 0, 0, 0, 0, 89, 71, 0, 0, 0, 0, 0, 0});
        byteArrayToString.put("05 0f 00 01 46 a2 53 54 6d 0d 03 00 00 00 13 00 00 00 00 00 00 00 ff 07 80 00 b0 b1 b2 b3 b4 b5 ",
                new byte[]{5, 15, 0, 1, 70, -94, 83, 84, 109, 13, 3, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, -1, 7, -128, 0, -80, -79, -78, -77, -76, -75});
        byteArrayToString.put("22 10 0e 36 af 72 d2 6f 07 8c 7a 20 8f ab f3 65 ",
                new byte[]{34, 16, 14, 54, -81, 114, -46, 111, 7, -116, 122, 32, -113, -85, -13, 101});

        snilsTestCase.put("15747954655", false);
        snilsTestCase.put("15347154755", false);
        snilsTestCase.put("99999999901", true);
        snilsTestCase.put("14227318335", true);
        snilsTestCase.put("12012012048", true);
        snilsTestCase.put("12026350700", true);
        snilsTestCase.put("1202635070", false);
        snilsTestCase.put("157-479-546 55", false);
        snilsTestCase.put("153-471-547 55", false);
        snilsTestCase.put("999-999-999 01", true);
        snilsTestCase.put("142-273-183 35", true);
        snilsTestCase.put("120-120-120 48", true);
        snilsTestCase.put("120-263-507 00", true);
        snilsTestCase.put("157 - 479 - 546 55", false);
        snilsTestCase.put("153 - 471 - 547 55", false);
        snilsTestCase.put("999 - 999 - 999 01", true);
        snilsTestCase.put("142 - 273 - 183 35", true);
        snilsTestCase.put("120 - 120 - 120 48", true);
        snilsTestCase.put("120 - 263 - 507 00", true);
        snilsTestCase.put("15267513870", true);
        snilsTestCase.put("15022366719", true);
        snilsTestCase.put("1245", false);
        snilsTestCase.put(null, false);
        snilsTestCase.put("", false);

        bcdToStringTestData.put("0015", new byte[]{00, 0x15});
        bcdToStringTestData.put("0002819997", new byte[]{00, 0x02, (byte) 0x81, (byte) 0x99, (byte) 0x97});
        bcdToStringTestData.put("0002728847", new byte[]{00, 0x02, 0x72, (byte) 0x88, 0x47});
        bcdToStringTestData.put("0002819975", new byte[]{00, 0x02, (byte) 0x81, (byte) 0x99, 0x75});

        BitSet bitSet = new BitSet(16);
        bitSet.set(2);
        bitSet.set(3);
        bitSet.set(6);
        bitSet.set(9);
        bitSet.set(10);
        bitSet.set(14);
        bitSetData.put(bitSet, new byte[]{0x4c, 0x46});

        BitSet bitSet2 = new BitSet(8);
        bitSet2.set(0);
        bitSet2.set(2);
        bitSet2.set(4);
        bitSet2.set(6);
        bitSetData.put(bitSet2, new byte[]{0x55});

        BitSet bitSet3 = new BitSet(24);
        bitSet3.set(0);
        bitSet3.set(8);
        bitSet3.set(16);
        bitSetData.put(bitSet3, new byte[]{0x01, 0x01, 0x01});
    }

    @After
    public void tearDown() throws Exception {
        byteToIntData.clear();
        byteToLongData.clear();
        hexStringToByteArrayData.clear();
        snilsTestCase.clear();
    }

    @Test
    public void testConvertByteToLong() {
        for (Map.Entry<Long, byte[]> entry : byteToLongData.entrySet()) {
            long expected = entry.getKey();
            long actual = CommonUtils.convertByteToLong(entry.getValue(), ByteOrder.LITTLE_ENDIAN);
            assertEquals("assert expected = " + expected + ", actual = " + actual, expected, actual);
        }
    }

    @Test
    public void testConvertByteToInt() {
        for (Map.Entry<Integer, byte[]> entry : byteToIntData.entrySet()) {
            long expected = entry.getKey();
            long actual = CommonUtils.convertByteToLong(entry.getValue(), ByteOrder.LITTLE_ENDIAN);
            assertEquals("assert expected = " + expected + ", actual = " + actual, expected, actual);
        }
    }

    @Test
    public void testHexStringToByteArray() {
        for (Map.Entry<String, byte[]> itemEntry : hexStringToByteArrayData.entrySet()) {
            byte[] expected = itemEntry.getValue();
            byte[] actual = CommonUtils.hexStringToByteArray(itemEntry.getKey());
            assertTrue(Arrays.equals(expected, actual));
        }
    }

    @Test
    public void testByteArrayToString() {
        for (Map.Entry<String, byte[]> itemEntry : byteArrayToString.entrySet()) {
            String expected = itemEntry.getKey();
            String actual = CommonUtils.byteArrayToString(itemEntry.getValue());
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testSnilsCheck() {
        for (Map.Entry<String, Boolean> entry : snilsTestCase.entrySet()) {
            String snilsNumber = entry.getKey();
            boolean actual = CommonUtils.checkSnils(snilsNumber);
            assertEquals("Check for snils number - " + snilsNumber, entry.getValue(), actual);
        }
    }

    @Test
    public void testMakeCorrectString() {

    }

    @Test
    public void testConvertUidToNumber() {

    }

    @Test
    public void testBscToString() {
        for (Map.Entry<String, byte[]> itemEntry : bcdToStringTestData.entrySet()) {
            String expected = itemEntry.getKey();
            String actual = CommonUtils.BCDtoString(itemEntry.getValue());
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testByteToBitset() {

        for (Map.Entry<BitSet, byte[]> entry : bitSetData.entrySet()) {
            BitSet expected = entry.getKey();
            byte[] data = entry.getValue();
            BitSet actual = CommonUtils.toBitSet(data);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testBytesToHex() {
        for (Map.Entry<String, byte[]> itemEntry : byteArrayToString.entrySet()) {
            String expected = itemEntry.getKey().replace(" ", "");
            String actual = CommonUtils.bytesToHex(itemEntry.getValue());
            assertEquals(expected.toLowerCase(), actual.toLowerCase());
        }
    }

    @Test
    public void testBytesToHexWithSpaces() throws Exception {
        for (Map.Entry<String, byte[]> itemEntry : byteArrayToString.entrySet()) {
            String expected = itemEntry.getKey();
            String actual = CommonUtils.bytesToHexWithSpaces(itemEntry.getValue());
            assertEquals(expected.toLowerCase(), actual.toLowerCase());
        }
    }
}