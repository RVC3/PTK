package ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.Date;

import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.ui.widget.SingleClickButton;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo.dialog.TripOpeningClosureDialog;
import ru.ppr.cppk.ui.fragment.base.MvpFragment;
import ru.ppr.cppk.utils.customProgressBar.CustomProgress;
import ru.ppr.database.cache.Value;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TicketStorageType;

import static ru.ppr.cppk.R.*;


/**
 * Фрагмент с информацией о БСК.
 *Dismiss Btn Click
 * @author Aleksandr Brazhkin
 */
public class CardInfoFragment extends MvpFragment implements CardInfoView {

    public static CardInfoFragment newInstance() {
        return new CardInfoFragment();
    }

    public static final String TAG = CardInfoFragment.class.getSimpleName();

    // region Di
    private CardInfoComponent component;

    //Progress Bar
    private CustomProgress customProgress;
    //endregion
    //region Views
    private TextView cardType;
    private TextView cardNumberView;
    private TextView cardStatusValue;
    private TextView stopListReasonLabel;
    private TextView stopListReasonValue;
    private View stopListReasonSeparator;
    private TextView validToLabel;
    private TextView validToValue;
    private View validToSeparator;
    private TextView exemptionLabel;
    private TextView exemptionCodeValue;
    private TextView exemptionGroupName;
    private View exemptionSeparator;
    private TextView fioLabel;
    private TextView fioValue;
    private TextView birthValue;
    private View birthdaySeparator;
    private TextView birthLabel;
    private View fioSeparator;
    private TextView exemptionPercentageLabel;
    private TextView exemptionPercentageValue;
    private TextView lastPassageValue;
    private TextView passageStationValue;
    private TextView intersectValidationValue;
    private TextView tvWalletDataValue;
    private TextView passageOutStationValue;
    private ConstraintLayout clPassageMarkView;
    private TextView tvTroykaPoezki;
    private TextView validityDateTime;
    private TextView typeTicket;
    private SingleClickButton btnOpenTicket;
/*    private SingleClickButton btnOpenTicket;*/
    private TripOpeningClosureDialog tripOpeningClosureDialog;
    //endregion
    //region Other
    private CardInfoPresenter presenter;
    private boolean exemptionInfoVisible;
    private boolean exemptionPercentageVisible;
    private boolean exemptionGroupNameVisible;
    private boolean fioVisible = true;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerCardInfoComponent.builder().appComponent(Dagger.appComponent()).build();
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::cardInfoPresenter, CardInfoPresenter.class);
        presenter.setNavigator(navigator);
        presenter.initialize2();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(layout.fragment_service_ticket_control_card_info, container, false);
        cardType = (TextView) view.findViewById(id.cardType);
        cardNumberView = (TextView) view.findViewById(id.cardNumberView);
        cardStatusValue = (TextView) view.findViewById(id.cardStatusValue);
        stopListReasonLabel = (TextView) view.findViewById(id.stopListReasonLabel);
        stopListReasonValue = (TextView) view.findViewById(id.stopListReasonValue);
        stopListReasonSeparator = view.findViewById(id.stopListReasonSeparator);
        validToLabel = (TextView) view.findViewById(id.validToLabel);
        validToValue = (TextView) view.findViewById(id.validToValue);
        validToSeparator = view.findViewById(id.validToSeparator);
        exemptionLabel = (TextView) view.findViewById(id.exemptionLabel);
        exemptionCodeValue = (TextView) view.findViewById(id.exemptionCodeValue);
        exemptionGroupName = (TextView) view.findViewById(id.exemptionGroupName);
        exemptionSeparator = view.findViewById(id.exemptionSeparator);
        fioLabel = (TextView) view.findViewById(id.fioLabel);
        fioValue = (TextView) view.findViewById(id.fioValue);
        birthValue = (TextView) view.findViewById(id.birthdayValue);
        birthdaySeparator = view.findViewById(id.BirthdaySeparator);
        birthLabel = (TextView) view.findViewById(id.BirthdayLabel);
        fioSeparator = view.findViewById(id.fioSeparator);
        exemptionPercentageLabel = (TextView) view.findViewById(id.exemptionPercentageLabel);
        exemptionPercentageValue = (TextView) view.findViewById(id.exemptionPercentageValue);
        lastPassageValue = (TextView) view.findViewById(id.lastPassageValue);
        passageStationValue = (TextView) view.findViewById(id.passageStationValue);
        intersectValidationValue = (TextView) view.findViewById(id.isValidProhodValue);
        clPassageMarkView = (ConstraintLayout) view.findViewById(id.cl_passage_mark);
        tvWalletDataValue = (TextView) view.findViewById(id.ctv_wallet_data);
        tvTroykaPoezki = (TextView) view.findViewById(id.ctv_troyka_poezdki);
        typeTicket = (TextView) view.findViewById(id.tv_type_ticket);
        validityDateTime = (TextView) view.findViewById(id.tv_validity_date_time);
        passageOutStationValue = (TextView) view.findViewById(id.passageOutStationValue);
/*        btnOpenTicket = (SingleClickButton) view.findViewById(id.btn_open_ticket);
        btnOpenTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.info(TAG, "Show Trip Opening Closure Dialog");
                showTripOpeningClosureDialog();
            }
        });*/
        return view;
    }


    /**
     * Показать диалог Открытия / Закрытия поездки
     */
    public void showTripOpeningClosureDialog() {
        Logger.info(TAG, "show dialog TripOpeningClosureDialog");
        tripOpeningClosureDialog = TripOpeningClosureDialog.newInstance(presenter.isValidProzod());

        tripOpeningClosureDialog.setDialogPositiveBtnClickListener((dialog, validProzod, idStation) -> {
            Logger.info(TAG, "=================================");
            Logger.info(TAG, "Dialog Validity Positive Btn Click");
            customProgress = CustomProgress.getInstance();
            customProgress.showProgress(getActivity(), "Загрузка...", false);
            if(validProzod) {
                Logger.info(TAG, "Prohod: " + validProzod);
                Logger.info(TAG, "Id station: " + idStation);
                writeWalletCardResult(1);
            }else {
                Logger.info(TAG, "Prohod: " + validProzod);
                Logger.info(TAG, "Id station: " + idStation);
                writeWalletCardResult(1);
            }
        });

        tripOpeningClosureDialog.setOnCancelListener(dialogInterface -> {
            Logger.info(TAG, "Cancel Dialog Validity");
        });

        tripOpeningClosureDialog.setOnDismissListener(dialogInterface -> {
            Logger.info(TAG, "Dismiss Dialog Validity");
        });

        tripOpeningClosureDialog.show(getFragmentManager(), tripOpeningClosureDialog.FRAGMENT_TAG);
    }


    private void writeWalletCardResult(int price) {
        Logger.info(TAG, "=================================");
        Logger.info(TAG, "Start Write Card");
        Logger.info(TAG, "=================================");
        presenter.startWriteCard();


       // WriteCardResult writePassageMarkResult = ((WritePassageMarkReader) cardReader).writePassageMark(passageMarkForWrite);
    }


    public void onPassageMarkChanged(PassageMark passageMark) {
        presenter.onPassageMarkChanged(passageMark);
    }

    @Override
    public void setTicketStorageType(TicketStorageType ticketStorageType) {
        cardType.setText(getTicketStorageType(ticketStorageType));
    }

    private int getTicketStorageType(TicketStorageType ticketStorageType){
        switch(ticketStorageType){
            case CPPK:
            case Service:
            case CPPKCounter:
                return string.card_info_card_type_bsc;
            case SKM:
                return string.card_info_card_type_skm;
            case SKMO:
                return string.card_info_card_type_skmo;
            case IPK:
                return string.card_info_card_type_ipk;
            case TRK:
                return string.card_info_card_type_troyka;
            case ETT:
                return string.card_info_card_type_ett;
            case STR:
                return string.card_info_card_type_strelka;
            default:
                return string.card_info_card_type_unknown;
        }
    }

    @Override
    public void setCardStatus(CardStatus cardStatus) {
        switch (cardStatus) {
            case IN_STOP_LIST: {
                cardStatusValue.setBackgroundResource(color.service_ticket_control_card_info_error);
                cardStatusValue.setText(string.card_info_card_status_in_stop_list);
                return;
            }
            case HAS_EXPIRED: {
                cardStatusValue.setBackgroundResource(color.service_ticket_control_card_info_error);
                cardStatusValue.setText(string.card_info_card_status_has_expired);
                return;
            }
            case VALID:
            default: {
                cardStatusValue.setBackgroundResource(color.service_ticket_control_card_info_success);
                cardStatusValue.setText(string.card_info_card_status_valid);
            }
        }
    }


    @Override
    public void setInStopListReasonVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        stopListReasonLabel.setVisibility(visibility);
        stopListReasonValue.setVisibility(visibility);
        stopListReasonSeparator.setVisibility(visibility);
    }

    @Override
    public void setInStopListReason(String inStopListReason) {
        stopListReasonValue.setText((inStopListReason == null)? getString(string.card_info_card_stop_list_unknown_reason): inStopListReason);
    }


    @Override
    public void setProhodValid(int color, int text) {
        Log.d(TAG, "colorRes:"+ color+ " textRes:"+ text );
        intersectValidationValue.setTextColor(getResources().getColor(color));
        intersectValidationValue.setText(getString(text));
    }

    @Override
    public void setWalletUnitsLeft(String walletUnits) {
        tvWalletDataValue.setText(walletUnits);
    }

    @Override
    public void setTroykaPoezdkiLeft(String poiezdki) {
        tvTroykaPoezki.setText(poiezdki);
    }

    @Override
    public void showOutComeStation(String outComeStation) {
        passageOutStationValue.setText((outComeStation == null) ? getString(string.card_info_passage_station_unknown) : outComeStation);
    }


    @Override
    public void showValidityDateTime(String dateTimeStr) {
        validityDateTime.setText(dateTimeStr);
    }

    @Override
    public void showNameTypeTicket(String nameTypeTicket) {
        typeTicket.setText(nameTypeTicket);
    }


    @Override
    public void setResOpeningClosureTrip(int resOpeningTrip) {
            String isOpeningTrip = getString(resOpeningTrip);
            if (isOpeningTrip != null)
                btnOpenTicket.setText(isOpeningTrip);
/*            String isOpeningTrip = getString(resOpeningTrip);
            if (isOpeningTrip != null)
                btnOpenTicket.setText(isOpeningTrip);*/
    }

    @Override
    public void setVisibleOpeningClosureTrip(boolean visibleOpenness) {
        btnOpenTicket.setVisibility(visibleOpenness ? View.VISIBLE : View.GONE);
       // btnOpenTicket.setVisibility(visibleOpenness ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setTimerValue(int value) {
        Logger.info(TAG, "Timer: " + value);
        if(customProgress != null){
            customProgress.setTimerSecond(value);
        }
    }

    @Override
    public void setState(State state) {
        String state_str = (state == null) ?  "" : state.name();
        Logger.info(TAG, state_str);
    }

    @Override
    public void setValidToVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        validToLabel.setVisibility(visibility);
        validToValue.setVisibility(visibility);
        validToSeparator.setVisibility(visibility);
    }

    @Override
    public void setValidToDate(Date date) {
        if (date == null) {
            validToValue.setText("");
        } else {
            validToValue.setText((date==null) ? "": DateFormatOperations.getOutDate(date));
        }
    }

    public String getBirthday(String code) {
        if (CheckLuhn(code)) {
            try {
                Logger.debug(TAG, "sk code: " + code.substring(0, 4));
                Logger.debug(TAG, "тип карты " + code.substring(4, 6));
                Logger.debug(TAG, "region " + code.substring(6, 8));
                int day = Integer.parseInt(code.substring(8, 10));
                Logger.debug(TAG, "mf " + (day > 50 ? "m" : "f"));
                if (day > 50)
                    day -= 50;

                int month = Integer.parseInt(code.substring(10, 12));
                if (month > 20)
                    month -= 20;
                if (month > 20)
                    month -= 20;

                return day + "." + month + "." + code.substring(12, 14);
            }
            catch (Exception e){
                Logger.debug(TAG, e.getLocalizedMessage());
                return "<Ошибка>";
            }
        } else
            return "<Ошибка>";
    }


    private boolean CheckLuhn(String ccNumber) {
        if (!ccNumber.matches("[0-9]+"))
            return false;
        if (ccNumber.length() < 15)
            return false;

        int sum = 0;
        boolean alternate = false;
        for (int i = ccNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(ccNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }


    @Override
    public void setExemptionInfoVisible(boolean visible) {
        exemptionInfoVisible = visible;
        int visibility = exemptionInfoVisible ? View.VISIBLE : View.GONE;
        exemptionLabel.setVisibility(visibility);
        exemptionCodeValue.setVisibility(visibility);
        exemptionSeparator.setVisibility(visibility);
        birthLabel.setVisibility(visibility);
        birthdaySeparator.setVisibility(visibility);
        birthValue.setVisibility(visibility);
        setExemptionPercentageVisible(exemptionPercentageVisible);
        setExemptionGroupNameVisible(exemptionGroupNameVisible);
        setFioVisible(fioVisible);
    }

    @Override
    public void setInfoVisiblePassageMark(boolean visible) {
        clPassageMarkView.setVisibility(visible ? View.VISIBLE: View.INVISIBLE);
    }

    @Override
    public void setExemptionCode(int exemptionCode) {
        exemptionCodeValue.setText(String.valueOf(exemptionCode));
    }

    @Override
    public void setExemptionGroupName(String groupName) {
        exemptionGroupName.setText(groupName);
        setExemptionGroupNameVisible(groupName != null);
    }

    @Override
    public void setExemptionPercentageValue(int percentageValue) {
        exemptionPercentageValue.setText(getString(string.card_info_exemption_value, percentageValue));
    }

    @Override
    public void setExemptionPercentageVisible(boolean visible) {
        exemptionPercentageVisible = visible;
        int visibility = exemptionInfoVisible && exemptionPercentageVisible ? View.VISIBLE : View.GONE;
        exemptionPercentageLabel.setVisibility(visibility);
        exemptionPercentageValue.setVisibility(visibility);
    }

    @Override
    public void setFio(String firstName, String secondName, String lastName) {
        fioValue.setText(getFioForDisplay(firstName, secondName, lastName));
    }

    @Override
    public void setCardNumber(String cardNumber) {
        cardNumberView.setText(cardNumber);
        if (cardNumber.startsWith("9643"))
            birthValue.setText(getBirthday(cardNumber));
    }

    @Override
    public void setLastPassageTime(String lastPassageTime) {
        if (lastPassageTime != null) {
            lastPassageValue.setText((lastPassageTime.isEmpty())? getResources().getString(R.string.card_info_passage_time_unknown) : lastPassageTime);
        }
    }

    @Override
    public void setPassageStation(String station, PassageStationType type) {
        passageStationValue.setText(getPassageStation(station, type));
    }

    private String getPassageStation(String station, PassageStationType type){
        return (type == PassageStationType.PTK) ? getResources().getString(string.card_info_passage_station_ptk) : (station == null) ? getResources().getString(string.card_info_passage_station_unknown) : station;
    }

    private void setExemptionGroupNameVisible(boolean visible) {
        exemptionGroupNameVisible = visible;
        exemptionGroupName.setVisibility(exemptionInfoVisible && visible ? View.VISIBLE : View.GONE);
    }

    private void setFioVisible(boolean visible) {
        fioVisible = visible;
        int visibility = exemptionInfoVisible && fioVisible ? View.VISIBLE : View.GONE;
        fioLabel.setVisibility(visibility);
        fioValue.setVisibility(visibility);
        fioSeparator.setVisibility(visibility);
    }

    /**
     * Возвращает ФИО в виде "Иванов И И".
     */
    private String getFioForDisplay(String firstName, String secondName, String lastName) {
        StringBuilder sb = new StringBuilder();

        if (lastName != null && lastName.trim().length() > 0)
            sb.append(lastName.trim());

        sb.append(" ");

        if (firstName != null && firstName.trim().length() > 0)
            sb.append(firstName.trim().substring(0, 1));

        sb.append(" ");

        if (secondName != null && secondName.trim().length() > 0)
            sb.append(secondName.trim().substring(0, 1));

        return sb.toString();
    }

    private final CardInfoPresenter.Navigator navigator = new CardInfoPresenter.Navigator() {

        @Override
        public void navigateToServiceTicketControlActivity() {
            Logger.info(TAG, "Update Data Card Info Fragment");
            tripOpeningClosureDialog.dismiss();
            if(customProgress != null)
                customProgress.hideProgress();
        }
    };

}
