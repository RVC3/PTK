package ru.ppr.cppk.ui.activity.closeTerminalDay;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.ui.activity.base.MvpActivity;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class CloseTerminalDayActivity extends MvpActivity implements CloseTerminalDayView {

    private static final String TAG = Logger.makeLogTag(CloseTerminalDayActivity.class);

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, CloseTerminalDayActivity.class);
    }

    // region Di
    private CloseTerminalDayDi di;
    // endregion
    // region Views
    private FeedbackProgressDialog mProgressDialog;
    private SimpleLseView simpleLseView;
    //endregion
    //region Other
    private CloseTerminalDayPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        di = new CloseTerminalDayDi(di());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_close_terminal_day);
        simpleLseView = (SimpleLseView) findViewById(R.id.simpleLseView);

        mProgressDialog = new FeedbackProgressDialog(this);
        mProgressDialog.setCancelable(false);

        presenter = getMvpDelegate().getPresenter(di::closeTerminalDayPresenter, CloseTerminalDayPresenter.class);
        presenter.bindInteractionListener(closeTerminalDayInteractionListener);
        presenter.initialize(
                di.localDaoSession(),
                di.uiThread(),
                di.posManager(),
                di.printerManager(),
                Dagger.appComponent().ticketTapeChecker()
        );
    }

    @Override
    public void onBackPressed() {
        // nop
    }

    @Override
    public void hideAnyError() {
        simpleLseView.hide();
    }

    @Override
    public void showInitDayEndAskDialog() {
        Fragment existingFragment = getFragmentManager().findFragmentByTag(SimpleDialog.FRAGMENT_TAG);
        SimpleDialog askDialog;
        if (existingFragment == null) {
            askDialog = SimpleDialog.newInstance(
                    null,
                    getString(R.string.terminal_day_close_ask_init),
                    getString(R.string.terminal_day_close_yes),
                    getString(R.string.terminal_day_close_no),
                    LinearLayout.HORIZONTAL,
                    -1);
            askDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        } else {
            askDialog = (SimpleDialog) existingFragment;
        }
        askDialog.setCancelable(false);
        askDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> presenter.onInitDayEndApproved());
        askDialog.setDialogNegativeBtnClickListener((dialog, dialogId) -> presenter.onInitDayEndCancelled());
    }

    @Override
    public void hideInitDayEndAskDialog() {
        Fragment existingFragment = getFragmentManager().findFragmentByTag(SimpleDialog.FRAGMENT_TAG);
        if (existingFragment != null) {
            SimpleDialog askDialog = (SimpleDialog) existingFragment;
            askDialog.dismiss();
        }
    }

    @Override
    public void showPreparingDataProgress() {
        mProgressDialog.setMessage(getString(R.string.terminal_day_close_closing_at_terminal));
        mProgressDialog.show();
    }

    @Override
    public void showPreparingDataError() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);

        stateBuilder.setTextMessage(R.string.terminal_day_close_failed_msg);
        stateBuilder.setButton1(R.string.sev_btn_repeat, v -> presenter.onPreparingDataRepeatBtnClick());
        stateBuilder.setButton2(R.string.sev_btn_cancel, v -> presenter.onPreparingDataCancelBtnClick());

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showClosingDayProgress() {
        mProgressDialog.setMessage(getString(R.string.terminal_day_close_closing_at_terminal));
        mProgressDialog.show();
    }

    @Override
    public void showClosingDayError(String bankResponse) {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);

        stateBuilder.setTextMessage(getResources().getString(R.string.terminal_day_close_failed_msg) + (bankResponse == null ? "" : " (" + bankResponse + ")"));
        stateBuilder.setButton1(R.string.sev_btn_repeat, v -> presenter.onClosingDayRepeatBtnClick());
        stateBuilder.setButton2(R.string.sev_btn_cancel, v -> presenter.onClosingDayCancelBtnClick());

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showCloseDaySuccessDialog(long dayNumber) {
        Fragment existingFragment = getFragmentManager().findFragmentByTag(SimpleDialog.FRAGMENT_TAG);
        SimpleDialog successDialog;
        if (existingFragment == null) {
            successDialog = SimpleDialog.newInstance(
                    null,
                    getString(R.string.terminal_day_close_success_msg, dayNumber),
                    getString(R.string.terminal_day_close_ok),
                    null,
                    LinearLayout.VERTICAL,
                    -1);
            successDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        } else {
            successDialog = (SimpleDialog) existingFragment;
        }
        successDialog.setCancelable(false);
        successDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> presenter.onOperationSuccessApproved());
    }

    @Override
    public void hideAnyProgress() {
        mProgressDialog.dismiss();
    }

    @Override
    public void showPrintingSlipProgress() {
        mProgressDialog.setMessage(getString(R.string.terminal_day_close_printing_slip));
        mProgressDialog.show();
    }

    @Override
    public void showPrintingSlipError() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);

        stateBuilder.setTextMessage(R.string.terminal_day_close_printing_slip_failed_msg);
        stateBuilder.setButton1(R.string.sev_btn_repeat, v -> presenter.onPrintingSlipRepeatBtnClick());
        stateBuilder.setButton2(R.string.sev_btn_cancel, v -> presenter.onPrintingSlipCancelBtnClick());

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    private CloseTerminalDayPresenter.InteractionListener closeTerminalDayInteractionListener = () -> finish();
}
