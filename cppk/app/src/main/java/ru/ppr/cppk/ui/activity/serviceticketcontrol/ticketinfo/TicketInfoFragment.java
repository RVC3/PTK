package ru.ppr.cppk.ui.activity.serviceticketcontrol.ticketinfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.localdb.model.ServiceZoneType;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.base.MvpFragment;


/**
 * Фрагмент с информацией о служебных данных.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketInfoFragment extends MvpFragment implements TicketInfoView {

    public static TicketInfoFragment newInstance() {
        return new TicketInfoFragment();
    }

    // region Di
    private TicketInfoComponent component;
    //endregion
    //region Views
    private View contentView;
    private SimpleLseView simpleLseView;
    private View notValidLabel;
    private TextView errorDescription;
    private TextView serviceCardLabel;
    private TextView travelAllowedLabel;
    private View turnstileOnlyLabel;
    private TextView validityTimeLabel;
    private TextView validityFromTime;
    private TextView validityToTime;
    private TextView coverageAreaLabel;
    private View allArea;
    TextView[] areas = new TextView[6];
    private TextView directionLabel;
    private TextView prodSectionLabel;
    private TextView stationLabel;
    private TextView checkDocumentsLabel;
    private TextView notValidBecauseLabel;
    private Button outOfAreaBtn;
    private Button noDocumentsBtn;
    private Button saleNewPdBtn;
    //endregion
    //region Other
    private TicketInfoPresenter presenter;
    private InteractionListener interactionListener;
    private DataErrorDesc dataErrorDesc;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerTicketInfoComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .build();
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::ticketInfoPresenter, TicketInfoPresenter.class);
        presenter.setNavigator(navigator);
        presenter.initialize2();
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_ticket_control_ticket_info, container, false);
        contentView = view.findViewById(R.id.contentView);
        simpleLseView = (SimpleLseView) view.findViewById(R.id.simpleLseView);
        notValidLabel = view.findViewById(R.id.notValidLabel);
        errorDescription = (TextView) view.findViewById(R.id.errorDescription);
        serviceCardLabel = (TextView) view.findViewById(R.id.serviceCardLabel);
        travelAllowedLabel = (TextView) view.findViewById(R.id.travelAllowedLabel);
        turnstileOnlyLabel = view.findViewById(R.id.turnstileOnlyLabel);
        validityTimeLabel = (TextView) view.findViewById(R.id.validityTimeLabel);
        validityFromTime = (TextView) view.findViewById(R.id.validityFromTime);
        validityToTime = (TextView) view.findViewById(R.id.validityToTime);
        coverageAreaLabel = (TextView) view.findViewById(R.id.coverageAreaLabel);
        allArea = view.findViewById(R.id.allArea);
        areas[0] = (TextView) view.findViewById(R.id.area1);
        areas[1] = (TextView) view.findViewById(R.id.area2);
        areas[2] = (TextView) view.findViewById(R.id.area3);
        areas[3] = (TextView) view.findViewById(R.id.area4);
        areas[4] = (TextView) view.findViewById(R.id.area5);
        areas[5] = (TextView) view.findViewById(R.id.area6);
        directionLabel = (TextView) view.findViewById(R.id.directionLabel);
        prodSectionLabel = (TextView) view.findViewById(R.id.prodSectionLabel);
        stationLabel = (TextView) view.findViewById(R.id.stationLabel);
        checkDocumentsLabel = (TextView) view.findViewById(R.id.checkDocumentsLabel);
        notValidBecauseLabel = (TextView) view.findViewById(R.id.notValidBecauseLabel);
        outOfAreaBtn = (Button) view.findViewById(R.id.outOfAreaBtn);
        outOfAreaBtn.setOnClickListener(v -> presenter.onOutOfAreaBtnClicked());
        noDocumentsBtn = (Button) view.findViewById(R.id.noDocumentsBtn);
        noDocumentsBtn.setOnClickListener(v -> presenter.onNoDocumentsBtnClicked());
        saleNewPdBtn = (Button) view.findViewById(R.id.saleNewPdBtn);
        saleNewPdBtn.setOnClickListener(v -> presenter.onSaleNewPdBtnClicked());

        return view;
    }

    @Override
    public void setState(State state) {
        switch (state) {
            case DATA: {
                contentView.setVisibility(View.VISIBLE);
                simpleLseView.hide();
                break;
            }
            case ERROR: {
                contentView.setVisibility(View.GONE);
                setDataErrorDesc(dataErrorDesc);
                break;
            }
            case VALIDATING: {
                contentView.setVisibility(View.GONE);
                SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
                stateBuilder.setMode(SimpleLseView.State.MODE_LOADING);
                stateBuilder.setTextMessage(R.string.ticket_info_checking_progress);
                simpleLseView.setState(stateBuilder.build());
                simpleLseView.show();
                break;
            }
        }
    }

    @Override
    public void setServiceZones(List<ServiceZoneInfo> serviceZones) {
        List<ServiceZoneInfo> sortedServiceZones = new ArrayList<>(serviceZones);
        Collections.sort(sortedServiceZones, serviceZoneComparator);
        if (sortedServiceZones.isEmpty()) {
            allArea.setVisibility(View.GONE);
            directionLabel.setVisibility(View.GONE);
            prodSectionLabel.setVisibility(View.GONE);
            stationLabel.setVisibility(View.GONE);
            for (TextView area : areas) {
                area.setVisibility(View.GONE);
            }
        } else if (sortedServiceZones.get(0).getType() == ServiceZoneType.Polygone) {
            allArea.setVisibility(View.VISIBLE);
            directionLabel.setVisibility(View.GONE);
            prodSectionLabel.setVisibility(View.GONE);
            stationLabel.setVisibility(View.GONE);
            for (TextView area : areas) {
                area.setVisibility(View.GONE);
            }
        } else {
            allArea.setVisibility(View.GONE);

            int directionLabelPos = -1;
            int prodSectionLabelPos = -1;
            int stationLabelPos = -1;

            for (int i = 0; i < Math.min(areas.length, sortedServiceZones.size()); i++) {
                ServiceZoneInfo serviceZoneInfo = sortedServiceZones.get(i);
                ServiceZoneType serviceZoneType = serviceZoneInfo.getType();
                if (serviceZoneType == ServiceZoneType.Direction && directionLabelPos == -1) {
                    directionLabelPos = i;
                } else if (serviceZoneType == ServiceZoneType.ProductionSection && prodSectionLabelPos == -1) {
                    prodSectionLabelPos = i;
                } else if (serviceZoneType == ServiceZoneType.Station && stationLabelPos == -1) {
                    stationLabelPos = i;
                }
                if (serviceZoneInfo.getName() == null) {
                    areas[i].setText(getString(R.string.ticket_info_unknown_zone, serviceZoneInfo.getCode()));
                } else {
                    areas[i].setText(sortedServiceZones.get(i).getName());
                }
                areas[i].setVisibility(View.VISIBLE);
            }

            if (directionLabelPos == -1) {
                directionLabel.setVisibility(View.GONE);
            } else {
                directionLabel.setVisibility(View.VISIBLE);
                ConstraintLayout.LayoutParams directionLabelLp = (ConstraintLayout.LayoutParams) directionLabel.getLayoutParams();
                directionLabelLp.baselineToBaseline = areas[directionLabelPos].getId();
                directionLabel.setLayoutParams(directionLabelLp);
            }

            if (prodSectionLabelPos == -1) {
                prodSectionLabel.setVisibility(View.GONE);
            } else {
                prodSectionLabel.setVisibility(View.VISIBLE);
                ConstraintLayout.LayoutParams prodSectionLabelLp = (ConstraintLayout.LayoutParams) prodSectionLabel.getLayoutParams();
                prodSectionLabelLp.baselineToBaseline = areas[prodSectionLabelPos].getId();
                prodSectionLabel.setLayoutParams(prodSectionLabelLp);
            }

            if (stationLabelPos == -1) {
                stationLabel.setVisibility(View.GONE);
            } else {
                stationLabel.setVisibility(View.VISIBLE);
                ConstraintLayout.LayoutParams stationLabelLp = (ConstraintLayout.LayoutParams) stationLabel.getLayoutParams();
                stationLabelLp.baselineToBaseline = areas[stationLabelPos].getId();
                stationLabel.setLayoutParams(stationLabelLp);
            }
        }
    }

    @Override
    public void setValidityTime(Date startDate, Date endDate) {
        if (startDate == null) {
            validityFromTime.setText("");
        } else {
            validityFromTime.setText(DateFormatOperations.getDateddMMyyyy(startDate));
        }
        if (endDate == null) {
            validityToTime.setText("");
        } else {
            validityToTime.setText(DateFormatOperations.getDateddMMyyyy(endDate));
        }
    }

    @Override
    public void setValidFromError(boolean error) {
        if (error) {
            validityFromTime.setTextColor(getResources().getColor(R.color.service_ticket_control_ticket_info_error));
        } else {
            validityFromTime.setTextColor(getResources().getColorStateList(R.color.service_ticket_control_ticket_info_normal));
        }
    }

    @Override
    public void setValidToError(boolean error) {
        if (error) {
            validityToTime.setTextColor(getResources().getColor(R.color.service_ticket_control_ticket_info_error));
        } else {
            validityToTime.setTextColor(getResources().getColorStateList(R.color.service_ticket_control_ticket_info_normal));
        }
    }

    @Override
    public void setCheckDocumentsLabelVisible(boolean visible) {
        checkDocumentsLabel.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setValid(boolean valid) {
        notValidLabel.setVisibility(valid ? View.GONE : View.VISIBLE);
        serviceCardLabel.setEnabled(valid);
        travelAllowedLabel.setEnabled(valid);
        validityTimeLabel.setEnabled(valid);
        validityFromTime.setEnabled(valid);
        validityToTime.setEnabled(valid);
        coverageAreaLabel.setEnabled(valid);
        allArea.setEnabled(valid);
        directionLabel.setEnabled(valid);
        prodSectionLabel.setEnabled(valid);
        stationLabel.setEnabled(valid);
        for (TextView area : areas) {
            area.setEnabled(valid);
        }
    }

    @Override
    public void setTravelAllowed(boolean allowed) {
        if (allowed) {
            travelAllowedLabel.setText(R.string.ticket_info_travel_allowed);
            travelAllowedLabel.setTextColor(getResources().getColorStateList(R.color.service_ticket_control_ticket_info_normal));
            turnstileOnlyLabel.setVisibility(View.GONE);
        } else {
            travelAllowedLabel.setText(R.string.ticket_info_travel_not_allowed);
            travelAllowedLabel.setTextColor(getResources().getColor(R.color.service_ticket_control_ticket_info_error));
            turnstileOnlyLabel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setOutOfAreaBtnVisible(boolean visible) {
        outOfAreaBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (outOfAreaBtn.getVisibility() == View.GONE) {
            ConstraintLayout.LayoutParams notValidBecauseLabelLp = (ConstraintLayout.LayoutParams) notValidBecauseLabel.getLayoutParams();
            notValidBecauseLabelLp.bottomToTop = noDocumentsBtn.getId();
            notValidBecauseLabel.setLayoutParams(notValidBecauseLabelLp);
        }
        updateNotValidBecauseLabelVisibility();
    }

    @Override
    public void setNoDocumentsBtnVisible(boolean visible) {
        noDocumentsBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (noDocumentsBtn.getVisibility() == View.GONE) {
            ConstraintLayout.LayoutParams notValidBecauseLabelLp = (ConstraintLayout.LayoutParams) notValidBecauseLabel.getLayoutParams();
            notValidBecauseLabelLp.bottomToTop = outOfAreaBtn.getId();
            notValidBecauseLabel.setLayoutParams(notValidBecauseLabelLp);
        }
        updateNotValidBecauseLabelVisibility();
    }

    @Override
    public void setSaleNewPdBtnVisible(boolean visible) {
        saleNewPdBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setValidityErrorDesc(ValidityErrorDesc validityErrorDesc) {
        if (validityErrorDesc == null) {
            this.errorDescription.setVisibility(View.GONE);
        } else {
            this.errorDescription.setVisibility(View.VISIBLE);
            switch (validityErrorDesc) {
                case INVALID_EDS_KEY: {
                    this.errorDescription.setText(R.string.ticket_info_error_invalid_eds_key);
                    break;
                }
                case REVOKED_EDS_KEY: {
                    this.errorDescription.setText(R.string.ticket_info_error_revoked_eds_key);
                    break;
                }
            }
        }
    }

    @Override
    public void setDataErrorDesc(DataErrorDesc dataErrorDesc) {
        this.dataErrorDesc = dataErrorDesc;
        if (dataErrorDesc == null){
            return;
        }
        String message = "";
        switch (dataErrorDesc) {
            case NO_DATA: {
                message = getString(R.string.ticket_info_error_no_data);
                break;
            }
            case INVALID_DEVICE_ID: {
                message = getString(R.string.ticket_info_error_invalid_device_id);
                break;
            }
        }
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(message);
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showSaleNewPdConfirmDialog() {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                getString(R.string.ticket_info_sale_new_pd_dialog_msg),
                getString(R.string.ticket_info_sale_new_pd_dialog_yes),
                getString(R.string.ticket_info_sale_new_pd_dialog_no),
                LinearLayout.HORIZONTAL,
                0);
        simpleDialog.setCancelable(false);
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> presenter.onSaleNewPdDialogYesBtnClicked());
        simpleDialog.setDialogNegativeBtnClickListener((dialog, dialogId) -> presenter.onSaleNewPdDialogNoBtnClicked());
    }

    private void updateNotValidBecauseLabelVisibility() {
        boolean outOfAreaBtnVisible = outOfAreaBtn.getVisibility() == View.VISIBLE;
        boolean noDocumentsBtnVisible = noDocumentsBtn.getVisibility() == View.VISIBLE;
        notValidBecauseLabel.setVisibility(outOfAreaBtnVisible || noDocumentsBtnVisible ? View.VISIBLE : View.GONE);
    }

    Comparator<ServiceZoneInfo> serviceZoneComparator = new Comparator<ServiceZoneInfo>() {

        private final HashMap<ServiceZoneType, Integer> serviceZoneTypeOrderIndexes = new HashMap<>();

        {
            serviceZoneTypeOrderIndexes.put(ServiceZoneType.Polygone, 0);
            serviceZoneTypeOrderIndexes.put(ServiceZoneType.Direction, 1);
            serviceZoneTypeOrderIndexes.put(ServiceZoneType.ProductionSection, 2);
            serviceZoneTypeOrderIndexes.put(ServiceZoneType.Station, 3);
            serviceZoneTypeOrderIndexes.put(ServiceZoneType.None, 4);
        }

        @Override
        public int compare(ServiceZoneInfo info1, ServiceZoneInfo info2) {
            int index1 = serviceZoneTypeOrderIndexes.get(info1.getType());
            int index2 = serviceZoneTypeOrderIndexes.get(info2.getType());
            if (index1 < index2) {
                return -1;
            } else if (index2 < index1) {
                return 1;
            } else {
                if (info1.getName() == null && info2.getName() == null) {
                    return 0;
                } else if (info1.getName() == null) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
    };

    private final TicketInfoPresenter.Navigator navigator = new TicketInfoPresenter.Navigator() {
        @Override
        public void navigateToSaleNewPd(PdSaleParams pdSaleParams) {
            interactionListener.navigateToSaleNewPd(pdSaleParams);
        }

        @Override
        public void navigateToPreviousScreen() {
            interactionListener.navigateToPreviousScreen();
        }
    };

    public interface InteractionListener {
        void navigateToSaleNewPd(PdSaleParams pdSaleParams);

        void navigateToPreviousScreen();
    }
}
