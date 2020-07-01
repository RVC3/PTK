package ru.ppr.cppk.utils;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import ru.ppr.cppk.helpers.TicketTypeValidityTimeChecker;
import ru.ppr.nsi.entity.TicketTypesValidityTimes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Артем on 24.03.2016.
 */
@RunWith(RobolectricTestRunner.class)
@SmallTest
public class CppkUtilsTest {

    @Test
    public void testTicketCategoryIsProceed_Valid() throws Exception {

        final int validFromSecond = 28800; // 8:00:00
        final int validToSecond = 72000; // 20:00:00
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);

        TicketTypesValidityTimes validityTimes = new TicketTypesValidityTimes(1, validFromSecond, validToSecond);
        assertTrue(new TicketTypeValidityTimeChecker().isTicketTypeAllowedForTime(Collections.singletonList(validityTimes), calendar.getTime()));
    }

    @Test
    public void testTicketCategoryIsProceed_Before() throws Exception {

        final int validFromSecond = 28800; // 8:00:00
        final int validToSecond = 72000; // 20:00:00
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 5);

        TicketTypesValidityTimes validityTimes = new TicketTypesValidityTimes(1, validFromSecond, validToSecond);
        assertFalse(new TicketTypeValidityTimeChecker().isTicketTypeAllowedForTime(Collections.singletonList(validityTimes), calendar.getTime()));
    }

    @Test
    public void testTicketCategoryIsProceed_After() throws Exception {

        final int validFromSecond = 28800; // 8:00:00
        final int validToSecond = 72000; // 20:00:00
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 22);

        TicketTypesValidityTimes validityTimes = new TicketTypesValidityTimes(1, validFromSecond, validToSecond);
        assertFalse(new TicketTypeValidityTimeChecker().isTicketTypeAllowedForTime(Collections.singletonList(validityTimes), calendar.getTime()));
    }

    @Test
    public void testTicketCategoryIsProceed_ValidEmptyList() throws Exception {
        assertTrue(new TicketTypeValidityTimeChecker().isTicketTypeAllowedForTime(Collections.emptyList(), new Date()));
    }

    @Test
    public void testEqualsRfidAttrForString() throws Exception {
        String fistAttr = "28 a5 17 57";
        String secondAttr = "28 A5 17 57";
        assertTrue(CppkUtils.equalsRfidAttr(fistAttr, secondAttr));
    }

    @Test
    public void testEqualsRfidAttrForByte() throws Exception {

        byte[] first = new byte[]{0x11, 0x12, 0x13, 0x14};
        byte[] second = new byte[]{0x11, 0x12, 0x13, 0x14};
        assertTrue(CppkUtils.equalsRfidAttr(first, second));
    }

    @Test
    public void testEqualsRfidAttrForStringNotEquals() throws Exception {
        String fistAttr = "28 a5 17 57";
        String secondAttr = "28 A5 17 58";
        assertFalse(CppkUtils.equalsRfidAttr(fistAttr, secondAttr));
    }

    @Test
    public void testEqualsRfidAttrForByteNotEquals() throws Exception {

        byte[] first = new byte[]{0x11, 0x12, 0x13, 0x14};
        byte[] second = new byte[]{0x11, 0x12, 0x13, 0x15};
        assertFalse(CppkUtils.equalsRfidAttr(first, second));
    }

    @Test
    public void testEqualsRfidCrystalNumber() throws Exception {
        String fistAttr = "154789354257451";
        String secondAttr = "154789354257451";
        assertTrue(CppkUtils.equalsRfidCrystalNumber(fistAttr, secondAttr));
    }

    @Test
    public void testEqualsRfidCrystalNumberNotEquals() throws Exception {
        String fistAttr = "154789354257451";
        String secondAttr = "54789664485";
        assertFalse(CppkUtils.equalsRfidCrystalNumber(fistAttr, secondAttr));
    }

    @Test
    public void testDatesInOneDay() throws Exception {
        Calendar firstDate = Calendar.getInstance();
        firstDate.set(2016, 3, 22, 23, 58);
        Calendar secondDate = Calendar.getInstance();
        secondDate.set(2016, 3, 22, 23, 59);
        assertTrue(CppkUtils.datesInOneDay(firstDate.getTime(), secondDate.getTime()));
    }

    @Test
    public void testDatesInOneDayDifferentDays() throws Exception {
        Calendar firstDate = Calendar.getInstance();
        firstDate.set(2016, 3, 22, 23, 58);
        Calendar secondDate = Calendar.getInstance();
        secondDate.set(2016, 3, 23, 0, 1);
        assertFalse(CppkUtils.datesInOneDay(firstDate.getTime(), secondDate.getTime()));
    }

    @Test
    public void testDatesInOneDayDifferentMonth() throws Exception {
        Calendar firstDate = Calendar.getInstance();
        firstDate.set(2016, 3, 22, 23, 58);
        Calendar secondDate = Calendar.getInstance();
        secondDate.set(2016, 4, 23, 23, 58);
        assertFalse(CppkUtils.datesInOneDay(firstDate.getTime(), secondDate.getTime()));
    }

    @Test
    public void testDatesInOneDayDifferentYears() throws Exception {
        Calendar firstDate = Calendar.getInstance();
        firstDate.set(2017, 3, 23, 23, 58);
        Calendar secondDate = Calendar.getInstance();
        secondDate.set(2016, 3, 23, 23, 58);
        assertFalse(CppkUtils.datesInOneDay(firstDate.getTime(), secondDate.getTime()));
    }
}