package ru.ppr.cppk.ui.fragment.exemptionEnterSurname;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.core.logic.FioNormalizer;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.ExemptionGroup;
import ru.ppr.nsi.repository.ExemptionGroupRepository;
import ru.ppr.nsi.repository.ExemptionRepository;
import ru.ppr.utils.CommonUtils;

/**
 * @author Aleksandr Brazhkin
 */
public class ExemptionEnterSurnamePresenter extends BaseMvpViewStatePresenter<ExemptionEnterSurnameView, ExemptionEnterSurnameViewState> {

    private static final String TAG = Logger.makeLogTag(ExemptionEnterSurnamePresenter.class);

    private static final Pattern FIO
            = Pattern.compile(
            "[a-zA-Zа-яёА-ЯЁ-]+\\s+[a-zA-Zа-яёА-ЯЁ]+\\.?\\s+[a-zA-Zа-яёА-ЯЁ]+\\.?\\s*"
    );

    private InteractionListener mInteractionListener;

    private boolean mInitialized = false;
    // Ext
    private List<ExemptionForEvent> exemptionsForEvent;
    private ExemptionGroupRepository exemptionGroupRepository;
    private ExemptionRepository exemptionRepository;
    private FioNormalizer fioNormalizer;
    // Local
    private List<Exemption> exemptions = new ArrayList<>();
    private Exemption exemptionForUi;

    public ExemptionEnterSurnamePresenter() {

    }

    @Override
    protected ExemptionEnterSurnameViewState provideViewState() {
        return new ExemptionEnterSurnameViewState();
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    void initialize(List<ExemptionForEvent> exemptionsForEvent,
                    ExemptionGroupRepository exemptionGroupRepository,
                    ExemptionRepository exemptionRepository,
                    FioNormalizer fioNormalizer) {
        if (!mInitialized) {
            mInitialized = true;
            this.exemptionsForEvent = exemptionsForEvent;
            this.exemptionGroupRepository = exemptionGroupRepository;
            this.exemptionRepository = exemptionRepository;
            this.fioNormalizer = fioNormalizer;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");


        for (ExemptionForEvent exemptionForEvent : exemptionsForEvent) {
            Exemption exemption = exemptionRepository.getExemption(exemptionForEvent.getCode(), exemptionForEvent.getActiveFromDate(), exemptionForEvent.getVersionId());
            exemptions.add(exemption);
        }

        // http://agile.srvdev.ru/browse/CPPKPP-34481
        // Со слов Рзянкиной Натальи Владимировны данные в льготах из разных регоинов не пойдут в конфликт,
        // поэтому отображаем информацию в UI только по первой льготе
        // * Множество разных регионов в даннос случае ограничено Москвой и Московской областью
        exemptionForUi = exemptions.get(0);

        ExemptionGroup exemptionGroup = exemptionGroupRepository.load(exemptionForUi.getExemptionGroupCode(), exemptionForUi.getVersionId());

        ExemptionEnterSurnameView.ExemptionInfo exemptionInfo = new ExemptionEnterSurnameView.ExemptionInfo();
        exemptionInfo.exemptionExpressCode = exemptionForUi.getExemptionExpressCode();
        exemptionInfo.percentage = exemptionForUi.getPercentage();
        exemptionInfo.groupName = exemptionGroup == null ? null : exemptionGroup.getGroupName();

        view.setExemptionInfo(exemptionInfo);
        view.setSnilsFieldVisible(exemptionForUi.isRequireSnilsNumber());
        view.setDocumentNumberFieldVisible(!exemptionForUi.isRequireSnilsNumber());
        view.setIssueDateFieldVisible(isExemptionForMilitaryRequirement());
    }

    void onUseExemptionBtnClicked(String fio, String documentNumber, Date issueDate) {
        if (!checkData(fio, documentNumber, issueDate)) {
            return;
        }

        for (int i = 0; i < exemptionsForEvent.size(); i++) {
            Exemption exemption = exemptions.get(i);
            ExemptionForEvent exemptionForEvent = exemptionsForEvent.get(i);
            exemptionForEvent.setIssueDate(issueDate);
            exemptionForEvent.setFio(TextUtils.isEmpty(fio) ? null : fioNormalizer.getNormalizedFio(fio));
            exemptionForEvent.setNumberOfDocumentWhichApproveExemption(TextUtils.isEmpty(documentNumber) ? null : documentNumber);
            exemptionForEvent.setRegionOkatoCode(exemption.getRegionOkatoCode());
            exemptionForEvent.setRequireSocialCard(exemption.isRequireSocialCard());
        }

        mInteractionListener.onExemptionSelected(exemptionsForEvent);
    }

    boolean onBackPressed() {
        mInteractionListener.onCancelSelectExemption();
        return true;
    }


    /**
     * Проверяет корректность данных в полях ввода.
     */
    private boolean checkData(String fio, String documentNumber, Date issueDate) {
        if (!exemptionForUi.isNotRequireDocumentNumber() && TextUtils.isEmpty(documentNumber.trim())) {
            view.showEmptyDocumentError(exemptionForUi.isRequireSnilsNumber());
            return false;
        }
        if (exemptionForUi.isRequireSnilsNumber() && !CommonUtils.checkSnils(documentNumber)) {
            view.showInvalidSnilsError();
            return false;
        }
        if (exemptionForUi.isNotRequireFIO()) {
            // Если ФИО НЕ требуется, то оставляем валидацию в случае если оно не пустое,
            // пустое значение считается валидным, см. комментарий http://agile.srvdev.ru/browse/CPPKPP-38054
            if (!TextUtils.isEmpty(fio.trim()) && !validateFio(fio)) {
                view.showInvalidFioError();
                return false;
            }
        } else {
            // Если ФИО требуется, то валидируем как полагается
            if (TextUtils.isEmpty(fio.trim()) || !validateFio(fio)) {
                view.showInvalidFioError();
                return false;
            }
        }
        if (isExemptionForMilitaryRequirement() && issueDate == null) {
            view.showEmptyIssueDateError();
            return false;
        }
        return true;
    }

    private boolean validateFio(String fio) {
        return FIO.matcher(fio).matches();
    }

    /**
     * Проверяет, что это льгота для военнослужащих.
     */
    private boolean isExemptionForMilitaryRequirement() {
        int code = exemptionForUi.getExemptionExpressCode();
        return (code == 2304 || code / 100 == 21);
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void onExemptionSelected(@NonNull List<ExemptionForEvent> exemptionsForEvent);

        void onCancelSelectExemption();
    }

}
