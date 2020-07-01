package ru.ppr.cppk.utils.validators;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Pattern;

public class KirilicInputFilter implements InputFilter {

    private static final String KitilicPattern = "[а-яА-ЯёЁ0-9.\\s\\-\\*\\']+$";
    private static final String PATTERN_TWO = "[^\\n]";

    private KirilicInputFilter() {
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        String textToCheck = source.subSequence(0, start).toString()
                + source.subSequence(start, end)
                + dest.subSequence(dend, dest.length()).toString();

        // Entered text does not match the pattern
        if (!Pattern.matches(PATTERN_TWO, textToCheck)) {
            return "";
        }

        return null;
    }

    public static KirilicInputFilter[] getKirilicInputFilters() {
        return new KirilicInputFilter[]{new KirilicInputFilter()};
    }

}
