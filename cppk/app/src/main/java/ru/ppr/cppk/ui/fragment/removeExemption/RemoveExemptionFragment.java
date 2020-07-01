package ru.ppr.cppk.ui.fragment.removeExemption;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import ru.ppr.core.ui.mvp.MvpDelegate;
import ru.ppr.cppk.R;
import ru.ppr.cppk.model.RemoveExemptionParams;
import ru.ppr.cppk.ui.fragment.base.LegacyMvpFragment;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class RemoveExemptionFragment extends LegacyMvpFragment implements RemoveExemptionView {

    private static final String TAG = Logger.makeLogTag(RemoveExemptionFragment.class);
    public static final String FRAGMENT_TAG = RemoveExemptionFragment.class.getSimpleName();

    public static RemoveExemptionFragment newInstance() {
        return new RemoveExemptionFragment();
    }

    /**
     * Di
     */
    private final RemoveExemptionDi di = new RemoveExemptionDi(di());

    private InteractionListener mInteractionListener;

    //Views
    private TextView expressCode;
    private TextView groupName;
    private TextView percentage;
    private ViewGroup fioLayout;
    private TextView fio;
    private ViewGroup documentNumberLayout;
    private TextView fieldDocumentNumber;
    private TextView documentNumber;
    private ViewGroup documentIssueDateLayout;
    private TextView documentIssueDate;
    private ViewGroup bscNumberLayout;
    private TextView bscNumber;
    private TextView bscType;
    //region Other
    private RemoveExemptionPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_remove_exemption, container, false);

        expressCode = (TextView) view.findViewById(R.id.expressCode);
        groupName = (TextView) view.findViewById(R.id.groupName);
        percentage = (TextView) view.findViewById(R.id.percentage);
        fioLayout = (ViewGroup) view.findViewById(R.id.fioLayout);
        fio = (TextView) view.findViewById(R.id.fio);
        documentNumberLayout = (ViewGroup) view.findViewById(R.id.documentNumberLayout);
        documentNumber = (TextView) view.findViewById(R.id.documentNumber);
        fieldDocumentNumber = (TextView) view.findViewById(R.id.fieldDocumentNumber);
        documentIssueDateLayout = (ViewGroup) view.findViewById(R.id.documentIssueDateLayout);
        documentIssueDate = (TextView) view.findViewById(R.id.documentIssueDate);
        bscNumberLayout = (ViewGroup) view.findViewById(R.id.bscNumberLayout);
        bscNumber = (TextView) view.findViewById(R.id.bscNumber);
        bscType = (TextView) view.findViewById(R.id.bscType);
        Button removeBtn = (Button) view.findViewById(R.id.removeBtn);
        removeBtn.setOnClickListener(v -> presenter.onRemoveBtnClicked());

        return view;
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    @Override
    public void init(MvpDelegate parent, String id) {
        super.init(parent, id);
        presenter = getMvpDelegate().getPresenter(RemoveExemptionPresenter::new, RemoveExemptionPresenter.class);
    }

    public void initialize(RemoveExemptionParams removeExemptionParams) {
        presenter.bindInteractionListener(exemptionManualInputInteractionListener);
        presenter.initialize(removeExemptionParams);
    }

    @Override
    public void setExemptionInfo(ExemptionInfo exemptionInfo) {
        expressCode.setText(String.valueOf(exemptionInfo.expressCode));
        groupName.setText(exemptionInfo.groupName == null ? getString(R.string.remove_exemption_unknown_group) : exemptionInfo.groupName);
        percentage.setText(getString(R.string.remove_exemption_percentage, exemptionInfo.percentage));
        if (exemptionInfo.fio == null) {
            fioLayout.setVisibility(View.GONE);
        } else {
            fio.setText(exemptionInfo.fio);
        }
        if (exemptionInfo.documentNumber == null) {
            documentNumberLayout.setVisibility(View.GONE);
        } else {
            documentNumber.setText(exemptionInfo.documentNumber);
        }
        if (exemptionInfo.documentIssueDate == null) {
            documentIssueDateLayout.setVisibility(View.GONE);
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String dateString = simpleDateFormat.format(exemptionInfo.documentIssueDate);
            documentIssueDate.setText(dateString);
        }
        if (exemptionInfo.bscType == null) {
            bscNumberLayout.setVisibility(View.GONE);
        } else {
            bscNumber.setText(exemptionInfo.bscNumber);
            bscType.setText(getString(R.string.remove_exemption_bsc_type, exemptionInfo.bscType));
        }
    }

    @Override
    public void setSnilsFieldVisible(boolean visible) {
        if (visible) {
            fieldDocumentNumber.setText(R.string.remove_exemption_snils_number);
        }
    }

    @Override
    public void setDocumentNumberFieldVisible(boolean visible) {
        if (visible) {
            fieldDocumentNumber.setText(R.string.remove_exemption_document_number);
        }
    }

    private RemoveExemptionPresenter.InteractionListener exemptionManualInputInteractionListener = new RemoveExemptionPresenter.InteractionListener() {
        @Override
        public void navigateToPreviousScreen(boolean exemptionRemoved) {
            mInteractionListener.navigateToPreviousScreen(exemptionRemoved);
        }
    };

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void navigateToPreviousScreen(boolean exemptionRemoved);
    }
}
