package ru.ppr.cppk.pd.check.write;

import org.junit.Test;

import java.util.Date;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.cppk.dataCarrier.entity.PD;

import static org.junit.Assert.assertFalse;

/**
 * Created by Артем on 01.03.2016.
 */
public class TicketCapCheckerTest {

    @Test
    public void testPerformCheck() throws Exception {
        PD pd = new PD(PdVersion.V64.getCode(), 16);
        TicketCapChecker capChecker = new TicketCapChecker();
        assertFalse(capChecker.performCheck(pd, new Date()));
    }
}