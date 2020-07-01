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

public class SetStopListDayActionFragment extends FragmentParent {

    private static final long DELAY_FOR_SHOW_KEYBOARD = 400;
    private View errorCodeView;
    private View currentCodeView;
    private EditText enterDayCodeView;
    private Globals globals;

    private Holder<PrivateSettings> privateSettingsHolder;

    public static Fragment newInstance() {
        return new SetStopListDayActionFragment();
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

        aq.id(R.id.enter_data_current_value_title).text(R.string.dialog_set_stopList_day_count)
                .id(R.id.enter_data_new_value_title).text(R.string.dialog_new_stop_list_action_days_title)
                .id(R.id.enter_data_error_message).text(R.string.dialog_new_stop_list_action_days_error)
                .id(R.id.enter_data_new_value).getEditText().setHint(R.string.dialog_new_stop_list_action_days);

        errorCodeView = aq.id(R.id.enter_day_code_error_day_code_layout).getView();
        currentCodeView = aq.id(R.id.enter_day_code_current_day_code_layout).getView();
        enterDayCodeView = aq.id(R.id.enter_data_new_value).getEditText();
        enterDayCodeView.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    saveStopListdayAction();
                    return true;
                }
                return false;
            }
        });
        enterDayCodeView.requestFocus();
        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        PrivateSettings privateSettings = privateSettingsHolder.get();
        int currentDayCode = privateSettings.getDayCode();
        aq.id(R.id.enter_data_current_value_value).text(String.valueOf(currentDayCode));

        return view;
    }

    private void saveStopListdayAction() {
        String countString = enterDayCodeView.getText().toString();
        if (countString == null || countString.isEmpty()) {
            showErrorMessage();
            return;
        }

        Integer count;
        try {
            count = Integer.valueOf(countString);
        } catch (NumberFormatException e) {
            Logger.info("EnterDayCodeFragment", "Day code is wrong");
            showErrorMessage();
            return;
        }

        PrivateSettings privateSettings = new PrivateSettings(privateSettingsHolder.get());
        privateSettings.setStopListValidTime(count);
        Dagger.appComponent().privateSettingsRepository().savePrivateSettings(privateSettings);
        privateSettingsHolder.set(privateSettings);

        if (getFragmentManager().getBackStackEntryCount() == 0) {
            //если фрагментов в стеке нету, то это значит что активити запущено только для ввода кода дня
            //и после удачного ввода необходимо закрыть активити
            getActivity().finish();
        } else {
            //если же фрагментов больше 0, то просто удаляем верхний фрагмент
            getFragmentManager().popBackStack();
        }
    }

    private void showErrorMessage() {
        errorCodeView.setVisibility(View.VISIBLE);
        currentCodeView.setVisibility(View.GONE);
        showKeyboard();
    }

    private void showKeyboard() {
        enterDayCodeView.postDelayed(new Runnable() {

            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(enterDayCodeView, InputMethodManager.SHOW_FORCED);
            }
        }, DELAY_FOR_SHOW_KEYBOARD);
    }

}
