package ru.ppr.cppk.utils.validators;

import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

/**
 * Класс фабрика для создания определенных типов фильтров
 *
 * @author A.Ushakov
 */
public class ConcreteInputFilter implements InputFilter {

    private static final String PATTERN_LETTER_SPACE_DOT = "([a-zA-Zа-яА-Я\\s.])";
    private static final String PATTERN_CYRILIC_AND_DIGIT = "([а-яА-Я0-9])";
    private static final String PATTERN_DIGIT = "([0-9])";

    private final String pattern;

    private ConcreteInputFilter(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        if (source instanceof Spanned) {
            SpannableStringBuilder sourceAsSpannableBuilder = new SpannableStringBuilder(source);
            for (int i = end - 1; i >= start; i--) {
                String charString = Character.toString(source.charAt(i));
                if (!charString.matches(pattern)) {
                    sourceAsSpannableBuilder.delete(i, i + 1);
                }
            }
            return source;
        } else {
            StringBuilder filteredStringBuilder = new StringBuilder();
            for (int i = start; i < end; i++) {
                char currentChar = source.charAt(i);
                String charString = Character.toString(source.charAt(i));
                if (charString.matches(pattern)) {
                    filteredStringBuilder.append(currentChar);
                }
            }
            return filteredStringBuilder.toString();
        }
    }

    /**
     * Возвращает фильтр для кириллических и латинских букв, точки и пробела
     *
     * @return
     */
    public static InputFilter getFilterForLetterDotSpace() {
        return new ConcreteInputFilter(PATTERN_LETTER_SPACE_DOT);
    }

    /**
     * Возвращает фильтр только для заглавных кирилических букв и цифр
     *
     * @return
     */
    public static InputFilter getFilterForCyrilicLetterAndDigit() {
        return new ConcreteInputFilter(PATTERN_CYRILIC_AND_DIGIT);
    }

    /**
     * Возвращает фильтр только для цифр
     *
     * @return
     */
    public static InputFilter getFilterForDigit() {
        return new ConcreteInputFilter(PATTERN_DIGIT);
    }

}
