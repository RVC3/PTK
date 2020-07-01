package ru.ppr.cppk.ui.activity.pdrepeal;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.repeal.DeletePdActivity;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.base.SimpleMvpActivity;
import ru.ppr.cppk.ui.activity.pdrepeal.poscancel.PosCancelFragment;
import ru.ppr.cppk.ui.activity.pdrepeal.poscancelprintslip.PosCancelPrintSlipFragment;
import ru.ppr.cppk.ui.activity.pdrepeal.printrepealcheck.PrintRepealCheckFragment;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.logger.Logger;

/**
 * Экран выбора льготы
 *
 * @author Aleksandr Brazhkin
 */
public class PdRepealActivity extends SimpleMvpActivity implements PdRepealView {

    private static final String TAG = Logger.makeLogTag(PdRepealActivity.class);

    // EXTRAS
    private static final String EXTRA_PD_REPEAL_PARAMS = "EXTRA_PD_REPEAL_PARAMS";
    // RC
    private static final int RC_DELETE_FROM_CARD = 37;
    // F_TAGS
    private static final String F_TAG_PRINT_SLIP = "F_TAG_PRINT_SLIP";
    private static final String F_TAG_PRINT_CHECK = "F_TAG_PRINT_CHECK";
    private static final String F_TAG_POS_CANCEL = "F_TAG_POS_CANCEL";

    public static Intent getCallingIntent(Context context, PdRepealParams pdRepealParams) {
        Intent intent = new Intent(context, PdRepealActivity.class);
        intent.putExtra(EXTRA_PD_REPEAL_PARAMS, pdRepealParams);
        return intent;
    }

    // region Di
    private PdRepealComponent component;
    // endregion
    //region Views
    private SimpleLseView simpleLseView;
    //endregion
    //region Other
    private PdRepealPresenter presenter;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerPdRepealComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .pdRepealParams(getIntent().getParcelableExtra(EXTRA_PD_REPEAL_PARAMS))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pd_repeal);

        simpleLseView = (SimpleLseView) findViewById(R.id.simpleLseView);

        presenter = getMvpDelegate().getPresenter(component::pdRepealPresenter, PdRepealPresenter.class);
        getMvpDelegate().bindView();
        presenter.bindNavigator(fineSaleNavigator);
        presenter.initialize();
    }

    @Override
    public void showAbortedState(String message) {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(getResources().getString(R.string.pd_repeal_aborted) + (message == null ? "" : " (" + message + ")"));
        stateBuilder.setButton1(R.string.pd_repeal_aborted_ok_btn, v -> presenter.onAbortedOkBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void hideAbortedState() {
        simpleLseView.hide();
    }

    @Override
    public void setInitializingStateVisible(boolean visible) {
        if (visible) {
            SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
            stateBuilder.setMode(SimpleLseView.State.MODE_LOADING);
            stateBuilder.setTextMessage(R.string.pd_repeal_initializing);
            simpleLseView.setState(stateBuilder.build());
            simpleLseView.show();
        } else {
            simpleLseView.hide();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_DELETE_FROM_CARD: {
                boolean deleted = DeletePdActivity.getResultFromIntent(resultCode, data);
                presenter.onDeletingFromCardFinished(deleted);
            }
            default: {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment instanceof FragmentOnBackPressed) {
            FragmentOnBackPressed fragmentOnBackPressed = (FragmentOnBackPressed) currentFragment;
            if (fragmentOnBackPressed.onBackPress()) {
                return;
            }
        }
        super.onBackPressed();
    }

    private PdRepealPresenter.Navigator fineSaleNavigator = new PdRepealPresenter.Navigator() {

        @Override
        public void navigateToPrintRepealCheck() {
            Logger.trace(TAG, "navigateToPrintRepealCheck");
            PrintRepealCheckFragment fragment = PrintRepealCheckFragment.newInstance();
            Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragmentContainer);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragmentContainer, fragment, F_TAG_PRINT_CHECK);
            if (currentFragment != null) {
                fragmentTransaction.remove(currentFragment);
            }
            fragmentTransaction.commit();
        }

        @Override
        public void navigateToPrintSlip() {
            Logger.trace(TAG, "navigateToPrintSlip");
            PosCancelPrintSlipFragment fragment = PosCancelPrintSlipFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .remove(getFragmentManager().findFragmentById(R.id.fragmentContainer))
                    .add(R.id.fragmentContainer, fragment, F_TAG_PRINT_SLIP)
                    .commit();
        }

        @Override
        public void navigateToCancelCardPayment(long bankTransactionEventId) {
            Logger.trace(TAG, "navigateToCancelCardPayment");
            PosCancelFragment fragment = PosCancelFragment.newInstance(bankTransactionEventId);
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, fragment, F_TAG_POS_CANCEL)
                    .commit();
        }

        @Override
        public void navigateToDeletePdFromCard(String cardUid, int pdPosition) {
            Logger.trace(TAG, "navigateToDeletePdFromCard");
            Navigator.navigateToDeletePdActivityForResult(PdRepealActivity.this, RC_DELETE_FROM_CARD, cardUid, pdPosition);
        }

        @Override
        public void closeScreen() {
            finish();
        }
    };

    public PdRepealComponent getComponent() {
        return component;
    }
}
