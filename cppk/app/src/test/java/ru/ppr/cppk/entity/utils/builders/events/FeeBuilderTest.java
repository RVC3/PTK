package ru.ppr.cppk.entity.utils.builders.events;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.math.BigDecimal;

import ru.ppr.cppk.localdb.model.Fee;
import ru.ppr.nsi.entity.FeeType;

import static org.junit.Assert.assertTrue;

/**
 * Created by Артем on 29.12.2015.
 */
@RunWith(RobolectricTestRunner.class)
@SmallTest
public class FeeBuilderTest {

    @Test
    public void testBuild() throws Exception {

        final BigDecimal total = new BigDecimal("50");
        final BigDecimal nds = new BigDecimal("6.65");
        final FeeType feeType = FeeType.BAGGAGE_IN_TRAIN;

        Fee fee = new FeeBuilder()
                .setNds(nds)
                .setTotal(total)
                .setFeeType(feeType)
                .build();

        assertTrue(total.compareTo(fee.getTotal()) == 0);
        assertTrue(nds.compareTo(fee.getNds()) == 0);
        assertTrue(feeType == fee.getFeeType());
    }

    @Test(expected = NullPointerException.class)
    public void checkNullTotal() {
        new FeeBuilder()
                .setNds(BigDecimal.ONE)
                .build();
    }

    public void checkNullNds() {
        new FeeBuilder().setTotal(new BigDecimal("50")).build();
    }
}