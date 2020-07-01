import android.util.Log;

import com.google.common.base.Strings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

import ru.ppr.core.dataCarrier.smartCard.entity.PersonalData;
import ru.ppr.core.dataCarrier.smartCard.parser.PersonalDataParser;
import ru.ppr.utils.CommonUtils;


import org.junit.runner.RunWith;

import static org.junit.Assert.*;



import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;



import static org.junit.Assert.*;


public class PersonalDataParserTest {

    @Test
    public void checkPersonalData() {
        PersonalDataParser personalDataParser = new PersonalDataParser();

        // заполнение нулевыми значениями
        String zeroes = Strings.repeat("0", 192);

        byte [] b = CommonUtils.hexStringToByteArray(zeroes);
        PersonalData data = personalDataParser.parse(b);
        System.out.println(data);
    }

    @Test
    public void checkNullData(){
        PersonalDataParser personalDataParser = new PersonalDataParser();

        PersonalData data = personalDataParser.parse(null);
        System.out.println(data.getName());
    }

    @Test
    public void checkPersonalBirth() throws ParseException {
        PersonalDataParser personalDataParser = new PersonalDataParser();

        String str = "19990109";
        byte [] birth = str.getBytes(Charset.forName("ASCII"));


        byte [] b = CommonUtils.hexStringToByteArray(Strings.repeat("0", 192));
        System.arraycopy(birth, 0, b,39, birth.length);
        b[38] = (byte) birth.length;

        PersonalData data = personalDataParser.parse(b);
        System.out.println(data.getBirthDate());
        assertThat(data.getBirthDate(),
                is((new SimpleDateFormat("yyyyMMdd")).parse(str)));
    }

    @Test
    public void checkPersonalBirthWithoutStrLen() throws ParseException {
        PersonalDataParser personalDataParser = new PersonalDataParser();

        String str = "19990109";
        byte [] birth = str.getBytes(Charset.forName("ASCII"));


        byte [] b = CommonUtils.hexStringToByteArray(Strings.repeat("0", 192));
        System.arraycopy(birth, 0, b,39, birth.length);

        PersonalData data = personalDataParser.parse(b);
        System.out.println(data.getBirthDate());
        assertThat(data.getBirthDate(),
                is(not((new SimpleDateFormat("yyyyMMdd")).parse(str))));
    }

    // проверка, что в данных персональных данных казались чужие/ломанные данные
    @Test
    public void checkPersonalRandom() throws ParseException {
        PersonalDataParser personalDataParser = new PersonalDataParser();

        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 100; i++) {
            byte[] bytes = new byte[96];
            random.nextBytes(bytes);
            CommonUtils.bytesToHexWithoutSpaces(bytes);

            PersonalData data = personalDataParser.parse(bytes);

            System.out.println(data.getBirthDate());
        }
        System.out.println("ends.");
    }


}
