package ru.ppr.ikkm.file.db;

import android.test.suitebuilder.annotation.SmallTest;
import android.util.SparseArray;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import ru.ppr.ikkm.file.state.model.PrinterSettings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * Created by Артем on 22.01.2016.
 */
@RunWith(RobolectricTestRunner.class)
@SmallTest
@Config(manifest = Config.NONE)
public class PrinterSettingDaoTest {

    PrinterSettingDao dao = null;
    private static final long PRINTER_ID = 12345678;
    private static final String PRINTER_ONE_MODEL = "model1";
    private static final long OTHER_PRINTER_ID = 654321;
    private static final String PRINTER_TWO_MODEL = "model2";

    @Before
    public void setUp() throws Exception {
        PrinterSQLiteHelper openHelper = new PrinterSQLiteHelper(RuntimeEnvironment.application);
        PrinterDaoSession printerDaoSession = new PrinterDaoSession(openHelper);
        dao = printerDaoSession.getPrinterSettingDao();
    }

    @After
    public void tearDown() throws Exception {
        dao.getPrinterDaoSession().getDatabase().rawQuery("select 'drop table ' || name || ';' " +
                "from sqlite_master " +
                " where type = 'table';", null);
        dao.getPrinterDaoSession().getDatabase().close();
    }

    @Test
    public void testEmptyTable(){

        PrinterSettings expected = new PrinterSettings(PRINTER_ID, PRINTER_ONE_MODEL);
        PrinterSettings actual = dao.loadFirst(PRINTER_ID, PRINTER_ONE_MODEL);
        assertEquals(expected, actual);
    }

    @Test
    public void testLoadFirst() throws Exception {

        List<String> headers = Arrays.asList("First", "Second", "Third", "Fourth", "Fifth");

        PrinterSettings printerSettings = new PrinterSettings(PRINTER_ID, PRINTER_ONE_MODEL);
        printerSettings.setHeaderLines(headers);
        printerSettings.setSerialNumber("12345");
        printerSettings.setCheckNumber(5);
        printerSettings.setShiftNumber(4);

        long id = dao.saveOrUpdate(printerSettings);
        assertTrue("Error insert to database" ,id != -1);

        PrinterSettings actual = dao.loadFirst(PRINTER_ID, PRINTER_ONE_MODEL);
        assertEquals(printerSettings, actual);
    }

    @Test
    public void testSaveOrUpdate() throws Exception {

        List<String> headers = Arrays.asList("First", "Second", "Third", "Fourth", "Fifth");

        SparseArray<Integer> vatTable = new SparseArray<>(2);
        vatTable.append(0,0);
        vatTable.append(3, 100);

        PrinterSettings printerSettings = new PrinterSettings(PRINTER_ID, PRINTER_ONE_MODEL);
        printerSettings.setHeaderLines(headers);
        printerSettings.setSerialNumber("12345");
        printerSettings.setCheckNumber(5);
        printerSettings.setShiftNumber(4);
        printerSettings.setRegisterNumber("123");
        printerSettings.setInn("555");
        printerSettings.setVatTable(vatTable);
        printerSettings.setOdometerValue(15487);
        printerSettings.setAvailableDocs(34);
        printerSettings.setAvailableShifts(88);

        //добавим новую запись, чтобы было что обновлять
        long id = dao.saveOrUpdate(printerSettings);
        assertTrue("Error insert to database" ,id != -1);

        // проверим правильно ли она сохранилась
        PrinterSettings actual = dao.loadFirst(PRINTER_ID, PRINTER_ONE_MODEL);
        assertEquals(printerSettings, actual);

        // обновим запись
        printerSettings.setShiftNumber(6);
        printerSettings.setCheckNumber(7);
        printerSettings.setSerialNumber("54321");
        dao.saveOrUpdate(printerSettings);

        //проверим коррекность обновления
        actual = dao.loadFirst(PRINTER_ID, PRINTER_ONE_MODEL);
        assertEquals(printerSettings, actual);
    }

    @Test
    public void testForDifferentId() throws Exception {

        // Запишем состояник для первого ид
        List<String> headers = Arrays.asList("First", "Second", "Third", "Fourth", "Fifth");

        PrinterSettings printerSettings = new PrinterSettings(PRINTER_ID, PRINTER_ONE_MODEL);
        printerSettings.setHeaderLines(headers);
        printerSettings.setSerialNumber("12345");
        printerSettings.setCheckNumber(5);
        printerSettings.setShiftNumber(4);

        long id = dao.saveOrUpdate(printerSettings);
        assertTrue("Error insert to database", id != -1);

        // Запишем состояние для второго ид
        List<String> headerTwo = Arrays.asList("First", "Third", "Second", "Fourth", "Fifth");

        PrinterSettings printerSettingsTwo = new PrinterSettings(OTHER_PRINTER_ID, PRINTER_TWO_MODEL);
        printerSettingsTwo.setHeaderLines(headerTwo);
        printerSettingsTwo.setSerialNumber("5432178");
        printerSettingsTwo.setCheckNumber(55);
        printerSettingsTwo.setShiftNumber(40);

        long idTwo = dao.saveOrUpdate(printerSettingsTwo);
        assertTrue("Error insert to database", idTwo != -1);

        PrinterSettings printerSettingsOneFromDb = dao.getPrinterDaoSession().getPrinterSettingDao()
                .loadFirst(PRINTER_ID, PRINTER_ONE_MODEL);
        PrinterSettings printerSettingsTwoFromDb = dao.getPrinterDaoSession().getPrinterSettingDao()
                .loadFirst(OTHER_PRINTER_ID, PRINTER_TWO_MODEL);

        assertEquals(printerSettings, printerSettingsOneFromDb);
        assertEquals(printerSettingsTwo, printerSettingsTwoFromDb);
    }
}