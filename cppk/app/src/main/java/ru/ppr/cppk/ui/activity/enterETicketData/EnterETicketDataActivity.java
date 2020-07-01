package ru.ppr.cppk.ui.activity.enterETicketData;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import ru.ppr.cppk.R;
import ru.ppr.cppk.model.ETicketDataParams;
import ru.ppr.cppk.ui.activity.base.SimpleMvpActivity;
import ru.ppr.logger.Logger;

/**
 * Окно ввода email и номера телефона
 *
 * @author Grigoriy Kashka
 */
public class EnterETicketDataActivity extends SimpleMvpActivity implements EnterETicketDataView {

    private static final String TAG = Logger.makeLogTag(EnterETicketDataActivity.class);

    // EXTRAS
    private static final String EXTRA_E_TICKET_PARAMS = "EXTRA_E_TICKET_PARAMS";
    public static final String EXTRA_E_TICKET_PARAMS_RESULT = "EXTRA_E_TICKET_PARAMS_RESULT";

    /*
     * UI
     */
    private Button okButton;
    private Button cancelButton;
    private ImageButton clearEmailPhoneBtn;
    private EditText emailPhoneEditText;
    private View errorLayout;
    //region Other
    private EnterETicketDataPresenter presenter;
    //endregion

    /**
     * статический конструктор
     *
     * @param context
     * @param eTicketDataParams
     * @return
     */
    public static Intent getCallingIntent(Context context, ETicketDataParams eTicketDataParams) {
        Intent intent = new Intent(context, EnterETicketDataActivity.class);
        intent.putExtra(EXTRA_E_TICKET_PARAMS, eTicketDataParams);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_eticket_data);

        findViews();

        presenter = getMvpDelegate().getPresenter(EnterETicketDataPresenter::new, EnterETicketDataPresenter.class);
        getMvpDelegate().bindView();
        presenter.bindInteractionListener(interactionListener);
        presenter.initialize(
                getIntent().getParcelableExtra(EXTRA_E_TICKET_PARAMS)
        );

        configViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        emailPhoneEditText.postDelayed(() -> setFocus(emailPhoneEditText), 200);
    }

    private void findViews() {
        okButton = (Button) findViewById(R.id.eTicketOk);
        cancelButton = (Button) findViewById(R.id.eTicketCancel);
        clearEmailPhoneBtn = (ImageButton) findViewById(R.id.eTicketEmailPhoneClearBtn);
        emailPhoneEditText = (EditText) findViewById(R.id.eTicketEmailPhoneEdit);
        errorLayout = findViewById(R.id.eTicketErrorLayout);
    }

    void configViews() {
        okButton.setOnClickListener(v -> presenter.onOkBtnClick(emailPhoneEditText.getText().toString()));
        cancelButton.setOnClickListener(v -> presenter.onCancelBtnClick());
        clearEmailPhoneBtn.setOnClickListener(v -> emailPhoneEditText.setText(""));
        emailPhoneEditText.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                presenter.onPhoneDoneClick(emailPhoneEditText.getText().toString());
            }
            return false;
        });
        errorLayout.setVisibility(View.GONE);

    }

    @Override
    public void setEmailAndPhone(ETicketDataParams eTicketDataParams) {
        emailPhoneEditText.setText(TextUtils.isEmpty(eTicketDataParams.getData()) ? "" : eTicketDataParams.getData());
    }

    @Override
    public void showError(boolean isDataOk) {
        errorLayout.setVisibility(isDataOk ? View.GONE : View.VISIBLE);
        if (!isDataOk)
            emailPhoneEditText.postDelayed(() -> setFocus(emailPhoneEditText), 200);
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

    private EnterETicketDataPresenter.InteractionListener interactionListener = eTicketDataParams -> {
        if (eTicketDataParams != null) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_E_TICKET_PARAMS_RESULT, eTicketDataParams);
            setResult(RESULT_OK, intent);
        }
        finish();
    };
}
