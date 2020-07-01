package ru.ppr.cppk.printer;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.TextStyle;

/**
 * Класс-помощник для форматирования текста.
 *
 * @author Aleksandr Brazhkin
 */
public class TextFormatter {

    private static final String DELIMITER = "----------------------------------------";

    private final IPrinter printer;
    private final String bigDelimiter;
    private final String smallDelimiter;

    public TextFormatter(IPrinter printer) {
        this.printer = printer;
        this.bigDelimiter = DELIMITER.substring(0, printer.getWidthForTextStyle(TextStyle.TEXT_NORMAL));
        this.smallDelimiter = DELIMITER.substring(0, 3);
    }

    /**
     * Возвращает ширину билетной ленты в символах для указанного стиля текста.
     *
     * @param textStyle Стиль текста
     * @return Ширина билетной ленты в символах
     */
    public int getWidthForTextStyle(TextStyle textStyle) {
        return printer.getWidthForTextStyle(textStyle);
    }

    /**
     * Выравнивает текст по краям.
     *
     * @param first  Первое слово, будет с левой стороны
     * @param second Второе слово, будет с правой стороны
     * @return Строка с выравниванием по краям
     */
    public String alignWidthText(String first, String second) {
        return alignWidthText(first, second, TextStyle.TEXT_NORMAL);
    }

    /**
     * Выравнивает текст по краям.
     *
     * @param first  Первое слово, будет с левой стороны
     * @param second Второе слово, будет с правой стороны
     * @return Строка с выравниванием по краям
     */
    public String alignWidthFiscalText(String first, String second) {
        return alignWidthText(first, second, TextStyle.FISCAL_NORMAL);
    }

    /**
     * Выравнивает текст по краям.
     *
     * @param first     Первое слово, будет с левой стороны
     * @param second    Второе слово, будет с правой стороны
     * @param textStyle Стиль текста
     * @return Строка с выравниванием по краям
     */
    public String alignWidthText(String first, String second, TextStyle textStyle) {
        return alignWidthText(first, second, getWidthForTextStyle(textStyle));
    }

    /**
     * Выравнивает текст по краям.
     *
     * @param first     Первое слово, будет с левой стороны
     * @param second    Второе слово, будет с правой стороны
     * @param tapeWidth Ширина билетной ленты в символах
     * @return Строка с выравниванием по краям
     */
    public String alignWidthText(String first, String second, int tapeWidth) {

        StringBuilder alignString = new StringBuilder();

        int countSpace = tapeWidth - (first.length() + second.length());

        if (countSpace < 0) {
            return alignString.append(first).append(" ").append(second).substring(0, tapeWidth);
        }

        alignString.append(first);
        for (int i = 0; i < countSpace; i++) {
            alignString.append(" ");
        }
        alignString.append(second);

        return alignString.toString();
    }

    /**
     * Выравнивает строку по центру.
     *
     * @param text Текст
     * @return Строка с выравниванием по центру
     */
    public String alignCenterText(String text) {
        return alignCenterText(text, TextStyle.TEXT_NORMAL);
    }

    /**
     * Перенос строки по словам
     *
     * @param text      - текст
     * @param textStyle - стиль
     * @return - список строк, каждая из которые влязит на одну строку в ленте
     */
    public String[] wordWrapText(final String text, TextStyle textStyle) {
        int rowLength = getWidthForTextStyle(textStyle);
        String wordWrapString = StringUtils.wordWrap(text, rowLength, Locale.getDefault());
        return wordWrapString.split("/n");
    }

    /**
     * Выравнивает строку по центру.
     *
     * @param text Текст
     * @return Строка с выравниванием по центру
     */
    public String alignCenterFiscalText(String text) {
        return alignCenterText(text, TextStyle.FISCAL_NORMAL);
    }

    /**
     * Выравнивает строку по центру.
     *
     * @param text      Текст
     * @param textStyle Стиль текста
     * @return Строка с выравниванием по центру
     */
    public String alignCenterText(String text, TextStyle textStyle) {
        return alignCenterText(text, getWidthForTextStyle(textStyle));
    }

    /**
     * Выравнивает строку по центру.
     *
     * @param text      Текст
     * @param tapeWidth Ширина билетной ленты в символах
     * @return Строка с выравниванием по центру
     */
    public String alignCenterText(String text, int tapeWidth) {

        if (text.length() > tapeWidth) {
            return text.substring(0, tapeWidth);
        }

        StringBuilder builder = new StringBuilder();

        int textLen = text.length();
        int countSpace = tapeWidth - textLen;
        int leftCountSpace = countSpace / 2;
        //добавляем пробелы слева
        for (int i = 0; i < leftCountSpace; i++) {
            builder.insert(0, " ");
        }
        //добавляем текст
        builder.append(text);
        //добавляем пробелы справа
        for (int i = leftCountSpace + textLen; i < tapeWidth; i++) {
            builder.append(" ");
        }

        return builder.toString();
    }

    public String asDate_dd_MM_yyyy_HH_mm_ss(Date dateTime) {

        if (dateTime == null)
            return "";

        String format = "dd.MM.yyyy HH:mm:ss";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(dateTime);
    }

    public String asDate_dd_MM_yyyy_HH_mm(Date dateTime) {

        if (dateTime == null)
            return "";

        String format = "dd.MM.yyyy HH:mm";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(dateTime);
    }

    public String asDate_dd_MM_yyyy(Date dateTime) {

        if (dateTime == null)
            return "";

        String format = "dd.MM.yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(dateTime);
    }

    public String asDate_dd_MMM_yyyy(Date dateTime) {
        if (dateTime == null)
            return "";

        String format = "dd MMM yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(dateTime);
    }

    public String asDate_HH_mm_ss(Date dateTime) {

        if (dateTime == null)
            return "";

        String format = "HH:mm:ss";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(dateTime);
    }

    public String asStr010d(long number) {
        return String.format(Locale.getDefault(), "%010d", number);
    }

    public String asDayCodeNumber(int number) {
        return String.format(Locale.getDefault(), "%04d", number);
    }

    public String asStr06d(int number) {
        return String.format(Locale.getDefault(), "%06d", number);
    }

    public String asStr02d(int number) {
        return String.format(Locale.getDefault(), "%02d", number);
    }

    public String asStr8s(String str) {
        return String.format(Locale.getDefault(), "%8s", str);
    }

    public String asStr(long number) {
        return String.format(Locale.getDefault(), "%d", number);
    }

    public String asMoney(BigDecimal value) {
        return String.format(Locale.ENGLISH, "%1.2f", value);
    }

    public String asProductItem(String name, BigDecimal sum) {
        return alignWidthFiscalText(name, String.format(Locale.getDefault(), "=%8s", asMoney(sum)));
    }

    public String asMaskedBankCardNumber(String cardPan) {
        // Почему-то терминал может вернуть не 4-значный CardPan,
        // поэтому форсированно берем последние 4 символа
        String tmp = cardPan;
        if (tmp != null && tmp.length() > 4) {
            tmp = tmp.substring(tmp.length() - 4, tmp.length());
        }
        return "************" + tmp;
    }

    public String bigDelimiter() {
        return bigDelimiter;
    }

    public String smallDelimiter() {
        return smallDelimiter;
    }
}
