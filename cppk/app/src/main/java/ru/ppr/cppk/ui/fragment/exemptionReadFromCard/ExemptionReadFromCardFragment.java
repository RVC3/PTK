package ru.ppr.cppk.ui.fragment.exemptionReadFromCard;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.List;

import ru.ppr.core.ui.mvp.MvpDelegate;
import ru.ppr.cppk.R;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.cppk.logic.exemptionChecker.ExemptionCheckResultStringifyer;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionParams;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.fragment.base.LegacyMvpFragment;
import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class ExemptionReadFromCardFragment extends LegacyMvpFragment implements ExemptionReadFromCardView, FragmentOnBackPressed {

    private static final String TAG = Logger.makeLogTag(ExemptionReadFromCardFragment.class);
    public static final String FRAGMENT_TAG = ExemptionReadFromCardFragment.class.getSimpleName();

    private static final int CHILD_BSC_WAIT = 0;
    private static final int CHILD_BSC_READING = 1;
    private static final int CHILD_BSC_READ_SUCCESS = 2;
    private static final int CHILD_BSC_READ_FAILED = 3;

    public static ExemptionReadFromCardFragment newInstance() {
        return new ExemptionReadFromCardFragment();
    }

    /**
     * Di
     */
    private final ExemptionReadFromCardDi di = new ExemptionReadFromCardDi(di());

    private InteractionListener mInteractionListener;

    //Views
    private ViewFlipper viewFlipper;
    private TextView exemptionCode;
    private TextView exemptionName;
    private TextView exemptionPercentage;
    private TextView bscTypeTextView;
    private TextView bscNumberTextView;
    private TextView fioView;
    private Button useExemptionBtn;
    private SimpleLseView simpleLseView;
    private final SimpleLseView.State.Builder lseViewStateBuilder = new SimpleLseView.State.Builder();
    //region Other
    private ExemptionReadFromCardPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lseViewStateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        lseViewStateBuilder.setButton2(R.string.exemption_read_from_card_cancel_btn, v -> presenter.onCancelBtnClicked());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exemption_read_from_card, container, false);

        viewFlipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);
        exemptionCode = (TextView) view.findViewById(R.id.exemption_code);
        exemptionName = (TextView) view.findViewById(R.id.exemption_name);
        exemptionPercentage = (TextView) view.findViewById(R.id.exemption_percentage);
        bscTypeTextView = (TextView) view.findViewById(R.id.bsc_type);
        bscNumberTextView = (TextView) view.findViewById(R.id.bsc_number);
        fioView = (TextView) view.findViewById(R.id.fio);
        useExemptionBtn = (Button) view.findViewById(R.id.use_exemption);
        useExemptionBtn.setOnClickListener(v -> presenter.onUseExemptionBtnClicked());
        simpleLseView = (SimpleLseView) view.findViewById(R.id.simpleLseView);

        return view;
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    @Override
    public void init(MvpDelegate parent, String id) {
        super.init(parent, id);
        presenter = getMvpDelegate().getPresenter(ExemptionReadFromCardPresenter::new, ExemptionReadFromCardPresenter.class);
    }

    public void initialize(SelectExemptionParams selectExemptionParams) {
        presenter.bindInteractionListener(exemptionReadFromCardInteractionListener);
        presenter.initialize(
                di.uiThread(),
                di.nsiDaoSession(),
                di.smartCardStopListItemRepository(),
                di.exemptionChecker(),
                di.commonSettings(),
                di.findCardTaskFactory(),
                selectExemptionParams,
                di.nsiVersionManager(),
                di.ticketTypeRepository(),
                di.exemptionGroupRepository(),
                di.smartCardCancellationReasonRepository(),
                di.exemptionRepository(),
                di.fioFormatter()
        );
    }

    @Override
    public void showCardNotFoundError() {
        showError(getString(R.string.exemption_read_from_card_not_founded));
    }

    @Override
    public void showReadCardError() {
        showError(getString(R.string.exemption_read_from_card_read_error));
    }

    @Override
    public void showNoExemptionOnCardError() {
        showError(getString(R.string.exemption_read_from_card_no_exemption));
    }

    @Override
    public void showCardInStopListError(String reason) {
        showError(getString(R.string.exemption_read_from_card_in_stop_list, reason));
    }

    @Override
    public void setRetryBtnVisible(boolean visible) {
        if (visible) {
            lseViewStateBuilder.setButton1(R.string.exemption_read_from_card_retry_btn, v -> presenter.onRetryBtnClicked());
        } else {
            lseViewStateBuilder.setButton1(null, null);
        }
        simpleLseView.setState(lseViewStateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showCardValidityTimeError() {
        showError(getString(R.string.exemption_read_from_card_expired));
    }

    @Override
    public void setBscType(String bscType) {
        bscTypeTextView.setText(bscType == null ?
                getResources().getString(R.string.exemption_read_from_card_unknown_bsc_type)
                : getResources().getString(R.string.exemption_read_from_card_bsc_type, bscType));
    }

    @Override
    public void setBscNumber(String bscNumber) {
        bscNumberTextView.setText(bscNumber);
    }

    @Override
    public void showUnknownError() {
        showError(getString(R.string.exemption_read_from_card_read_error));
    }

    @Override
    public void showReadCardState() {
        viewFlipper.setDisplayedChild(CHILD_BSC_READING);
    }

    @Override
    public void showSearchCardState() {
        viewFlipper.setDisplayedChild(CHILD_BSC_WAIT);
    }

    @Override
    public void showReadCompletedState() {
        viewFlipper.setDisplayedChild(CHILD_BSC_READ_SUCCESS);
    }

    @Override
    public void setTimerValue(String value) {

    }

    @Override
    public void setFio(String fio) {
        this.fioView.setText(fio);
    }

    @Override
    public void setExemptionInfo(ExemptionInfo exemptionInfo) {
        exemptionCode.setText(exemptionInfo == null ? "" : String.valueOf(exemptionInfo.exemptionExpressCode));
        exemptionPercentage.setText(exemptionInfo == null ? "" : getResources().getString(R.string.exemption_read_from_card_reading_card_percentage, exemptionInfo.percentage));
        exemptionName.setText(exemptionInfo == null ? "" : exemptionInfo.groupName);
    }

    @Override
    public void showExemptionNotFoundMessage(int exemptionExpressCode) {
        showExemptionUsageDisabledMessage(getString(R.string.exemption_read_from_card_msg_not_found, exemptionExpressCode));
    }

    @Override
    public void showExemptionUsageDisabledMessage(ExemptionUsageDisabledMessage exemptionUsageDisabledMessage) {
        String msg = new ExemptionCheckResultStringifyer(getActivity()).getString(
                exemptionUsageDisabledMessage.checkResult,
                exemptionUsageDisabledMessage.exemptionExpressCode,
                exemptionUsageDisabledMessage.ticketTypeName
        );
        showExemptionUsageDisabledMessage(msg);
    }

    private void showExemptionUsageDisabledMessage(String msg) {
        Fragment existingFragment = getFragmentManager().findFragmentByTag(SimpleDialog.FRAGMENT_TAG);
        SimpleDialog exemptionUsageDialog;
        if (existingFragment == null) {
            exemptionUsageDialog = SimpleDialog.newInstance(null, msg, getString(R.string.exemption_read_from_card_close_dialog_btn), null, LinearLayout.VERTICAL, 0);
            exemptionUsageDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        }
    }

    private void showError(String msg) {
        lseViewStateBuilder.setTextMessage(msg);
        simpleLseView.setState(lseViewStateBuilder.build());
        simpleLseView.show();
        viewFlipper.setDisplayedChild(CHILD_BSC_READ_FAILED);
    }

    @Override
    public boolean onBackPress() {
        return presenter.onBackPressed();
    }

    private ExemptionReadFromCardPresenter.InteractionListener exemptionReadFromCardInteractionListener = new ExemptionReadFromCardPresenter.InteractionListener() {

        @Override
        public void onCancelSelectExemption() {
            mInteractionListener.onCancelSelectExemption();
        }

        @Override
        public void onExemptionSelected(@NonNull List<ExemptionForEvent> exemptionsForEvent, @Nullable AdditionalInfoForEtt additionalInfoForEtt) {
            mInteractionListener.onExemptionSelected(exemptionsForEvent, additionalInfoForEtt);
        }
    };

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void onCancelSelectExemption();

        void onExemptionSelected(@NonNull List<ExemptionForEvent> exemptionsForEvent, @Nullable AdditionalInfoForEtt additionalInfoForEtt);
    }
}
