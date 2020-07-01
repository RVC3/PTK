package ru.ppr.cppk.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import ru.ppr.core.logic.FioFormatter;
import ru.ppr.core.logic.FioNormalizer;

/**
 * @author Grigoriy Kashka
 */
@RunWith(RobolectricTestRunner.class)
public class FioNormilizerTest {

    @Test
    public void testFioNormilize() {
        FioNormalizer fioNormalizer = new FioNormalizer(new FioFormatter());
        Assert.assertEquals(fioNormalizer.getNormalizedFio("Фамилия Имя Отчество"), "Фамилия И. О.");
        Assert.assertEquals(fioNormalizer.getNormalizedFio("Фамилия И О"), "Фамилия И. О.");
        Assert.assertEquals(fioNormalizer.getNormalizedFio("Фамилия Имя О"), "Фамилия И. О.");
        Assert.assertEquals(fioNormalizer.getNormalizedFio("Фамилия Ифывафыва Отчество."), "Фамилия И. О.");
        Assert.assertEquals(fioNormalizer.getNormalizedFio("Фамилия И. О."), "Фамилия И. О.");
        Assert.assertEquals(fioNormalizer.getNormalizedFio("    Фамилия    Имя    Отчество    "), "Фамилия И. О.");
        Assert.assertEquals(fioNormalizer.getNormalizedFio("Фамилия   Имя. Отчество.  "), "Фамилия И. О.");
        Assert.assertEquals(fioNormalizer.getNormalizedFio("Фамилия  Имя. Отчество. "), "Фамилия И. О.");
        Assert.assertEquals(fioNormalizer.getNormalizedFio("Фамилия Имя. Отчество."), "Фамилия И. О.");
        Assert.assertEquals(fioNormalizer.getNormalizedFio("Фамилия Имя. Отчество."), "Фамилия И. О.");
        Assert.assertEquals(fioNormalizer.getNormalizedFio("фамилия имя. отчество."), "Фамилия И. О.");
        Assert.assertEquals(fioNormalizer.getNormalizedFio("фамилия имя. отчество."), "Фамилия И. О.");
        Assert.assertEquals(fioNormalizer.getNormalizedFio("фамилия Имя. отчество."), "Фамилия И. О.");
        Assert.assertEquals(fioNormalizer.getNormalizedFio("фам-Лия Имя. отчество."), "Фам-Лия И. О.");

    }

}