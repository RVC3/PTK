package ru.ppr.cppk.utils.validators;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class CyrillicTextWatcher implements TextWatcher {

    private static final String CYRILLIC_PATTERN = "^[а-яА-ЯёЁ0-9-.*'\\s()\\[\\]]{0,50}";

    private final EditText textView;
    private String lastCorrectString = "";

    public CyrillicTextWatcher(EditText textView) {
        this.textView = textView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {

        String text = textView.getText().toString();

        if (!text.isEmpty() && !text.matches(CYRILLIC_PATTERN)) {
            textView.setText(lastCorrectString.trim());
        } else {
            lastCorrectString = text;
        }
    }

}
