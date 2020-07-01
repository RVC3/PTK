package ru.ppr.cppk.ui.activity.senddocstoofd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.base.MvpActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;

/**
 * Окно отправки данных в ОФД
 *
 * @author Grigoriy Kashka
 */
public class SendDocsToOfdActivity extends MvpActivity implements SendDocsToOfdView {

    private static final String EXTRA_BACK_TO_WELCOME_ACTIVITY = "EXTRA_BACK_TO_WELCOME_ACTIVITY";

    // region Di
    private SendDocsToOfdComponent component;
    // endregion
    // region Views
    private Button sendButton;
    private Button updateButton;
    private TextView unsentDocsCountValueTextView;
    private TextView firstUnsentDocNumberValueTextView;
    private TextView firstUnsentDocDateTimeValueTextView;
    private View errorLayout;
    private TextView errorTextView;
    // endregion
    //region Other
    private SendDocsToOfdPresenter presenter;
    private FeedbackProgressDialog progressDialog;
    //endregion

    public static Intent getCallingIntent(Context context, boolean backToWelcomeActivity) {
        return new Intent(context, SendDocsToOfdActivity.class)
                .putExtra(EXTRA_BACK_TO_WELCOME_ACTIVITY, backToWelcomeActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerSendDocsToOfdComponent.builder().appComponent(Dagger.appComponent()).activityModule(new ActivityModule(this)).build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::sendDocsToOfdPresenter, SendDocsToOfdPresenter.class);
        ///////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_send_docs_to_ofd);

        progressDialog = new FeedbackProgressDialog(this);
        progressDialog.setMessage(getString(R.string.send_docs_to_ofd_progress));
        progressDialog.setCancelable(false);

        updateButton = (Button) findViewById(R.id.sendDocsToOfdUpdateBtn);
        sendButton = (Button) findViewById(R.id.sendDocsToOfdSendBtn);
        unsentDocsCountValueTextView = (TextView) findViewById(R.id.sendDocsToOfdUnsentDocsCountValue);
        firstUnsentDocNumberValueTextView = (TextView) findViewById(R.id.sendDocsToOfdFirstUnsentDocNumberValue);
        firstUnsentDocDateTimeValueTextView = (TextView) findViewById(R.id.sendDocsToOfdFirstUnsentDocDateTimeValue);
        errorLayout = findViewById(R.id.sendDocsToOfdErrorLayout);
        errorTextView = (TextView) findViewById(R.id.sendDocsToOfdErrorTextView);

        updateButton.setOnClickListener(v -> presenter.onUpdateBtnClick());
        sendButton.setOnClickListener(v -> presenter.onSendBtnClick());
        errorLayout.setVisibility(View.GONE);
        ///////////////////////////////////////////////////////////////////////////////////////
        presenter.setBackToWelcomeActivity(getIntent().getBooleanExtra(EXTRA_BACK_TO_WELCOME_ACTIVITY, false));
        presenter.setNavigator(sendDocsToOfdPresenterNavigator);
        presenter.initialize();
    }

    @Override
    public void setUnsentDocsCount(int unsentDocsCount) {
        unsentDocsCountValueTextView.setText(String.valueOf(unsentDocsCount));
    }

    @Override
    public void setFirstUnsentDocNumber(int firstUnsentDocNumber) {
        firstUnsentDocNumberValueTextView.setText(String.valueOf(firstUnsentDocNumber));
    }

    @Override
    public void setFirstUnsentDocDateTime(Date firstUnsentDocDateTime) {
        String out = getResources().getString(R.string.send_docs_to_ofd_no_data);
        if (firstUnsentDocDateTime != null) {
            SimpleDateFormat local = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            out = local.format(firstUnsentDocDateTime);
        }
        firstUnsentDocDateTimeValueTextView.setText(out);
    }

    @Override
    public void showError(Error error) {
        switch (error) {
            case GET_DATA:
                errorTextView.setText(R.string.send_docs_to_ofd_error);
                break;
            case EXIST_UNSENT_DOCS:
                errorTextView.setText(R.string.send_docs_to_ofd_exist_unsent_docs_error);
                break;
            case NOT_ALL_DOCS_SENT:
                errorTextView.setText(R.string.send_docs_to_ofd_not_all_docs_sent_error);
                break;
        }
        errorLayout.setVisibility(error == Error.NONE ? View.GONE : View.VISIBLE);
    }

    @Override
    public void showProgress() {
        progressDialog.show();
    }

    @Override
    public void hideProgress() {
        progressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        presenter.onBackPressed();
    }

    private final SendDocsToOfdPresenter.Navigator sendDocsToOfdPresenterNavigator = new SendDocsToOfdPresenter.Navigator() {

        @Override
        public void navigateBack() {
            finish();
        }

        @Override
        public void navigateToWelcomeActivity() {
            Navigator.navigateToWelcomeActivity(SendDocsToOfdActivity.this, false);
        }

    };

}