package ru.ppr.cppk.logic;


import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.CheckDao;
import ru.ppr.cppk.db.local.ShiftEventDao;
import ru.ppr.cppk.db.local.TestTicketDao;
import ru.ppr.cppk.entity.event.base34.TestTicketEvent;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.localdb.model.ShiftEvent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Игнорим тесты до лучших времен:
 * https://github.com/powermock/powermock/issues/768
 * Created by Артем on 11.01.2016.
 */
@RunWith(RobolectricTestRunner.class)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*", "*.*"})
@PrepareForTest(ShiftManager.class)
@SmallTest
public class ShiftManagerTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Ignore
    @Test
    public void testIsShiftOpened() throws Exception {

        /**
         * Смена должна быть открыта, т.к. статуст == ShiftStatus.STARTED
         */

        ShiftEvent shiftEvent = mock(ShiftEvent.class);
        when(shiftEvent.getStatus()).thenReturn(ShiftEvent.Status.STARTED);

        ShiftEventDao shiftEventDao = mock(ShiftEventDao.class);
        when(shiftEventDao.getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES)).thenReturn(shiftEvent);

        TestTicketDao testTicketDao = mock(TestTicketDao.class);
        when(testTicketDao.getFirstTestTicketForShift(null, null)).thenReturn(new TestTicketEvent());

        LocalDaoSession localDaoSession = mock(LocalDaoSession.class);
        when(localDaoSession.getShiftEventDao()).thenReturn(shiftEventDao);
        when(localDaoSession.getTestTicketDao()).thenReturn(testTicketDao);

        ShiftAlarmManager shiftAlarmManager = mock(ShiftAlarmManager.class);
        PrivateSettings privateSettings = mock(PrivateSettings.class);
        ShiftManager shiftManager = new ShiftManager(null, shiftAlarmManager);
        ShiftManager spy = PowerMockito.spy(shiftManager);
        PowerMockito.doReturn(localDaoSession).when(spy, "getLocalDaoSession");
        PowerMockito.doReturn(privateSettings).when(spy, "getPrivateSettings");
        spy.refreshState();
        assertTrue(spy.isShiftOpened());
    }

    @Ignore
    @Test
    public void testIsShiftClosed() throws Exception {

        /**
         * Смена должна быть закрыта, т.к. статус == ShiftStatus.ENDED
         */

        ShiftEvent shiftEvent = mock(ShiftEvent.class);
        when(shiftEvent.getStatus()).thenReturn(ShiftEvent.Status.ENDED);

        ShiftEventDao shiftEventDao = mock(ShiftEventDao.class);
        when(shiftEventDao.getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES)).thenReturn(shiftEvent);

        TestTicketDao testTicketDao = mock(TestTicketDao.class);
        when(testTicketDao.getFirstTestTicketForShift("", null)).thenReturn(new TestTicketEvent());

        LocalDaoSession localDaoSession = mock(LocalDaoSession.class);
        when(localDaoSession.getShiftEventDao()).thenReturn(shiftEventDao);
        when(localDaoSession.getTestTicketDao()).thenReturn(testTicketDao);

        ShiftAlarmManager shiftAlarmManager = mock(ShiftAlarmManager.class);
        PrivateSettings privateSettings = mock(PrivateSettings.class);
        ShiftManager shiftManager = new ShiftManager(null, shiftAlarmManager);
        ShiftManager spy = PowerMockito.spy(shiftManager);
        PowerMockito.doReturn(localDaoSession).when(spy, "getLocalDaoSession");
        PowerMockito.doReturn(privateSettings).when(spy, "getPrivateSettings");
        spy.refreshState();
        assertFalse(spy.isShiftOpened());
    }

    @Ignore
    @Test
    public void testIsShiftClosedEventNull() throws Exception {

        /**
         * Смена должна быть закрыта т.к. событие смены == null
         */

        ShiftEventDao shiftEventDao = mock(ShiftEventDao.class);
        when(shiftEventDao.getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES)).thenReturn(null);

        TestTicketDao testTicketDao = mock(TestTicketDao.class);
        when(testTicketDao.getFirstTestTicketForShift("", null)).thenReturn(new TestTicketEvent());

        LocalDaoSession localDaoSession = mock(LocalDaoSession.class);
        when(localDaoSession.getShiftEventDao()).thenReturn(shiftEventDao);
        when(localDaoSession.getTestTicketDao()).thenReturn(testTicketDao);

        ShiftAlarmManager shiftAlarmManager = mock(ShiftAlarmManager.class);
        PrivateSettings privateSettings = mock(PrivateSettings.class);
        ShiftManager shiftManager = new ShiftManager(null, shiftAlarmManager);
        ShiftManager spy = PowerMockito.spy(shiftManager);
        PowerMockito.doReturn(localDaoSession).when(spy, "getLocalDaoSession");
        PowerMockito.doReturn(privateSettings).when(spy, "getPrivateSettings");
        spy.refreshState();
        assertFalse(spy.isShiftOpened());
    }

    @Ignore
    @Test
    public void testIsShiftOpenedWithTestPd() throws Exception {

        /**
         * Результат должен быть равен false, т.к. смена должна открыта, тестовый билет напечатан
         */

        ShiftEvent shiftEvent = mock(ShiftEvent.class);
        when(shiftEvent.getStatus()).thenReturn(ShiftEvent.Status.STARTED);
        when(shiftEvent.getStartTime()).thenReturn(new Date(0));
        when(shiftEvent.getShiftId()).thenReturn("");

        ShiftEventDao shiftEventDao = mock(ShiftEventDao.class);
        when(shiftEventDao.getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES)).thenReturn(shiftEvent);

        TestTicketDao testTicketDao = mock(TestTicketDao.class);
        when(testTicketDao.getFirstTestTicketForShift("", null)).thenReturn(new TestTicketEvent());

        LocalDaoSession localDaoSession = mock(LocalDaoSession.class);
        when(localDaoSession.getShiftEventDao()).thenReturn(shiftEventDao);
        when(localDaoSession.getTestTicketDao()).thenReturn(testTicketDao);

        ShiftAlarmManager shiftAlarmManager = mock(ShiftAlarmManager.class);
        PrivateSettings privateSettings = mock(PrivateSettings.class);
        ShiftManager shiftManager = new ShiftManager(null, shiftAlarmManager);
        ShiftManager spy = PowerMockito.spy(shiftManager);
        PowerMockito.doReturn(localDaoSession).when(spy, "getLocalDaoSession");
        PowerMockito.doReturn(privateSettings).when(spy, "getPrivateSettings");
        spy.refreshState();
        assertTrue(spy.isShiftOpenedWithTestPd());
    }

    @Ignore
    @Test
    public void testIsShiftOpenedWithOutTestPD() throws Exception {

        /**
         * Результат должен быть равен false, т.к. тестовый ПД НЕ напечатан
         */

        ShiftEvent shiftEvent = mock(ShiftEvent.class);
        when(shiftEvent.getStatus()).thenReturn(ShiftEvent.Status.STARTED);
        when(shiftEvent.getStartTime()).thenReturn(new Date(0));
        when(shiftEvent.getShiftId()).thenReturn("");

        ShiftEventDao shiftEventDao = mock(ShiftEventDao.class);
        when(shiftEventDao.getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES)).thenReturn(shiftEvent);

        TestTicketDao testTicketDao = mock(TestTicketDao.class);
        when(testTicketDao.getFirstTestTicketForShift("", null)).thenReturn(null);

        LocalDaoSession localDaoSession = mock(LocalDaoSession.class);
        when(localDaoSession.getShiftEventDao()).thenReturn(shiftEventDao);
        when(localDaoSession.getTestTicketDao()).thenReturn(testTicketDao);

        ShiftAlarmManager shiftAlarmManager = mock(ShiftAlarmManager.class);
        PrivateSettings privateSettings = mock(PrivateSettings.class);
        ShiftManager shiftManager = new ShiftManager(null, shiftAlarmManager);
        ShiftManager spy = PowerMockito.spy(shiftManager);
        PowerMockito.doReturn(localDaoSession).when(spy, "getLocalDaoSession");
        PowerMockito.doReturn(privateSettings).when(spy, "getPrivateSettings");
        spy.refreshState();
        assertFalse(spy.isShiftOpenedWithTestPd());
    }

    @Ignore
    @Test
    public void testIsShiftClosedWithTestPd() throws Exception {

        /**
         * Результат должен быть false, т.к. смена закрыта
         */

        ShiftEvent shiftEvent = mock(ShiftEvent.class);
        when(shiftEvent.getStatus()).thenReturn(ShiftEvent.Status.ENDED);
        when(shiftEvent.getStartTime()).thenReturn(new Date(0));

        ShiftEventDao shiftEventDao = mock(ShiftEventDao.class);
        when(shiftEventDao.getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES)).thenReturn(shiftEvent);

        CheckDao checkDao = mock(CheckDao.class);

        LocalDaoSession localDaoSession = mock(LocalDaoSession.class);
        when(localDaoSession.getShiftEventDao()).thenReturn(shiftEventDao);
        when(localDaoSession.getCheckDao()).thenReturn(checkDao);

        ShiftAlarmManager shiftAlarmManager = mock(ShiftAlarmManager.class);
        PrivateSettings privateSettings = mock(PrivateSettings.class);
        ShiftManager shiftManager = new ShiftManager(null, shiftAlarmManager);
        ShiftManager spy = PowerMockito.spy(shiftManager);
        PowerMockito.doReturn(localDaoSession).when(spy, "getLocalDaoSession");
        PowerMockito.doReturn(privateSettings).when(spy, "getPrivateSettings");
        spy.refreshState();
        assertFalse(spy.isShiftOpenedWithTestPd());
    }
}