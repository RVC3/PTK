package ru.ppr.cppk.ui.widget.textWatchers;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by Александр on 18.02.2016.
 */
public class NumberTextWatcher implements TextWatcher {

    private boolean mFormatting;

    public NumberTextWatcher() {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!mFormatting) {
            mFormatting = true;
            String ss = s.toString();
            String newSs = ss.replaceAll("(?:^№)|[^\\d]", "");
            if (!newSs.isEmpty()) {
                newSs = "№ " + newSs;
            }
            s.replace(0, ss.length(), newSs);
            mFormatting = false;
        }

    }

}
