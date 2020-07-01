package ru.ppr.cppk.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import ru.ppr.core.dataCarrier.smartCard.entity.PersonalData;
import ru.ppr.core.dataCarrier.smartCard.parser.PersonalDataParser;
import ru.ppr.core.logic.FioFormatter;
import ru.ppr.utils.CommonUtils;

/**
 * @author Grigoriy Kashka
 */
@RunWith(RobolectricTestRunner.class)
public class PersonalDataTest {

    @Test
    public void testPersonalDataParse() {
        PersonalDataParser pdp = new PersonalDataParser();
        PersonalData pd = pdp.parse(CommonUtils.hexStringToByteArray("21CAD3CBC8CACEC2C02020202020202020202020202020202020202020202020202000816600C43139333130393137006E20C3C0CBC8CDC020C8CBDCC8CDC8D7CDC0202020202020202020202020202020202020202020202020202020202020"));

        Assert.assertEquals(pd.getSurname(), "КУЛИКОВА");
        Assert.assertEquals(pd.getName(), "ГАЛИНА");
        Assert.assertEquals(pd.getSecondName(), "ИЛЬИНИЧНА");

        FioFormatter fioFormatter = new FioFormatter();

        Assert.assertEquals(fioFormatter.getFullNameAsSurnameWithInitials("Фамилия", "Имя", "Отчетсво"), "Фамилия И. О.");
        Assert.assertEquals(fioFormatter.getFullNameAsSurnameWithInitials("Фамилия", "", "Отчетсво"), "Фамилия О.");
        Assert.assertEquals(fioFormatter.getFullNameAsSurnameWithInitials("Фамилия", "Имя", ""), "Фамилия И.");
        Assert.assertEquals(fioFormatter.getFullNameAsSurnameWithInitials("", "Имя", "Отчетсво"), "И. О.");
        Assert.assertEquals(fioFormatter.getFullNameAsSurnameWithInitials("Фамилия", "", ""), "Фамилия");
        Assert.assertEquals(fioFormatter.getFullNameAsSurnameWithInitials("", "Имя", ""), "И.");
        Assert.assertEquals(fioFormatter.getFullNameAsSurnameWithInitials("", "", "Отчетсво"), "О.");
        Assert.assertEquals(fioFormatter.getFullNameAsSurnameWithInitials("", "", ""), "");

    }

}
