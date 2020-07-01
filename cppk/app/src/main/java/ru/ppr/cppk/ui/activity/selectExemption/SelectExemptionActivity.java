package ru.ppr.cppk.ui.activity.selectExemption;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.ppr.cppk.R;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.cppk.ui.activity.base.SimpleMvpActivity;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.fragment.exemptionEnterSurname.ExemptionEnterSurnameFragment;
import ru.ppr.cppk.ui.fragment.exemptionManualInput.ExemptionManualInputFragment;
import ru.ppr.cppk.ui.fragment.exemptionReadFromCard.ExemptionReadFromCardFragment;
import ru.ppr.logger.Logger;

/**
 * Экран выбора льготы
 *
 * @author Aleksandr Brazhkin
 */
public class SelectExemptionActivity extends SimpleMvpActivity implements SelectExemptionView {

    private static final String TAG = Logger.makeLogTag(SelectExemptionActivity.class);

    // EXTRAS
    private static final String EXTRA_SELECT_EXEMPTION_PARAMS = "EXTRA_SELECT_EXEMPTION_PARAMS";
    private static final String EXTRA_SELECT_EXEMPTION_RESULT = "EXTRA_SELECT_EXEMPTION_RESULT";
    // MVP CHILDREN
    private static final String EXEMPTION_MANUAL_INPUT_FRAGMENT = "EXEMPTION_MANUAL_INPUT_FRAGMENT";
    private static final String EXEMPTION_READ_FROM_CARD_FRAGMENT = "EXEMPTION_READ_FROM_CARD_FRAGMENT";
    private static final String EXEMPTION_ENTER_SURNAME_FRAGMENT = "EXEMPTION_ENTER_SURNAME_FRAGMENT";

    public static Intent getCallingIntent(Context context, SelectExemptionParams selectExemptionParams) {
        Intent intent = new Intent(context, SelectExemptionActivity.class);
        intent.putExtra(EXTRA_SELECT_EXEMPTION_PARAMS, selectExemptionParams);
        return intent;
    }

    @Nullable
    public static SelectExemptionResult getResultFromIntent(int resultCode, @Nullable final Intent intent) {
        SelectExemptionResult selectExemptionResult = null;
        if (resultCode == Activity.RESULT_OK && intent != null) {
            selectExemptionResult = intent.getParcelableExtra(SelectExemptionActivity.EXTRA_SELECT_EXEMPTION_RESULT);
        }
        return selectExemptionResult;
    }

    /**
     * Di
     */
    private SelectExemptionDi di;
    /**
     * Экран ручного ввода льготы
     */
    private ExemptionManualInputFragment exemptionManualInputFragment;
    /**
     * Экран считывания льготы с карты
     */
    private ExemptionReadFromCardFragment exemptionReadFromCardFragment;
    //region Other
    private SelectExemptionPresenter presenter;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_exemption);
        di = new SelectExemptionDi(di());

        ExemptionManualInputFragment fragment = (ExemptionManualInputFragment) getFragmentManager().findFragmentByTag(ExemptionManualInputFragment.FRAGMENT_TAG);
        if (fragment != null) {
            exemptionManualInputFragment = fragment;
            exemptionManualInputFragment.setInteractionListener(exemptionManualInputInteractionListener);
            exemptionManualInputFragment.init(getMvpDelegate(), EXEMPTION_MANUAL_INPUT_FRAGMENT);
        }

        ExemptionReadFromCardFragment fragmentBsk = (ExemptionReadFromCardFragment) getFragmentManager().findFragmentByTag(ExemptionReadFromCardFragment.FRAGMENT_TAG);
        if (fragmentBsk != null) {
            exemptionReadFromCardFragment = fragmentBsk;
            exemptionReadFromCardFragment.setInteractionListener(exemptionReadFromCardInteractionListener);
            exemptionReadFromCardFragment.init(getMvpDelegate(), EXEMPTION_READ_FROM_CARD_FRAGMENT);
        }

        presenter = getMvpDelegate().getPresenter(SelectExemptionPresenter::new, SelectExemptionPresenter.class);
        getMvpDelegate().bindView();
        presenter.bindInteractionListener(selectExemptionInteractionListener);
        presenter.initialize(
                getIntent().getParcelableExtra(EXTRA_SELECT_EXEMPTION_PARAMS),
                di.ticketStorageTypeToTicketTypeChecker()
        );
    }

    @Override
    public void onBackPressed() {
        Logger.trace(TAG, "onBackPressed");
        FragmentManager manager = getFragmentManager();
        Fragment currentFragment = manager.findFragmentById(R.id.fragmentContainer);

        if (currentFragment instanceof FragmentOnBackPressed) {
            FragmentOnBackPressed fragmentOnBackPressed = (FragmentOnBackPressed) currentFragment;
            if (fragmentOnBackPressed.onBackPress()) {
                Logger.info(TAG, "onBackPressed, currentFragment return true");
                return;
            }
        }

        super.onBackPressed();
    }

    private ExemptionManualInputFragment.InteractionListener exemptionManualInputInteractionListener = new ExemptionManualInputFragment.InteractionListener() {
        @Override
        public void navigateToReadFromCard() {
            presenter.onNavigateToReadFromCard();
        }

        @Override
        public void navigateToEnterSurname(List<ExemptionForEvent> exemptionsForEvent) {
            presenter.onNavigateToEnterSurname(exemptionsForEvent);
        }
    };

    private ExemptionEnterSurnameFragment.InteractionListener exemptionEnterSurnameInteractionListener = new ExemptionEnterSurnameFragment.InteractionListener() {
        @Override
        public void onExemptionSelected(@NonNull List<ExemptionForEvent> exemptionsForEvent) {
            presenter.onExemptionSelected(exemptionsForEvent, null);
        }

        @Override
        public void onCancelSelectExemption() {
            presenter.onCancelSelectExemption();
        }
    };

    private ExemptionReadFromCardFragment.InteractionListener exemptionReadFromCardInteractionListener = new ExemptionReadFromCardFragment.InteractionListener() {
        @Override
        public void onCancelSelectExemption() {
            presenter.onCancelSelectExemption();
        }

        @Override
        public void onExemptionSelected(@NonNull List<ExemptionForEvent> exemptionsForEvent, @Nullable AdditionalInfoForEtt additionalInfoForEtt) {
            presenter.onExemptionSelected(exemptionsForEvent, additionalInfoForEtt);
        }
    };

    private SelectExemptionPresenter.InteractionListener selectExemptionInteractionListener = new SelectExemptionPresenter.InteractionListener() {
        @Override
        public void transferDataToChildManualInput(SelectExemptionParams selectExemptionParams) {
            ExemptionManualInputFragment fragment = ExemptionManualInputFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, fragment, ExemptionManualInputFragment.FRAGMENT_TAG)
                    .commit();
            exemptionManualInputFragment = fragment;
            exemptionManualInputFragment.setInteractionListener(exemptionManualInputInteractionListener);
            exemptionManualInputFragment.init(getMvpDelegate(), EXEMPTION_MANUAL_INPUT_FRAGMENT);
            exemptionManualInputFragment.initialize(selectExemptionParams);
        }

        @Override
        public void transferDataToChildReadFromBsk(SelectExemptionParams selectExemptionParams) {
            ExemptionReadFromCardFragment fragment = ExemptionReadFromCardFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, fragment, ExemptionReadFromCardFragment.FRAGMENT_TAG)
                    .commit();
            exemptionReadFromCardFragment = fragment;
            exemptionReadFromCardFragment.setInteractionListener(exemptionReadFromCardInteractionListener);
            exemptionReadFromCardFragment.init(getMvpDelegate(), EXEMPTION_READ_FROM_CARD_FRAGMENT);
            exemptionReadFromCardFragment.initialize(selectExemptionParams);
        }

        @Override
        public void navigateToReadFromCard(SelectExemptionParams selectExemptionParams) {
            ExemptionReadFromCardFragment fragment = ExemptionReadFromCardFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment, ExemptionReadFromCardFragment.FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
            exemptionReadFromCardFragment = fragment;
            exemptionReadFromCardFragment.setInteractionListener(exemptionReadFromCardInteractionListener);
            exemptionReadFromCardFragment.init(getMvpDelegate(), EXEMPTION_READ_FROM_CARD_FRAGMENT);
            exemptionReadFromCardFragment.initialize(selectExemptionParams);
        }

        @Override
        public void navigateToEnterSurname(List<ExemptionForEvent> exemptionForEvents) {
            ExemptionEnterSurnameFragment fragment = ExemptionEnterSurnameFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment, ExemptionEnterSurnameFragment.FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
            fragment.setInteractionListener(exemptionEnterSurnameInteractionListener);
            fragment.init(getMvpDelegate(), EXEMPTION_ENTER_SURNAME_FRAGMENT);
            fragment.initialize(exemptionForEvents);
        }

        @Override
        public void navigateToPreviousScreen(SelectExemptionResult selectExemptionResult) {
            if (selectExemptionResult != null) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_SELECT_EXEMPTION_RESULT, selectExemptionResult);
                setResult(RESULT_OK, intent);
            }
            finish();
        }
    };
}
