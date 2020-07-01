package ru.ppr.cppk.logic.fiscalDocStateSync;

import java.math.BigDecimal;

import javax.inject.Inject;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.cppk.logic.DocumentNumberProvider;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.fiscalDocStateSync.base.FiscalDocStatusUpdater;
import ru.ppr.cppk.logic.fiscalDocStateSync.updater.CloseShiftEventStatusUpdater;
import ru.ppr.cppk.logic.fiscalDocStateSync.updater.FineSaleEventStatusUpdater;
import ru.ppr.cppk.logic.fiscalDocStateSync.updater.TestPdSaleEventStatusUpdater;
import ru.ppr.cppk.logic.fiscalDocStateSync.updater.TicketReturnEventStatusUpdater;
import ru.ppr.cppk.logic.fiscalDocStateSync.updater.TicketSaleEventStatusUpdater;
import ru.ppr.cppk.logic.interactor.PdValidityPeriodCalculator;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.printer.rx.operation.PrinterGetLastDocumentInfo;
import ru.ppr.cppk.printer.rx.operation.base.OperationFactory;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.repository.TicketTypeRepository;
import rx.Single;

/**
 * @author Dmitry Nevolin
 */
public class FiscalDocStateSynchronizer {

    private static final String TAG = Logger.makeLogTag(FiscalDocStateSynchronizer.class);

    private final ShiftManager shiftManager;
    private final LocalDaoSession localDaoSession;
    private final DocumentNumberProvider documentNumberProvider;
    private final NsiVersionManager nsiVersionManager;
    private final OperationFactory operationFactory;
    private final FiscalDocStateSyncChecker fiscalDocStateSyncChecker;
    private final PdValidityPeriodCalculator pdValidityPeriodCalculator;
    private final TicketTypeRepository ticketTypeRepository;

    @Inject
    public FiscalDocStateSynchronizer(ShiftManager shiftManager,
                                      LocalDaoSession localDaoSession,
                                      DocumentNumberProvider documentNumberProvider,
                                      NsiVersionManager nsiVersionManager,
                                      OperationFactory operationFactory,
                                      FiscalDocStateSyncChecker fiscalDocStateSyncChecker,
                                      PdValidityPeriodCalculator pdValidityPeriodCalculator,
                                      TicketTypeRepository ticketTypeRepository) {
        Logger.trace(TAG, "Create Instance");
        this.shiftManager = shiftManager;
        this.localDaoSession = localDaoSession;
        this.documentNumberProvider = documentNumberProvider;
        this.nsiVersionManager = nsiVersionManager;
        this.operationFactory = operationFactory;
        this.fiscalDocStateSyncChecker = fiscalDocStateSyncChecker;
        this.pdValidityPeriodCalculator = pdValidityPeriodCalculator;
        this.ticketTypeRepository = ticketTypeRepository;
    }

    /**
     * Результат выполнения операции синхронизации
     */
    public class SyncStateResult {
        /**
         * Необходимость синхронизации
         */
        boolean isSyncNeeded = false;
        /**
         * Флаг того что последний документ лег на ФР
         */
        boolean isLastPdLayDown = false;

        public boolean isSyncNeeded() {
            return isSyncNeeded;
        }

        public void setSyncNeeded(boolean syncNeeded) {
            isSyncNeeded = syncNeeded;
        }

        public boolean isLastPdLayToFr() {
            return isLastPdLayDown;
        }

        public void setLastPdLayDown(boolean lastPdLayDown) {
            isLastPdLayDown = lastPdLayDown;
        }
    }

    /**
     * Вернет true если чек лег на ФР, false - если не лег
     *
     * @return
     * @throws PrinterException
     */
    public SyncStateResult syncCheckState() throws PrinterException {

        Logger.trace(TAG, "syncCheckState() START");

        SyncStateResult syncStateResult = new SyncStateResult();

        FiscalDocStateSyncChecker.Result result = fiscalDocStateSyncChecker.check();

        if (!result.isEmpty()) {

            syncStateResult.setSyncNeeded(true);

            PrinterGetLastDocumentInfo.Result printerInfo;
            BigDecimal printerTotal;

            try {
                printerInfo = operationFactory.getGetLastDocumentInfoOperation().call()
                        .toBlocking()
                        .single();
                printerTotal = operationFactory.getGetCashInFrOperation().call()
                        .toBlocking()
                        .single()
                        .getCashInFR();
            } catch (Exception exception) {
                throw new PrinterException(exception);
            }

            FiscalDocStatusUpdater<?> fiscalDocStatusUpdater;

            if (result.getCloseShiftEvent() != null) {
                fiscalDocStatusUpdater = new CloseShiftEventStatusUpdater(localDaoSession,
                        documentNumberProvider,
                        shiftManager,
                        printerInfo.getSpnd(),
                        printerInfo.getOperationTime(),
                        result.getCloseShiftEvent());
            } else if (result.getTicketSaleEvent() != null) {
                fiscalDocStatusUpdater = new TicketSaleEventStatusUpdater(localDaoSession,
                        documentNumberProvider,
                        printerInfo.getSpnd(),
                        printerInfo.getOperationTime(),
                        nsiVersionManager,
                        result.getTicketSaleEvent(),
                        pdValidityPeriodCalculator,
                        ticketTypeRepository);
            } else if (result.getTicketReturnEvent() != null) {
                fiscalDocStatusUpdater = new TicketReturnEventStatusUpdater(localDaoSession,
                        documentNumberProvider,
                        printerInfo.getSpnd(),
                        printerInfo.getOperationTime(),
                        result.getTicketReturnEvent());
            } else if (result.getFineSaleEvent() != null) {
                fiscalDocStatusUpdater = new FineSaleEventStatusUpdater(localDaoSession,
                        documentNumberProvider,
                        printerInfo.getSpnd(),
                        printerInfo.getOperationTime(),
                        result.getFineSaleEvent());
            } else {//if (result.getTestTicketEvent() != null)
                fiscalDocStatusUpdater = new TestPdSaleEventStatusUpdater(localDaoSession,
                        documentNumberProvider,
                        printerInfo.getSpnd(),
                        printerInfo.getOperationTime(),
                        result.getTestTicketEvent());
            }

            FiscalDocStateSyncInfoBuilder.Info currentInfo = new FiscalDocStateSyncInfoBuilder(localDaoSession, shiftManager.getCurrentShiftId()).build();

            Logger.info(TAG, "syncCheckState() printerInfo=" + printerInfo.toString());
            Logger.info(TAG, "syncCheckState() printerTotal=" + printerTotal.toString());
            Logger.info(TAG, "syncCheckState() currentInfo=" + currentInfo.toString());

            // Если равны и сквозной номер и выручка, значит состояние синхронизировано
            // помечаем события соответствующим статусом
            //http://agile.srvdev.ru/browse/CPPKPP-38173 - на текущий момент у нас нет возможности достоверно достать сумму по ФР с Штриха
            if (printerInfo.getSpnd() == currentInfo.getNumber() /*&& printerTotal.compareTo(currentInfo.getTotal()) == 0*/) {
                Logger.info(TAG, "syncCheckState() чек не лег на фискальник, синхронизация не требуется - помечаем чек как BROKEN");
                fiscalDocStatusUpdater.updateToBroken();
                Logger.info(TAG, "syncCheckState() updateToBroken ready");

                syncStateResult.setLastPdLayDown(false);
                return syncStateResult;
            }

            Logger.info(TAG, "syncCheckState() чек лег на фискальник, требуется синхронизация");


            BigDecimal checkedDocValue;

            if (result.getCloseShiftEvent() != null) {
                checkedDocValue = BigDecimal.ZERO;
            } else if (result.getTicketSaleEvent() != null) {
                TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao()
                        .load(result.getTicketSaleEvent().getTicketSaleReturnEventBaseId());
                Price fullPrice = localDaoSession.getPriceDao().load(ticketSaleReturnEventBase.getFullPriceId());
                checkedDocValue = fullPrice.getPayed();
            } else if (result.getTicketReturnEvent() != null) {
                Price price = localDaoSession.getPriceDao().load(result.getTicketReturnEvent().getPriceId());
                checkedDocValue = price.getPayed();
            } else if (result.getFineSaleEvent() != null) {
                checkedDocValue = result.getFineSaleEvent().getAmount();
            } else {//if (result.getTestTicketEvent() != null)
                checkedDocValue = BigDecimal.ZERO;
            }

            int numberDiff = printerInfo.getSpnd() - currentInfo.getNumber();
            BigDecimal totalDiff = printerTotal.subtract(currentInfo.getTotal());
            // см. http://agile.srvdev.ru/browse/CPPKPP-35201
            // Если разница не только в "зависшем" документе, вероятно принтером пользовалось
            // другое оборудование, считаем что чек напечатан успешно и выводим информацию в лог
            if (numberDiff != 1 || totalDiff.compareTo(checkedDocValue) != 0) {
                Logger.error(TAG, "syncCheckState() !!!ATTENTION!!! Printer maybe used by other devices, " +
                        "numberDiff: " + numberDiff + "; totalDiff: " + totalDiff + "; checkedDocValue: " + checkedDocValue);
            }

            Logger.info(TAG, "syncCheckState() ставим чеку статус CheckPrinted");
            fiscalDocStatusUpdater.updateToCheckPrinted();
            Logger.trace(TAG, "syncCheckState() updateToCheckPrinted ready");

            syncStateResult.setLastPdLayDown(true);

        }
        Logger.trace(TAG, "syncCheckState() FINISH");


        return syncStateResult;
    }

    public Single<SyncStateResult> rxSyncCheckState() {
        return Single.fromCallable(this::syncCheckState);
    }

}
