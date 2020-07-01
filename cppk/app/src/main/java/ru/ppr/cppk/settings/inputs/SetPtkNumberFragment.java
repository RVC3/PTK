package ru.ppr.cppk.settings.inputs;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.androidquery.AQuery;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.logger.Logger;

public class SetPtkNumberFragment extends FragmentParent {

    private static final long DELAY_FOR_SHOW_KEYBOARD = 400;
    private View errorView;
    private View currentTerminalNumberView;
    private EditText enterTerminalNumber;
    private Globals globals;

    private Holder<PrivateSettings> privateSettingsHolder;

    public static Fragment newInstance() {
        return new SetPtkNumberFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        globals = (Globals) getActivity().getApplication();

        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();

        View view = inflater.inflate(R.layout.fragment_enter_data, container, false);

        AQuery aq = new AQuery(view);

        aq.id(R.id.enter_data_current_value_title).text(R.string.dialog_cur_ptk_number)
                .id(R.id.enter_data_new_value_title).text(R.string.dialog_new_ptk_number)
                .id(R.id.enter_data_error_message).text(R.string.dialog_not_valid_msg)
                .id(R.id.enter_data_new_value).getEditText();

        errorView = aq.id(R.id.enter_day_code_error_day_code_layout).getView();
        currentTerminalNumberView = aq.id(R.id.enter_day_code_current_day_code_layout).getView();
        enterTerminalNumber = aq.id(R.id.enter_data_new_value).getEditText();
        enterTerminalNumber.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    saveTerminalNumber();
                    return true;
                }
                return false;
            }
        });
        enterTerminalNumber.requestFocus();
        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        PrivateSettings privateSettings = privateSettingsHolder.get();
        long terminalNumber = privateSettings.getTerminalNumber();
        aq.id(R.id.enter_data_current_value_value).text(String.valueOf(terminalNumber));

        return view;
    }

    private void saveTerminalNumber() {
        String numberString = enterTerminalNumber.getText().toString();
        if (numberString == null || numberString.isEmpty()) {
            showErrorMessage();
            return;
        }

        long number;
        try {
            long longValue = Long.valueOf(numberString);
            if (longValue >> 32 != 0) {
                showErrorMessage();
                return;
            }
            number = longValue;
        } catch (NumberFormatException e) {
            Logger.info("EnterDayCodeFragment", "Terminal orderNumber is wrong");
            showErrorMessage();
            return;
        }

        PrivateSettings privateSettings = new PrivateSettings(privateSettingsHolder.get());
        privateSettings.setTerminalNumber(number);
        Dagger.appComponent().privateSettingsRepository().savePrivateSettings(privateSettings);
        privateSettingsHolder.set(privateSettings);

        getActivity().finish();
    }

    private void showErrorMessage() {
        errorView.setVisibility(View.VISIBLE);
        currentTerminalNumberView.setVisibility(View.GONE);
        showKeyboard();
    }

    private void showKeyboard() {
        enterTerminalNumber.postDelayed(new Runnable() {

            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(enterTerminalNumber, InputMethodManager.SHOW_FORCED);
            }
        }, DELAY_FOR_SHOW_KEYBOARD);
    }

}
