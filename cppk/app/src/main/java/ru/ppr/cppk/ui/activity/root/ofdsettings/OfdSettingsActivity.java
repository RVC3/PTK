package ru.ppr.cppk.ui.activity.root.ofdsettings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.base.MvpActivity;

/**
 * Окно настроек соединения с ОФД
 *
 * @author Grigoriy Kashka
 */
public class OfdSettingsActivity extends MvpActivity implements OfdSettingsView {

    // region Di
    private OfdSettingsComponent component;
    // endregion
    // region Views
    private Button readButton;
    private Button writeButton;
    private EditText ipEditText;
    private EditText portEditText;
    private EditText timeoutEditText;
    private View errorLayout;
    private TextView errorTextView;
    private TextView readyTextView;
    // endregion
    //region Other
    private OfdSettingsPresenter presenter;
    private FeedbackProgressDialog progressDialog;
    //endregion

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, OfdSettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerOfdSettingsComponent.builder().appComponent(Dagger.appComponent()).activityModule(new ActivityModule(this)).build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::ofdSettingsPresenter, OfdSettingsPresenter.class);
        ///////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_debug_printer_ofd_settings);

        progressDialog = new FeedbackProgressDialog(this);
        progressDialog.setCancelable(false);

        readButton = (Button) findViewById(R.id.ofdSettingsReadBtn);
        writeButton = (Button) findViewById(R.id.ofdSettingsWriteBtn);
        ipEditText = (EditText) findViewById(R.id.ofdSettingsIpValue);
        portEditText = (EditText) findViewById(R.id.ofdSettingsPortValue);
        timeoutEditText = (EditText) findViewById(R.id.ofdSettingsTimeoutValue);
        errorLayout = findViewById(R.id.ofdSettingsErrorLayout);
        readyTextView = (TextView) findViewById(R.id.ofdSettingsReadyTextView);
        errorTextView = (TextView) findViewById(R.id.ofdSettingsErrorTextView);

        readButton.setOnClickListener(v -> presenter.onReadBtnClick());
        writeButton.setOnClickListener(v -> presenter.onWriteBtnClick(ipEditText.getText().toString(), portEditText.getText().toString(), timeoutEditText.getText().toString()));
        errorLayout.setVisibility(View.GONE);
        readyTextView.setVisibility(View.GONE);
        ///////////////////////////////////////////////////////////////////////////////////////
        presenter.initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ipEditText.postDelayed(() -> setFocus(ipEditText), 200);
    }

    @Override
    public void setIp(String ip) {
        ipEditText.setText(ip == null ? "" : ip);
    }

    @Override
    public void setPort(int port) {
        portEditText.setText(String.valueOf(port));
    }

    @Override
    public void setTimeout(int timeout) {
        timeoutEditText.setText(String.valueOf(timeout));
    }

    @Override
    public void setState(State state) {
        switch (state) {
            case ERROR_IP:
                errorTextView.setText(R.string.ofd_settings_error_ip_format);
                break;
            case ERROR_PORT:
                errorTextView.setText(R.string.ofd_settings_error_port_format);
                break;
            case ERROR_TIMEOUT:
                errorTextView.setText(R.string.ofd_settings_error_timeout_format);
                break;
            case ERROR_GET_DATA:
                errorTextView.setText(R.string.ofd_settings_error_get_data);
                break;
            case ERROR_SET_DATA:
                errorTextView.setText(R.string.ofd_settings_error_set_data);
                break;
        }

        readyTextView.setVisibility(state == State.SUCCESS ? View.VISIBLE : View.GONE);
        errorLayout.setVisibility(state == State.DEFAULT || state == State.SUCCESS ? View.GONE : View.VISIBLE);
        hideKeyboard();
    }

    @Override
    public void showProgress() {
        progressDialog.show();
    }

    @Override
    public void hideProgress() {
        progressDialog.dismiss();
    }

    /**
     * перемещает курсор в нужное поле ввода
     *
     * @param editText
     */
    private void setFocus(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(ipEditText.getWindowToken(), 0);
        inputMethodManager.hideSoftInputFromWindow(portEditText.getWindowToken(), 0);
        inputMethodManager.hideSoftInputFromWindow(timeoutEditText.getWindowToken(), 0);
        ipEditText.clearFocus();
        portEditText.clearFocus();
        timeoutEditText.clearFocus();
    }

}