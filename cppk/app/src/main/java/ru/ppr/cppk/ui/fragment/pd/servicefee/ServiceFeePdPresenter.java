package ru.ppr.cppk.ui.fragment.pd.servicefee;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.ui.fragment.pd.simple.model.ServicePdViewModel;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.ServiceFee;
import ru.ppr.nsi.repository.ServiceFeeRepository;

/**
 * @author Dmitry Nevolin
 */
class ServiceFeePdPresenter extends BaseMvpViewStatePresenter<ServiceFeePdView, ServiceFeePdViewState> {

    private static final String TAG = Logger.makeLogTag(ServiceFeePdPresenter.class);

    private boolean initialized = false;

    private final PD pd;
    private final NsiVersionManager nsiVersionManager;
    private final ServiceFeeRepository serviceFeeRepository;
    private final ServicePdViewModel pdViewModel = new ServicePdViewModel();

    @Inject
    ServiceFeePdPresenter(ServiceFeePdViewState viewState,
                          @NonNull PD pd,
                          @NonNull NsiVersionManager nsiVersionManager,
                          @NonNull ServiceFeeRepository serviceFeeRepository) {
        super(viewState);
        this.pd = pd;
        this.nsiVersionManager = nsiVersionManager;
        this.serviceFeeRepository = serviceFeeRepository;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        ServiceFee serviceFee = serviceFeeRepository.load(pd.serviceFeeCode, nsiVersionManager.getCurrentNsiVersionId());

        if (serviceFee == null) {
            Logger.info(TAG, "serviceFee == null");

            view.updateServiceFeeNotFound(true);
        } else {
            // --- Устанавлиаем заголовок
            view.updatePdTitle(serviceFee.getName());
            pdViewModel.setTitle(serviceFee.getName());
            // --- Устанавлиаем номер
            view.updatePdNumber(pd.numberPD);
            pdViewModel.setNumber(pd.numberPD);
            // --- Устанавлиаем даты
            Date startPdDate = pd.getStartPdDate();

            view.updateDateActionsFrom(startPdDate);
            pdViewModel.setValidityFromDate(startPdDate);
            // Если null значит срок действия неограничен (http://agile.srvdev.ru/browse/CPPKPP-36175)
            if (serviceFee.getValidityPeriod() == null) {
                view.updateDateActionsTo(null);
            } else {
                // вычитаем 1 день, т.к. 1й день действия уже учтен во времени начала действия ПД
                Calendar endTime = Calendar.getInstance();
                endTime.setTime(startPdDate);
                endTime.add(Calendar.DAY_OF_MONTH, serviceFee.getValidityPeriod() - 1);

                view.updateDateActionsTo(endTime.getTime());
                pdViewModel.setValidityToDate(endTime.getTime());
            }
            // --- Проверяем валидность
            List<PassageResult> errors = pd.errors;

            if (errors.isEmpty()) {
                Logger.info(TAG, "ПД(услуга) валиден");
                view.updatePdValid(true);
                pdViewModel.setValid(true);
            } else {
                Logger.info(TAG, "ПД(услуга) не валиден '" + errors.toString() + "'");
                view.updatePdValid(false);
                view.updatePdErrors(errors);
                pdViewModel.setValid(false);
                pdViewModel.setValidityFromDateError(errors.contains(PassageResult.TooEarly));
                pdViewModel.setValidityToDateError(errors.contains(PassageResult.TooLate));

                if (errors.contains(PassageResult.InvalidSign)) {
                    pdViewModel.setInvalidEdsKeyError(true);
                } else if (errors.contains(PassageResult.SignKeyRevoked)) {
                    pdViewModel.setRevokedEdsKeyError(true);
                } else if (errors.contains(PassageResult.BannedByStopListTickets)) {
                    pdViewModel.setTicketInStopListError(true);
                }
            }
        }
    }

    void onZoomPdClicked() {
        view.showZoomPdDialog(pdViewModel);
    }
}
