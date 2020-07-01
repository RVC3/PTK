package ru.ppr.cppk.settings.inputs;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
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

public class SetTimeForCancellation extends FragmentParent {

    private static final long DELAY_FOR_SHOW_KEYBOARD = 400;
    private View errorView;
    private View currentTimeView;
    private EditText enterCancellationTime;
    private Globals globals;

    private Holder<PrivateSettings> privateSettingsHolder;

    public static Fragment newInstance() {
        return new SetTimeForCancellation();
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

        aq.id(R.id.enter_data_current_value_title).text(R.string.dialog_cur_time_for_annulate)
                .id(R.id.enter_data_new_value_title).text(R.string.dialog_new_time_for_annulate)
                .id(R.id.enter_data_error_message).text(R.string.dialog_not_valid_msg)
                .id(R.id.enter_data_new_value).getEditText().setHint(R.string.dialog_ime_for_annulate);

        errorView = aq.id(R.id.enter_day_code_error_day_code_layout).getView();
        currentTimeView = aq.id(R.id.enter_day_code_current_day_code_layout).getView();
        enterCancellationTime = aq.id(R.id.enter_data_new_value).getEditText();
        enterCancellationTime.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                saveCancellationTime();
                return true;
            }
            return false;
        });
        enterCancellationTime.requestFocus();
        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        PrivateSettings privateSettings = privateSettingsHolder.get();
        int cancellationTime = privateSettings.getTimeForAnnulate();
        aq.id(R.id.enter_data_current_value_value).text(String.valueOf(cancellationTime));

        return view;
    }

    private void saveCancellationTime() {
        String countString = enterCancellationTime.getText().toString();
        if (countString.isEmpty()) {
            showErrorMessage();
            return;
        }

        Integer time;
        try {
            time = Integer.valueOf(countString);
        } catch (NumberFormatException e) {
            Logger.info("EnterDayCodeFragment", "Cancellation pd value is wrong");
            showErrorMessage();
            return;
        }

        PrivateSettings privateSettings = new PrivateSettings(privateSettingsHolder.get());
        privateSettings.setTimeForAnnulate(time);
        Dagger.appComponent().privateSettingsRepository().savePrivateSettings(privateSettings);
        privateSettingsHolder.set(privateSettings);

        getActivity().finish();
    }

    private void showErrorMessage() {
        errorView.setVisibility(View.VISIBLE);
        currentTimeView.setVisibility(View.GONE);
        showKeyboard();
    }

    private void showKeyboard() {
        enterCancellationTime.postDelayed(() -> {
            InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(enterCancellationTime, InputMethodManager.SHOW_FORCED);
        }, DELAY_FOR_SHOW_KEYBOARD);
    }
}
