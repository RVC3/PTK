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

public class SetCloseShiftAttentionPeriodFragment extends FragmentParent {

    private static final long DELAY_FOR_SHOW_KEYBOARD = 400;
    private View errorView;
    private View currentPeriodView;
    private EditText enterPeriodView;
    private Globals globals;

    private Holder<PrivateSettings> privateSettingsHolder;

    public static Fragment newInstance() {
        return new SetCloseShiftAttentionPeriodFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        globals = (Globals) getActivity().getApplication();
        View view = inflater.inflate(R.layout.fragment_enter_data, container, false);

        AQuery aq = new AQuery(view);

        aq.id(R.id.enter_data_current_value_title).text(R.string.dialog_cur_time_to_close_shift_message)
                .id(R.id.enter_data_new_value_title).text(R.string.dialog_new_time_to_close_shift_message)
                .id(R.id.enter_data_error_message).text(R.string.dialog_not_valid_msg);

        errorView = aq.id(R.id.enter_day_code_error_day_code_layout).getView();
        currentPeriodView = aq.id(R.id.enter_day_code_current_day_code_layout).getView();
        enterPeriodView = aq.id(R.id.enter_data_new_value).getEditText();
        enterPeriodView.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    savePeriod();
                    return true;
                }
                return false;
            }
        });
        enterPeriodView.requestFocus();
        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        PrivateSettings privateSettings = privateSettingsHolder.get();
        int currentPeriod = privateSettings.getTimeForShiftCloseMessage();
        aq.id(R.id.enter_data_current_value_value).text(String.valueOf(currentPeriod));

        return view;
    }

    private void savePeriod() {
        String periodString = enterPeriodView.getText().toString();
        if (periodString == null || periodString.isEmpty()) {
            showErrorMessage();
            return;
        }

        Integer period;
        try {
            period = Integer.valueOf(periodString);
        } catch (NumberFormatException e) {
            Logger.info("EnterDayCodeFragment", "Cancellation pd value is wrong");
            showErrorMessage();
            return;
        }

        PrivateSettings privateSettings = new PrivateSettings(privateSettingsHolder.get());
        privateSettings.setTimeToCloseShiftMessage(period);
        Dagger.appComponent().privateSettingsRepository().savePrivateSettings(privateSettings);
        privateSettingsHolder.set(privateSettings);

        getActivity().finish();
    }

    private void showErrorMessage() {
        errorView.setVisibility(View.VISIBLE);
        currentPeriodView.setVisibility(View.GONE);
        showKeyboard();
    }

    private void showKeyboard() {
        enterPeriodView.postDelayed(new Runnable() {

            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(enterPeriodView, InputMethodManager.SHOW_FORCED);
            }
        }, DELAY_FOR_SHOW_KEYBOARD);
    }

}
