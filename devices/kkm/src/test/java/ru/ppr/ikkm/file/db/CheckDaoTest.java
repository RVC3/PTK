package ru.ppr.ikkm.file.db;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.file.state.model.Check;
import ru.ppr.ikkm.file.state.model.Item;
import ru.ppr.ikkm.file.state.model.Shift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by Артем on 29.03.2016.
 */
@RunWith(RobolectricTestRunner.class)
@SmallTest
public class CheckDaoTest {

    PrinterDaoSession printerDaoSession;

    @Before
    public void setUp() throws Exception {

        PrinterSQLiteHelper printerSQLiteHelper = new PrinterSQLiteHelper(RuntimeEnvironment.application);
        printerDaoSession = new PrinterDaoSession(printerSQLiteHelper);
    }

    @After
    public void tearDown() throws Exception {
        printerDaoSession.getDatabase().rawQuery("select 'drop table ' || name || ';' " +
                "from sqlite_master " +
                " where type = 'table';", null);
        printerDaoSession.getDatabase().close();
    }

    @Test
    @Ignore
    public void testSaveAndLoad() throws Exception {

        Shift shift = mock(Shift.class);
        when(shift.getId()).thenReturn(125L);

        ShiftDao shiftDao = mock(ShiftDao.class);
        when(shiftDao.load(125L)).thenReturn(shift);

        final PrinterDaoSession printerDaoSessionSpy = spy(printerDaoSession);
        doReturn(shiftDao).when(printerDaoSessionSpy).getShiftDao();

        final CheckDao spyCheckDao = new CheckDao(printerDaoSessionSpy);


        Item item = new Item();
        item.setTotal(BigDecimal.valueOf(15.8));
        item.setDiscount(BigDecimal.ZERO);
        item.setNds(BigDecimal.ZERO);
        item.setSum(BigDecimal.valueOf(154.37));
        item.setGoodDescription("description");

        Check check = new Check();
        check.setPayment(BigDecimal.valueOf(102.08));
        check.setPaymentMethod(IPrinter.PaymentType.CARD);
        check.setPrintTime(new Date(1459261628L));
        check.setSpdnNumber(50);
        check.setTotal(BigDecimal.valueOf(154.37));
        check.setType(IPrinter.DocType.SALE);
        check.setItems(Collections.singletonList(item));
        check.setShift(shift);
        final long newId = spyCheckDao.save(check);
        assertTrue(newId != -1);

        final Check actualCheck = spyCheckDao.load(newId);
        assertNotNull(actualCheck);
        assertEquals(check, actualCheck);
    }
}