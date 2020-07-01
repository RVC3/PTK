package ru.ppr.cppk.ui.activity.serviceticketcontrol;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.base.MvpActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.controlreadbsc.model.ControlReadBscParams;
import ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo.CardInfoFragment;
import ru.ppr.cppk.ui.activity.serviceticketcontrol.ticketinfo.TicketInfoFragment;
import ru.ppr.logger.Logger;


/**
 * Экран контроля карты со служебной информацией.
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceTicketControlActivity extends MvpActivity implements ServiceTicketControlView {

    private static final String TAG = Logger.makeLogTag(ServiceTicketControlActivity.class);

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, ServiceTicketControlActivity.class);
    }

    //region Теги фрагментов
    private static final String F_TAG_TICKET_INFO = "F_TAG_TICKET_INFO";
    private static final String F_TAG_CARD_INFO = "F_TAG_CARD_INFO";
    //endregion

    // region Di
    private ServiceTicketControlComponent component;
    // endregion
    // region Views
    private RadioGroup tabs;
    private RadioButton ticketInfo;
    private RadioButton cardInfo;

    //endregion
    //region Other
    private ServiceTicketControlPresenter presenter;
    private TicketInfoFragment ticketInfoFragment;
    private CardInfoFragment cardInfoFragment;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerServiceTicketControlComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::serviceTicketControlPresenter, ServiceTicketControlPresenter.class);
        ///////////////////////////////////////////////////////////////////////////////////////
        canUserHardwareButton();
        ///////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_service_ticket_control);
        tabs = (RadioGroup) findViewById(R.id.tabs);
        tabs.setOnCheckedChangeListener((group, checkedId) -> {
            Logger.trace(TAG, "onCheckedChanged, ticketInfoShown = " + (checkedId == R.id.ticketInfo));
            setSelectedTab(checkedId);
        });
        ticketInfo = (RadioButton) findViewById(R.id.ticketInfo);
        cardInfo = (RadioButton) findViewById(R.id.cardInfo);
        ///////////////////////////////////////////////////////////////////////////////////////
        setupTicketInfoFragment();
        setupCardInfoFragment();
        setSelectedTab(R.id.ticketInfo);
        ///////////////////////////////////////////////////////////////////////////////////////
        presenter.setNavigator(navigator);
        presenter.initialize2();
    }

    private void setupTicketInfoFragment() {
        ticketInfoFragment = (TicketInfoFragment) getFragmentManager().findFragmentByTag(F_TAG_TICKET_INFO);
        if (ticketInfoFragment == null) {
            ticketInfoFragment = TicketInfoFragment.newInstance();
            getFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, ticketInfoFragment, F_TAG_TICKET_INFO)
                    .commit();
        }
        ticketInfoFragment.setInteractionListener(ticketInfoInteractionListener);
    }

    private void setupCardInfoFragment() {
        cardInfoFragment = (CardInfoFragment) getFragmentManager().findFragmentByTag(F_TAG_CARD_INFO);
        if (cardInfoFragment == null) {
            cardInfoFragment = CardInfoFragment.newInstance();
            getFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, cardInfoFragment, F_TAG_CARD_INFO)
                    .commit();
        }
    }


    private void setSelectedTab(int tabId) {
        tabs.check(tabId);
        if (tabId == R.id.ticketInfo) {
            getFragmentManager().beginTransaction()
                    .show(ticketInfoFragment)
                    .hide(cardInfoFragment)
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .show(cardInfoFragment)
                    .hide(ticketInfoFragment)
                    .commit();
        }
    }

    @Override
    public void onClickRfrid() {
        presenter.onRfidBtnClicked();
    }

    @Override
    public void onClickBarcode() {
        presenter.onBarcodeBtnClicked();
    }

    @Override
    public void onBackPressed() {
        Logger.trace(TAG, "onBackPressed");
        if (cardInfo.isChecked()) {
            setSelectedTab(R.id.ticketInfo);
            return;
        }
        super.onBackPressed();
    }

    private final ServiceTicketControlPresenter.Navigator navigator = new ServiceTicketControlPresenter.Navigator() {

        private static final long NAVIGATION_DELAY = 500;

        private long lastNavigationToAnotherActivityTime = 0;

        @Override
        public void navigateToCardInfo() {
            setSelectedTab(R.id.cardInfo);
        }

        @Override
        public void navigateToControlReadBsc() {
            if (!isNavigationAllowed()) {
                return;
            }
            ControlReadBscParams controlReadBscParams = new ControlReadBscParams();
            controlReadBscParams.setReadForTransferParams(null);
            controlReadBscParams.setIncrementPmHwUsageCounter(true);
            Navigator.navigateToControlReadBscActivity(ServiceTicketControlActivity.this, controlReadBscParams);
            finish();
        }

        @Override
        public void navigateToControlReadBarcode() {
            if (!isNavigationAllowed()) {
                return;
            }
            Navigator.navigateToControlReadBarcodeActivity(ServiceTicketControlActivity.this, null);
            finish();
        }

        private boolean isNavigationAllowed() {
            final long now = SystemClock.elapsedRealtime();
            if (now - lastNavigationToAnotherActivityTime > NAVIGATION_DELAY) {
                lastNavigationToAnotherActivityTime = now;
                return true;
            } else {
                return false;
            }
        }
    };

    private TicketInfoFragment.InteractionListener ticketInfoInteractionListener = new TicketInfoFragment.InteractionListener() {

        @Override
        public void navigateToSaleNewPd(PdSaleParams pdSaleParams) {
            Navigator.navigateToPdSaleActivity(ServiceTicketControlActivity.this, pdSaleParams);
            finish();
        }

        @Override
        public void navigateToPreviousScreen() {
            finish();
        }
    };
}
