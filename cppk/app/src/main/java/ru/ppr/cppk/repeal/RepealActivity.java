package ru.ppr.cppk.repeal;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import javax.inject.Inject;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.helpers.DeferredActionHandler;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.logic.fiscalDocStateSync.FiscalDocStateSyncChecker;
import ru.ppr.cppk.logic.fiscalDocStateSync.FiscalDocStateSynchronizer;
import ru.ppr.cppk.pd.utils.reader.ReaderType;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.readpdfortransfer.model.ReadForTransferParams;
import ru.ppr.cppk.ui.activity.repealreadbarcode.RepealReadBarcodeActivity;
import ru.ppr.cppk.ui.activity.repealreadbsc.RepealReadBscActivity;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.logger.Logger;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;

/**
 * Экран аннулирования ПД.
 */
public class RepealActivity extends SystemBarActivity {

    private static final String TAG = Logger.makeLogTag(RepealActivity.class);
    private static final String DIALOG_FISCAL_DOC_STATE_SYNC_RETRY = "DIALOG_FISCAL_DOC_STATE_SYNC_RETRY";

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, RepealActivity.class);
    }

    // region Di
    private RepealComponent component;
    @Inject
    FiscalDocStateSyncChecker fiscalDocStateSyncChecker;
    @Inject
    FiscalDocStateSynchronizer fiscalDocStateSynchronizer;
    // endregion
    // region Views
    Button bscBtn;
    Button barcodeBtn;
    Button executedPdBtn;
    //endregion
    //region Other
    private final DeferredActionHandler deferredActionHandler = new DeferredActionHandler();
    private ProgressDialog syncProgressDialog;
    private Subscription syncStateSubscription = Subscriptions.unsubscribed();
    /**
     * Флаг, отображающий факт выполнения процесса синхронизаци
     */
    private boolean syncProcessIsRunning;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerRepealComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repeal_activity);

        bscBtn = (Button) findViewById(R.id.repail_read_bsc);
        bscBtn.setOnClickListener(view -> {
            Logger.info(TAG, "Нажали на кнопку считать БСК");
            if (isActivityResumed())
                startReadPd(ReaderType.TYPE_BSC, null);
        });

        barcodeBtn = (Button) findViewById(R.id.repail_read_barcode);
        barcodeBtn.setOnClickListener(v -> {
            Logger.info(TAG, "Нажали на кнопку считать ШК");
            if (isActivityResumed())
                startReadPd(ReaderType.TYPE_BARCODE, null);
        });

        executedPdBtn = (Button) findViewById(R.id.repail_executed_pd);
        executedPdBtn.setOnClickListener(v -> {
            Logger.info(TAG, "Нажали на кнопку Оформленные ПД");
            Navigator.navigateToRepealFromHistoryActivity(this);
        });

        // Проверяем наличие несинхронизированных документов
        checkFiscalDocsState();
    }

    /**
     * Выполняет проверку наличия фискальных документов в несинхронизированнном состоянии.
     * Запускает принудительную синхронизацию при их наличии.
     */
    private void checkFiscalDocsState() {
        Logger.info(TAG, "checkFiscalDocsState");
        // Проверяем наличие несинхронизированных документов
        FiscalDocStateSyncChecker.Result result = fiscalDocStateSyncChecker.check();

        if (!result.isEmpty()) {
            // Если есть несинхронизированные документы
            // Запускаем процесс синхронихации состояния чеков
            startFiscalDocStateSynchronization();
            return;
        }

        // Разрешаем считывание ПД боковыми кнопками
        canUserHardwareButton();
    }

    /**
     * Запускает процесс принудительной синхронизации состояния чеков.
     */
    private void startFiscalDocStateSynchronization() {
        Logger.info(TAG, "startFiscalDocStateSynchronization");

        if (syncProcessIsRunning) {
            throw new IllegalStateException("Sync process is already running");
        }

        syncProcessIsRunning = true;
        setRetrySyncDialogVisible(false);
        setSyncProgressDialogVisible(true);

        syncStateSubscription = fiscalDocStateSynchronizer.rxSyncCheckState()
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    setSyncProgressDialogVisible(false);
                    // Разрешаем считывание ПД боковыми кнопками
                    canUserHardwareButton();
                    syncProcessIsRunning = false;
                }, error -> {
                    Logger.error(TAG, error);
                    setSyncProgressDialogVisible(false);
                    setRetrySyncDialogVisible(true);
                    syncProcessIsRunning = false;
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        deferredActionHandler.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        deferredActionHandler.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        syncStateSubscription.unsubscribe();
    }

    @Override
    public void onBackPressed() {
        if (!syncProcessIsRunning) {
            // Если сейчас не выполняет процесс синхронизации, разрешаем обработку нажатия кнопки "Назад"
            super.onBackPressed();
        }
    }

    @Override
    public void startReadPd(ReaderType readerType, ReadForTransferParams readForTransferParams) {
        if (!isAlreadyRead()) {
            setAlreadyRead(true);
            _readerType = readerType;

            Intent intent;

            if (_readerType == ReaderType.TYPE_BSC) {
                Logger.info(TAG, SystemBarActivity.class.getSimpleName() + " - запускаем " + RepealReadBscActivity.class.getSimpleName() + " " + ReaderType.TYPE_BSC.toString());

                intent = RepealReadBscActivity.getCallingIntent(this);
            } else { // В будущем
                Logger.info(TAG, SystemBarActivity.class.getSimpleName() + " - запускаем " + RepealReadBarcodeActivity.class.getSimpleName() + " " + ReaderType.TYPE_BARCODE.toString());

                intent = RepealReadBarcodeActivity.getCallingIntent(this);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            startActivity(intent);
        }
    }

    /**
     * Настараивает видимость диалога процесса синхронизации состояния чеков.
     *
     * @param visible {@code true} для отображения диалога, {@code false} - для скрытия
     */
    private void setSyncProgressDialogVisible(boolean visible) {
        if (visible) {
            deferredActionHandler.post(() -> {
                if (syncProgressDialog == null) {
                    syncProgressDialog = new FeedbackProgressDialog(this);
                }
                syncProgressDialog.setCancelable(false);
                syncProgressDialog.setCanceledOnTouchOutside(false);
                syncProgressDialog.setMessage(getString(R.string.repeal_activity_dialog_fiscal_doc_state_sync_processing_message));
                syncProgressDialog.show();
            });
        } else {
            if (syncProgressDialog != null) {
                syncProgressDialog.dismiss();
                syncProgressDialog = null;
            }
        }
    }

    /**
     * Настараивает видимость диалога ошибки синхронизации состояния чеков.
     *
     * @param visible {@code true} для отображения диалога, {@code false} - для скрытия
     */
    private void setRetrySyncDialogVisible(boolean visible) {
        if (visible) {
            deferredActionHandler.post(() -> {
                SimpleDialog dialogFiscalDocStateSyncRetry = (SimpleDialog) getFragmentManager().findFragmentByTag(DIALOG_FISCAL_DOC_STATE_SYNC_RETRY);

                if (dialogFiscalDocStateSyncRetry == null) {
                    dialogFiscalDocStateSyncRetry = SimpleDialog.newInstance(null,
                            getString(R.string.repeal_activity_dialog_fiscal_doc_state_sync_retry_message),
                            getString(R.string.repeal_activity_dialog_fiscal_doc_state_sync_retry_ok),
                            getString(R.string.repeal_activity_dialog_fiscal_doc_state_sync_retry_close),
                            LinearLayout.HORIZONTAL,
                            0);
                    dialogFiscalDocStateSyncRetry.setCancelable(false);
                }

                dialogFiscalDocStateSyncRetry.setDialogPositiveBtnClickListener((dialog, dialogId) -> startFiscalDocStateSynchronization());
                dialogFiscalDocStateSyncRetry.setDialogNegativeBtnClickListener((dialog, dialogId) -> finish());

                dialogFiscalDocStateSyncRetry.show(getFragmentManager(), DIALOG_FISCAL_DOC_STATE_SYNC_RETRY);
            });
        } else {
            SimpleDialog dialogFiscalDocStateSyncRetry = (SimpleDialog) getFragmentManager().findFragmentByTag(DIALOG_FISCAL_DOC_STATE_SYNC_RETRY);

            if (dialogFiscalDocStateSyncRetry != null) {
                dialogFiscalDocStateSyncRetry.dismiss();
            }
        }
    }

}
