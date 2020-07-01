package ru.ppr.cppk.ui.fragment;

import android.app.Activity;
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

import java.util.Locale;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.logger.Logger;

public class EnterDayCodeFragment extends FragmentParent {


    public static final String FRAGMENT_TAG = EnterDayCodeFragment.class.getSimpleName();
    private static final String TAG = Logger.makeLogTag(EnterDayCodeFragment.class);

    private static final long DELAY_FOR_SHOW_KEYBOARD = 400;
    private View errorCodeView;
    private View currentCodeView;
    private EditText enterDayCodeView;
    private Globals globals;

    private OnFragmentInteractionListener onFragmentInteractionListener;

    private Holder<PrivateSettings> privateSettingsHolder;

    public static EnterDayCodeFragment newInstance() {
        EnterDayCodeFragment fragment = new EnterDayCodeFragment();
        return fragment;
    }

    public interface OnFragmentInteractionListener {
        void onDayCodeChanged();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            onFragmentInteractionListener = (OnFragmentInteractionListener) activity;
        }
    }

    @Override
    public void onDetach() {
        onFragmentInteractionListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_enter_data, null);
        if (view == null)
            return super.onCreateView(inflater, container, savedInstanceState);

        globals = (Globals) getActivity().getApplication();

        AQuery aq = new AQuery(view);

        aq.id(R.id.enter_data_current_value_title).text(R.string.dialog_cur_day_code)
                .id(R.id.enter_data_new_value_title).text(R.string.dialog_new_day_code)
                .id(R.id.enter_data_error_message).text(R.string.dialog_incorrect_day_code)
                .id(R.id.enter_data_new_value).getEditText().setHint(R.string.dialog_enter_new_day_code);


        errorCodeView = aq.id(R.id.enter_day_code_error_day_code_layout).getView();
        currentCodeView = aq.id(R.id.enter_day_code_current_day_code_layout).getView();
        enterDayCodeView = aq.id(R.id.enter_data_new_value).getEditText();
        enterDayCodeView.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    saveDayCode();
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
        aq.id(R.id.enter_data_current_value_value).text(String.format(Locale.getDefault(), "%04d", currentDayCode));

        return view;
    }

    private void saveDayCode() {

        String dayCodeString = enterDayCodeView.getText().toString();
        if (dayCodeString == null || dayCodeString.isEmpty()) {
            showErrorMessage();
            return;
        }

        Integer dayCode;
        try {
            dayCode = Integer.valueOf(dayCodeString);
        } catch (NumberFormatException e) {
            Logger.error(TAG, "Day code is wrong");
            showErrorMessage();
            return;
        }

        PrivateSettings privateSettings = new PrivateSettings(privateSettingsHolder.get());
        privateSettings.setDayCode(dayCode);
        Dagger.appComponent().privateSettingsRepository().savePrivateSettings(privateSettings);
        privateSettingsHolder.set(privateSettings);

        if (onFragmentInteractionListener != null) {
            onFragmentInteractionListener.onDayCodeChanged();
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
