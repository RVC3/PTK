package ru.ppr.cppk.pd.check.write;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.interactor.PdValidityPeriodCalculator;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.repository.TicketTypeRepository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by Артем on 16.02.2016.
 */
@RunWith(RobolectricTestRunner.class)
public class OneOffAndSeasonForPeriodCheckerTest {

    @Test
    public void testPerformCheck_valid() throws Exception {
        Date currentDate = new Date(1455634800000L); //2016-02-16T15:00:00+00:00 in ISO 8601

        Date startDate = new Date(1455620040000L); //2016-02-16T10:54:00+00:00 in ISO 8601

        TicketType ticketType = mock(TicketType.class);

        Globals globals = mock(Globals.class);
        NsiDaoSession nsiDaoSession = mock(NsiDaoSession.class);
        when(globals.getNsiDaoSession()).thenReturn(nsiDaoSession);
        CommonSettings commonSettings = new CommonSettings();

        Tariff tariff = mock(Tariff.class);
        when(tariff.getTicketTypeCode()).thenReturn(1);
        TicketTypeRepository ticketTypeRepository = mock(TicketTypeRepository.class);
        when(ticketTypeRepository.load(1, 0)).thenReturn(ticketType);

        PdValidityPeriodCalculator pdValidityPeriodCalculator = mock(PdValidityPeriodCalculator.class);
        when(pdValidityPeriodCalculator.calcValidityPeriod(startDate, TicketWayType.OneWay, ticketType, 0)).thenReturn(1);

        TicketCategoryChecker ticketCategoryChecker = mock(TicketCategoryChecker.class);
        when(ticketCategoryChecker.isSingleTicket(-1)).thenReturn(true);

        NsiVersionManager nsiVersionManager = mock(NsiVersionManager.class);
        when(nsiVersionManager.getCurrentNsiVersionId()).thenReturn(0);

        PD pd = spy(new PD(PdVersion.V5.getCode(), 18));
        pd.saleDatetimePD = startDate;
        pd.term = 0;
        pd.wayType = TicketWayType.OneWay;
        when(pd.getTariff()).thenReturn(tariff);

        OneOffAndSeasonForPeriodChecker seasonChecker = new OneOffAndSeasonForPeriodChecker(ticketTypeRepository, commonSettings, pdValidityPeriodCalculator, nsiVersionManager, ticketCategoryChecker);
        assertTrue(seasonChecker.performCheck(pd, currentDate));
    }

    @Test
    public void testPerformCheck_invalid() throws Exception {
        Date currentDate = new Date(1456185600000L); //2016-02-23T00:00:00+00:00 in ISO 8601

        Date startDate = new Date(1455619335000L); //2016-02-16T10:42:15+00:00 in ISO 8601

        TicketType ticketType = mock(TicketType.class);

        Globals globals = mock(Globals.class);
        NsiDaoSession nsiDaoSession = mock(NsiDaoSession.class);
        when(globals.getNsiDaoSession()).thenReturn(nsiDaoSession);
        CommonSettings commonSettings = new CommonSettings();

        Tariff tariff = mock(Tariff.class);
        when(tariff.getTicketTypeCode()).thenReturn(1);
        TicketTypeRepository ticketTypeRepository = mock(TicketTypeRepository.class);
        when(ticketTypeRepository.load(1, 0)).thenReturn(ticketType);

        PdValidityPeriodCalculator pdValidityPeriodCalculator = mock(PdValidityPeriodCalculator.class);
        when(pdValidityPeriodCalculator.calcValidityPeriod(startDate, TicketWayType.OneWay, ticketType, 0)).thenReturn(1);

        TicketCategoryChecker ticketCategoryChecker = mock(TicketCategoryChecker.class);
        when(ticketCategoryChecker.isSingleTicket(-1)).thenReturn(true);

        NsiVersionManager nsiVersionManager = mock(NsiVersionManager.class);
        when(nsiVersionManager.getCurrentNsiVersionId()).thenReturn(0);

        PD pd = spy(new PD(PdVersion.V5.getCode(), 18));
        pd.saleDatetimePD = startDate;
        pd.term = 0;
        pd.wayType = TicketWayType.OneWay;
        when(pd.getTariff()).thenReturn(tariff);

        OneOffAndSeasonForPeriodChecker seasonChecker = new OneOffAndSeasonForPeriodChecker(ticketTypeRepository, commonSettings, pdValidityPeriodCalculator, nsiVersionManager, ticketCategoryChecker);
        assertFalse(seasonChecker.performCheck(pd, currentDate));
    }

    @Test
    public void testPerformCheckForSeasonTicket_valid() throws Exception {

        Date currentDate = new Date(1455620040000L); //2016-02-16T10:54:00+00:00 in ISO 8601

        Date startDate = new Date(1455619335000L); //2016-02-16T10:42:15+00:00 in ISO 8601

        TicketType ticketType = mock(TicketType.class);

        Globals globals = mock(Globals.class);
        NsiDaoSession nsiDaoSession = mock(NsiDaoSession.class);
        when(globals.getNsiDaoSession()).thenReturn(nsiDaoSession);

        Tariff tariff = mock(Tariff.class);
        when(tariff.getTicketTypeCode()).thenReturn(1);
        TicketTypeRepository ticketTypeRepository = mock(TicketTypeRepository.class);
        when(ticketTypeRepository.load(1, 0)).thenReturn(ticketType);

        PdValidityPeriodCalculator pdValidityPeriodCalculator = mock(PdValidityPeriodCalculator.class);
        when(pdValidityPeriodCalculator.calcValidityPeriod(startDate, TicketWayType.OneWay, ticketType, 0)).thenReturn(1);

        TicketCategoryChecker ticketCategoryChecker = mock(TicketCategoryChecker.class);
        when(ticketCategoryChecker.isSingleTicket(-1)).thenReturn(true);

        NsiVersionManager nsiVersionManager = mock(NsiVersionManager.class);
        when(nsiVersionManager.getCurrentNsiVersionId()).thenReturn(0);

        PD pd = spy(new PD(PdVersion.V5.getCode(), 18));
        pd.saleDatetimePD = startDate;
        pd.term = 0;
        when(pd.getTariff()).thenReturn(tariff);
        CommonSettings commonSettings = new CommonSettings();

        OneOffAndSeasonForPeriodChecker seasonChecker = new OneOffAndSeasonForPeriodChecker(ticketTypeRepository, commonSettings, pdValidityPeriodCalculator, nsiVersionManager, ticketCategoryChecker);
        assertTrue(seasonChecker.performCheck(pd, currentDate));
    }

    @Test
    public void testPerformCheckForSeasonTicket_invalid() throws Exception {

        Date currentDate = new Date(1460246400000L); //2016-04-10T00:00:00+00:00 in ISO 8601

        Date startDate = new Date(1455619335000L); //2016-02-16T10:42:15+00:00 in ISO 8601

        TicketType ticketType = mock(TicketType.class);

        Globals globals = mock(Globals.class);
        NsiDaoSession nsiDaoSession = mock(NsiDaoSession.class);
        when(globals.getNsiDaoSession()).thenReturn(nsiDaoSession);

        Tariff tariff = mock(Tariff.class);
        when(tariff.getTicketTypeCode()).thenReturn(1);
        TicketTypeRepository ticketTypeRepository = mock(TicketTypeRepository.class);
        when(ticketTypeRepository.load(1, 0)).thenReturn(ticketType);

        PdValidityPeriodCalculator pdValidityPeriodCalculator = mock(PdValidityPeriodCalculator.class);
        when(pdValidityPeriodCalculator.calcValidityPeriod(startDate, TicketWayType.OneWay, ticketType, 0)).thenReturn(1);

        TicketCategoryChecker ticketCategoryChecker = mock(TicketCategoryChecker.class);
        when(ticketCategoryChecker.isSingleTicket(-1)).thenReturn(true);

        NsiVersionManager nsiVersionManager = mock(NsiVersionManager.class);
        when(nsiVersionManager.getCurrentNsiVersionId()).thenReturn(0);

        PD pd = spy(new PD(PdVersion.V5.getCode(), 18));
        pd.saleDatetimePD = startDate;
        pd.term = 0;
        when(pd.getTariff()).thenReturn(tariff);
        CommonSettings commonSettings = new CommonSettings();

        OneOffAndSeasonForPeriodChecker seasonChecker = new OneOffAndSeasonForPeriodChecker(ticketTypeRepository, commonSettings, pdValidityPeriodCalculator, nsiVersionManager, ticketCategoryChecker);
        assertFalse(seasonChecker.performCheck(pd, currentDate));
    }
}