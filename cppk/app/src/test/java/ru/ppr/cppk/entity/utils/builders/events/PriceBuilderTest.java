package ru.ppr.cppk.entity.utils.builders.events;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import ru.ppr.cppk.localdb.model.Price;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Artem Ushakov
 */
public class PriceBuilderTest {

    private Price mockPrice;

    @Before
    public void setUp() {
        mockPrice = new Price();
        mockPrice.setFull(new BigDecimal("50"));
        mockPrice.setNds(new BigDecimal("2"));
        mockPrice.setPayed(new BigDecimal("50"));
        mockPrice.setSumForReturn(BigDecimal.ZERO);
    }

    @Test
    public void testBuild() throws Exception {
        Price actualPrice = new PriceBuilder()
                .setFull(mockPrice.getFull())
                .setPayed(mockPrice.getPayed())
                .setSumForReturn(mockPrice.getSumForReturn())
                .setNds(mockPrice.getNds())
                .build();

        assertNotNull(actualPrice);
        assertEquals(mockPrice, actualPrice);
    }

    @Test(expected = NullPointerException.class)
    public void testNullFull() {
        new PriceBuilder()
                .setPayed(mockPrice.getPayed())
                .setSumForReturn(mockPrice.getSumForReturn())
                .setNds(mockPrice.getNds())
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void testNullPayed() {
        new PriceBuilder()
                .setFull(mockPrice.getFull())
                .setSumForReturn(mockPrice.getSumForReturn())
                .setNds(mockPrice.getNds())
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void testNullNds() {
        new PriceBuilder()
                .setFull(mockPrice.getFull())
                .setPayed(mockPrice.getPayed())
                .setSumForReturn(mockPrice.getSumForReturn())
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void testNullSumForReturn() {
        new PriceBuilder()
                .setFull(mockPrice.getFull())
                .setPayed(mockPrice.getPayed())
                .setNds(mockPrice.getNds())
                .build();
    }
}