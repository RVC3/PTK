package ru.ppr.core.dataCarrier.smartCard.parser;

import android.support.annotation.NonNull;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ppr.core.dataCarrier.smartCard.entity.EttData;
import ru.ppr.logger.Logger;

/**
 * Парсер данных ЭТТ.
 *
 * @author Dmitry Nevolin
 */
public class EttDataParser {

    private static final String TAG = Logger.makeLogTag(EttDataParser.class);

    private static final char[] KOI7_N2 = {
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
            0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
            0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F,
            0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,
            0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F,
            'Ю', 'А', 'Б', 'Ц', 'Д', 'Е', 'Ф', 'Г', 'Х', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О',
            'П', 'Я', 'Р', 'С', 'Т', 'У', 'Ж', 'В', 'Ь', 'Ы', 'З', 'Ш', 'Э', 'Щ', 'Ч', 0x7F
    };

    private static final SimpleDateFormat BIRTH_DATE_FORMAT = new SimpleDateFormat("ddMMyyyy", Locale.US);
    private static final Pattern CHECK_SUM_PATTERN = Pattern.compile("\\(.*?\\)");

    public EttDataParser() {

    }

    /**
     * Парсит данные ЭТТ.
     *
     * @param data Данные ЭТТ
     * @return Данные ЭТТ
     */
    @NonNull
    public EttData parse(@NonNull byte[] data) {
        EttData ettData = new EttData();
        Matcher checkSumPatternMatcher = CHECK_SUM_PATTERN.matcher(new String(data, Charset.forName("ASCII")));
        //должно быть 2 блока подходящих под паттерн.
        //в первом блоке: шифр категории пассажира, код билетной группы, код организации, номер требования, шифр льготы "Экспресс"
        if (checkSumPatternMatcher.find()) {
            String block = checkSumPatternMatcher.group();
            String[] splitBlock = checkSumPatternMatcher.group().substring(1, block.length() - 1).split("-");

            if (splitBlock.length < 4)
                throw new RuntimeException("Ошибка формата блока данных ЭТТ");

            //шифр категории пассажира
            ettData.setPassengerCategoryCode(decodeKOI7(splitBlock[0]));
            //код билетной группы
            ettData.setDivisionCode(decodeKOI7(splitBlock[1]));
            //код организации
            ettData.setOrganizationCode(decodeKOI7(splitBlock[2]));
            //номер требования
            ettData.setEttNumber(decodeKOI7(splitBlock[3]));
            //шифр льготы "Экспресс" - пока не используется,
            //в связи с чем не входит в перечень обязательных и обрабатывается отдельно
            if (splitBlock.length > 4)
                ettData.setExemptionExpressCode(decodeKOI7(splitBlock[4]));
        }
        //во втором блоке: ФИО пассажира, дата рождения пассажира, государство выдачи документа, место рождения пассажира,
        //пол пассажира, фаимилия (инициалы работника), код СНИЛС
        if (checkSumPatternMatcher.find()) {
            String block = checkSumPatternMatcher.group();
            String[] splitBlock = checkSumPatternMatcher.group().substring(1, block.length() - 1).split("/");

            if (splitBlock.length < 5)
                throw new RuntimeException("Ошибка формата блока данных ЭТТ");

            //ФИО пассажира
            String[] FIO = splitBlock[0].split("=");
            //фамилия, нужно декодировать из КОИ-7
            ettData.setSurname(decodeKOI7(FIO[0]));
            //имя, нужно декодировать из КОИ-7
            ettData.setFirstName(decodeKOI7(FIO[1]));
            //отчество, нужно декодировать из КОИ-7
            ettData.setSecondName(decodeKOI7(FIO[2]));
            //дата рождения пассажира
            try {
                ettData.setBirthDate(BIRTH_DATE_FORMAT.parse(splitBlock[1]));
            } catch (ParseException e) {
                Logger.error(TAG, e);
            }
            //государство выдачи документа и место рождения пассажира находятся в одном блоке,
            //надо дополнительно парсить
            String thirdBlock = splitBlock[2];
            int divider = thirdBlock.lastIndexOf("[");
            //государство выдачи документа
            ettData.setDocumentIssuingCountry(thirdBlock.substring(0, divider));
            //место рождения пассажира, нужно декодировать из КОИ-7
            ettData.setBirthPlace(decodeKOI7(thirdBlock.substring(divider + 1, thirdBlock.length() - 1)));
            //пол пассажира, нужно декодировать из КОИ-7
            ettData.setGender(decodeKOI7(splitBlock[3]));
            //фаимилия (инициалы работника) может отсутствовать, тогда код СНИЛС будет на его месте
            String fifthBlock = splitBlock[4];
            //код СНИЛС начинается со звёздочки *
            if (fifthBlock.startsWith("*")) {
                //код СНИЛС
                ettData.setSnilsCode(fifthBlock.substring(1));
            } else if (splitBlock.length > 5) {
                //фаимилия (инициалы работника), нужно декодировать из КОИ-7
                ettData.setGuardianFio(decodeKOI7(fifthBlock.replace("=", " ")));
                //код СНИЛС
                ettData.setSnilsCode(splitBlock[5].substring(1));
            }
        }

        return ettData;
    }

    private static String decodeKOI7(String target) {
        String result = "";

        for (char _char : target.toCharArray())
            if (_char < KOI7_N2.length)
                result += KOI7_N2[_char];

        return result;
    }
}
