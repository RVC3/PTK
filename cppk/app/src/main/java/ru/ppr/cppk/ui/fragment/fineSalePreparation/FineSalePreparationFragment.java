package ru.ppr.cppk.ui.fragment.fineSalePreparation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.List;

import ru.ppr.core.ui.mvp.MvpDelegate;
import ru.ppr.cppk.R;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.logic.CriticalNsiVersionDialogDelegate;
import ru.ppr.cppk.model.ETicketDataParams;
import ru.ppr.cppk.model.FineSaleData;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.enterETicketData.EnterETicketDataActivity;
import ru.ppr.cppk.ui.adapter.spinner.FineAdapter;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.base.LegacyMvpFragment;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Fine;

/**
 * Экран оформления доплаты
 *
 * @author Aleksandr Brazhkin
 */
public class FineSalePreparationFragment extends LegacyMvpFragment implements FineSalePreparationView {

    private static final String TAG = Logger.makeLogTag(FineSalePreparationFragment.class);
    public static final String FRAGMENT_TAG = FineSalePreparationFragment.class.getSimpleName();
    // Dialog tags
    private static final String NO_FINES_AVAILABLE_DIALOG_TAG = "NO_FINES_AVAILABLE_DIALOG_TAG";
    private static final String REALLY_WANT_FINE_DIALOG_TAG = "REALLY_WANT_FINE_DIALOG_TAG";
    private static final String DIALOG_CRITICAL_NSI_CLOSE = "DIALOG_CRITICAL_NSI_CLOSE";
    // RC
    private static final int RC_E_TICKET = 101;

    public static FineSalePreparationFragment newInstance() {
        return new FineSalePreparationFragment();
    }

    /**
     * Di
     */
    private final FineSalePreparationDi di = new FineSalePreparationDi(di());

    private InteractionListener mInteractionListener;

    //Views
    private Spinner fineSpinner;
    private RadioGroup paymentTypeGroup;
    private RadioButton paymentTypeCash;
    private RadioButton paymentTypeCard;
    private Button sendETicketBtn;
    private View separatorAfterETicketBtn;
    private TextView costValue;
    private TextView regionValue;
    private ViewGroup costGroup;

    // Other
    private FineAdapter mFineAdapter;
    private int selectedFinePosition = 0;
    //region Other
    private FineSalePreparationPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFineAdapter = new FineAdapter(getActivity(), R.layout.item_spinner_fine_sale_fine);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fine_sale_preparation, container, false);

        fineSpinner = (Spinner) view.findViewById(R.id.fineSpinner);
        fineSpinner.setAdapter(mFineAdapter);
        fineSpinner.setOnTouchListener(fineSpinnerOnTouchListener);
        fineSpinner.setOnItemSelectedListener(fineSpinnerOnItemSelectedListener);
        //////////////////////////////////////////////////////////////////////////////////////
        paymentTypeGroup = (RadioGroup) view.findViewById(R.id.paymentTypeGroup);
        paymentTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.paymentTypeCard) {
                presenter.onPaymentTypeChecked(PaymentType.INDIVIDUAL_BANK_CARD);
            } else {
                presenter.onPaymentTypeChecked(PaymentType.INDIVIDUAL_CASH);
            }
        });
        paymentTypeCash = (RadioButton) view.findViewById(R.id.paymentTypeCash);
        paymentTypeCard = (RadioButton) view.findViewById(R.id.paymentTypeCard);
        //////////////////////////////////////////////////////////////////////////////////////
        sendETicketBtn = (Button) view.findViewById(R.id.sendETicketBtn);
        sendETicketBtn.setOnClickListener(v -> presenter.onSendETicketBtnClick());
        separatorAfterETicketBtn = view.findViewById(R.id.separatorAfterETicketBtn);
        //////////////////////////////////////////////////////////////////////////////////////
        costValue = (TextView) view.findViewById(R.id.costValue);
        regionValue = (TextView) view.findViewById(R.id.regionValue);
        costGroup = (ViewGroup) view.findViewById(R.id.costGroup);
        //////////////////////////////////////////////////////////////////////////////////////
        view.findViewById(R.id.formPdBtn).setOnClickListener(v -> presenter.onSellBtnClick());

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_E_TICKET:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    presenter.onETicketDataSelected(data.getParcelableExtra(EnterETicketDataActivity.EXTRA_E_TICKET_PARAMS_RESULT));
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);

        }
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    @Override
    public void init(MvpDelegate parent, String id) {
        super.init(parent, id);
        presenter = getMvpDelegate().getPresenter(FineSalePreparationPresenter::new, FineSalePreparationPresenter.class);
    }

    public void initialize(FineSaleData fineSaleData) {
        presenter.bindInteractionListener(fineSalePreparationInteractionListener);
        presenter.initialize(fineSaleData, di.uiThread(), di.nsiDaoSession(), di.printerManager(), di.privateSettings(), di.nsiVersionManager(),
                di.criticalNsiVersionChecker(), di.fineRepository());
    }

    @Override
    public void setFines(List<Fine> fines) {
        Logger.trace(TAG, "setFines, size = " + fines.size());
        mFineAdapter.setItems(fines);
    }

    @Override
    public void setRegion(String regionName) {
        regionValue.setText(regionName);
    }

    @Override
    public void setCost(BigDecimal cost) {
        costValue.setText(cost == null ? "" : getString(R.string.fine_sale_preparation_cost_value, cost));
    }

    @Override
    public void setSendETicketBtnVisible(boolean visible) {
        sendETicketBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
        separatorAfterETicketBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setPaymentTypeVisible(boolean visible) {
        paymentTypeGroup.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setCostGroupVisible(boolean visible) {
        costGroup.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setReallyWantFineDialogVisible(boolean visible, String fineName, BigDecimal fineCost) {
        SimpleDialog simpleDialog = (SimpleDialog) getFragmentManager().findFragmentByTag(REALLY_WANT_FINE_DIALOG_TAG);

        if (visible) {
            if (simpleDialog == null) {
                String message = getString(R.string.fine_sale_preparation_really_want_fine_dialog_message);

                if (fineName != null && fineCost != null) {
                    message = getString(R.string.fine_sale_preparation_really_want_fine_dialog_message_with_name_and_cost, fineName, fineCost);
                }

                simpleDialog = SimpleDialog.newInstance(null,
                        message,
                        getString(R.string.fine_sale_preparation_really_want_fine_dialog_positive),
                        getString(R.string.fine_sale_preparation_really_want_fine_dialog_negative),
                        LinearLayout.HORIZONTAL,
                        0);
                simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> presenter.onRallyWantFineDialogPositiveClick());
                simpleDialog.setOnDismissListener(dialog -> presenter.onRallyWantFineDialogDismiss());
                simpleDialog.show(getFragmentManager(), REALLY_WANT_FINE_DIALOG_TAG);
            } else {
                simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> presenter.onRallyWantFineDialogPositiveClick());
                simpleDialog.setOnDismissListener(dialog -> presenter.onRallyWantFineDialogDismiss());
            }
        } else {
            if (simpleDialog != null) {
                simpleDialog.dismiss();
            }
        }
    }

    @Override
    public void setCriticalNsiBackDialogVisible(boolean visible) {
        SimpleDialog criticalNsiVersionDialog = (SimpleDialog) getFragmentManager().findFragmentByTag(DIALOG_CRITICAL_NSI_CLOSE);

        if (visible) {
            // http://agile.srvdev.ru/browse/CPPKPP-35280
            // (Сделать проверки при разблокировке ПТК и при открытии экрана продажи.)
            // ShiftAlarmManager тут не работает
            CriticalNsiVersionDialogDelegate criticalNsiVersionDialogDelegate = new CriticalNsiVersionDialogDelegate(
                    di.criticalNsiVersionChecker(),
                    getFragmentManager(),
                    getResources(),
                    DIALOG_CRITICAL_NSI_CLOSE);

            if (criticalNsiVersionDialog == null) {
                criticalNsiVersionDialogDelegate.showCriticalNsiCloseDialogIfNeeded(onCriticalNsiBackDialogShownListener);
            } else {
                criticalNsiVersionDialog.setDialogPositiveBtnClickListener(onCriticalNsiBackDialogShownListener);
            }
        } else {
            if (criticalNsiVersionDialog != null) {
                criticalNsiVersionDialog.dismiss();
            }
        }
    }

    @Override
    public void setCriticalNsiCloseShiftDialogVisible(boolean visible) {
        SimpleDialog criticalNsiVersionDialog = (SimpleDialog) getFragmentManager().findFragmentByTag(DIALOG_CRITICAL_NSI_CLOSE);

        if (visible) {
            // http://agile.srvdev.ru/browse/CPPKPP-35280
            // (Сделать проверки при разблокировке ПТК и при открытии экрана продажи.)
            // ShiftAlarmManager тут не работает
            CriticalNsiVersionDialogDelegate criticalNsiVersionDialogDelegate = new CriticalNsiVersionDialogDelegate(
                    di.criticalNsiVersionChecker(),
                    getFragmentManager(),
                    getResources(),
                    DIALOG_CRITICAL_NSI_CLOSE);

            if (criticalNsiVersionDialog == null) {
                criticalNsiVersionDialogDelegate.showCriticalNsiCloseDialogIfNeeded(onCriticalNsiCloseShiftDialogShownListener);
            } else {
                criticalNsiVersionDialog.setDialogPositiveBtnClickListener(onCriticalNsiCloseShiftDialogShownListener);
            }
        } else {
            if (criticalNsiVersionDialog != null) {
                criticalNsiVersionDialog.dismiss();
            }
        }
    }

    @Override
    public void setNoFinesAvailableDialogVisible(boolean visible) {
        SimpleDialog simpleDialog = (SimpleDialog) getFragmentManager().findFragmentByTag(NO_FINES_AVAILABLE_DIALOG_TAG);

        if (visible) {
            if (simpleDialog == null) {
                simpleDialog = SimpleDialog.newInstance(null,
                        getString(R.string.fine_sale_preparation_no_fines_available_dialog_message),
                        getString(R.string.fine_sale_preparation_no_fines_available_dialog_close),
                        null,
                        LinearLayout.VERTICAL,
                        0);
                simpleDialog.setOnDismissListener(dialog -> presenter.onNoFinesAvailableDialogDismiss());
                simpleDialog.show(getFragmentManager(), NO_FINES_AVAILABLE_DIALOG_TAG);
            } else {
                simpleDialog.setOnDismissListener(dialog -> presenter.onNoFinesAvailableDialogDismiss());
            }
        } else {
            if (simpleDialog != null) {
                simpleDialog.dismiss();
            }
        }
    }

    /**
     * Обработчик выбора тарифного плана.
     */
    private final AdapterView.OnItemSelectedListener fineSpinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Logger.trace(TAG, "onItemSelected, uiPosition = " + position);
            if (selectedFinePosition == position) {
                Logger.trace(TAG, "onItemSelected skipped");
            } else {
                // В нулевой позиции мнимый элемент
                // https://aj.srvdev.ru/browse/CPPKPP-28012
                mFineAdapter.setSelectedPosition(position);
                selectedFinePosition = position;
                presenter.onFineSelected(position - 1);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            /* NOP */
        }
    };

    /**
     * Блокировщик открытия выпадающего списка тарифных планов, если в нем нет иных вариантов.
     */
    View.OnTouchListener fineSpinnerOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            for (int i = 0; i < mFineAdapter.getItems().size(); i++) {
                if (mFineAdapter.getSelectedPosition() - 1 != i) {
                    return false;
                }
            }
            return true;
        }
    };

    private final SimpleDialog.DialogBtnClickListener onCriticalNsiBackDialogShownListener = (dialog, dialogId) -> {
        presenter.onCriticalNsiBackDialogRead();
    };

    private final SimpleDialog.DialogBtnClickListener onCriticalNsiCloseShiftDialogShownListener = (dialog, dialogId) -> {
        presenter.onCriticalNsiCloseShiftDialogRead();
    };

    private FineSalePreparationPresenter.InteractionListener fineSalePreparationInteractionListener = new FineSalePreparationPresenter.InteractionListener() {

        @Override
        public void onSendETicketBtnClick(ETicketDataParams eTicketDataParams) {
            Navigator.navigateToEnterETicketDataActivity(RC_E_TICKET, getActivity(), FineSalePreparationFragment.this, eTicketDataParams);
        }

        @Override
        public void onSellBtnClick() {
            mInteractionListener.onSellBtnClick();
        }

        @Override
        public void closeScreen() {
            mInteractionListener.closeScreen();
        }

        @Override
        public void navigateToCloseShiftActivity() {
            mInteractionListener.navigateToCloseShiftActivity();
        }
    };

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {

        void onSellBtnClick();

        void closeScreen();

        void navigateToCloseShiftActivity();
    }
}
