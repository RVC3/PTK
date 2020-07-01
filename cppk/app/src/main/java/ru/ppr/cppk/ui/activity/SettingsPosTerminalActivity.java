package ru.ppr.cppk.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.Date;
import java.util.List;

import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.local.BankTransactionDao;
import ru.ppr.cppk.db.local.TerminalDayDao;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.base34.TerminalDay;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.utils.builders.events.TerminalDayGenerator;
import ru.ppr.cppk.exceptions.PrettyException;
import ru.ppr.cppk.helpers.DeferredActionHandler;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.localdb.model.BankOperationType;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.model.BluetoothDevice;
import ru.ppr.cppk.model.PosOperationResult;
import ru.ppr.cppk.pos.PosType;
import ru.ppr.cppk.printer.rx.operation.bankSlip.PrinterPrintBankSlipOperation;
import ru.ppr.cppk.service.ServiceTerminalMonitor;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.PosTypeChoiceDialog;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.ipos.model.TransactionResult;
import ru.ppr.logger.Logger;
import ru.ppr.security.entity.PermissionDvc;
import rx.AsyncEmitter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Экран настроек POS терминала.
 */
public class SettingsPosTerminalActivity extends SystemBarActivity {

    private static final String TAG = Logger.makeLogTag(SettingsPosTerminalActivity.class);

    private static final int REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE = 101;

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, SettingsPosTerminalActivity.class);
        return intent;
    }

    private volatile boolean canBack = true;

    private FeedbackProgressDialog progressDialog;

    private EditText log;
    private EditText mac_address;
    private EditText port;
    private SimpleLseView simpleLseView;

    private PosManager posManager;

    private final DeferredActionHandler deferredActionHandler = new DeferredActionHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pos_terminal_settings);

        posManager = Dagger.appComponent().posManager();

        log = (EditText) findViewById(R.id.log);
        mac_address = (EditText) findViewById(R.id.mac_address);
        port = (EditText) findViewById(R.id.port);

        findViewById(R.id.clear_log).setOnClickListener(v -> log.setText(""));

        progressDialog = new FeedbackProgressDialog(this);
        progressDialog.setCancelable(false);

        findViewById(R.id.test_connection).setOnClickListener(v -> {
            if (!posManager.isReady() || ServiceTerminalMonitor.isBusy()) {
                posTerminalBusy();

                return;
            }

            canBack = false;

            progressDialog.setMessage(getString(R.string.device_any_connection));
            progressDialog.show();

            posManager.testDevice(new PosManager.AbstractTransactionListener() {
                @Override
                public void onConnectionTimeout() {
                    runOnUiThread(() -> {

                        if (progressDialog != null)
                            progressDialog.dismiss();

                        canBack = true;

                        connectionTimeout();
                    });
                }

                @Override
                public void onResult(@NonNull PosOperationResult<TransactionResult> operationResult) {
                    runOnUiThread(() -> {
                        if (progressDialog != null)
                            progressDialog.dismiss();

                        TransactionResult result = operationResult.getTransactionResult();
                        if (result != null) {
                            deviceIsAvailable();
                        } else {
                            deviceIsNotAvailable();
                        }

                        canBack = true;
                    });
                }
            });
        });

        findViewById(R.id.bind_device).setOnClickListener(v -> {
            canBack = false;
            Navigator.navigateToBluetoothDeviceSearchActivity(this, null, REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE, new BluetoothDevice(SharedPreferencesUtils.getPosMacAddress(this), null));
        });

        findViewById(R.id.test_host).setOnClickListener(v -> {
            if (!posManager.isReady() || ServiceTerminalMonitor.isBusy()) {
                posTerminalBusy();

                return;
            }

            canBack = false;

            progressDialog.setMessage(getString(R.string.settings_pos_terminal_dialog));
            progressDialog.show();

            posManager.testHost(new PosManager.AbstractTransactionListener() {
                @Override
                public void onConnectionTimeout() {
                    runOnUiThread(() -> {
                        if (progressDialog != null)
                            progressDialog.dismiss();

                        canBack = true;

                        connectionTimeout();
                    });
                }

                @Override
                public void onResult(@NonNull PosOperationResult<TransactionResult> operationResult) {
                    runOnUiThread(() -> {
                        if (progressDialog != null)
                            progressDialog.dismiss();

                        TransactionResult result = operationResult.getTransactionResult();
                        if (result != null) {
                            logResult(result.toString());
                        }

                        if ((result != null) && result.isApproved()) {
                            testHostSuccess();
                        } else {
                            testHostFailure();
                        }


                        canBack = true;
                    });
                }
            });
        });

        findViewById(R.id.admin_menu).setOnClickListener(v -> {
            if (!posManager.isReady() || ServiceTerminalMonitor.isBusy()) {
                posTerminalBusy();

                return;
            }

            canBack = false;

            progressDialog.setMessage(getString(R.string.settings_pos_terminal_dialog));
            progressDialog.show();

            posManager.invokeAdministrativeMenu(new PosManager.AbstractTransactionListener() {
                @Override
                public void onConnectionTimeout() {
                    runOnUiThread(() -> {
                        if (progressDialog != null)
                            progressDialog.dismiss();

                        canBack = true;

                        connectionTimeout();
                    });
                }

                @Override
                public void onResult(@NonNull PosOperationResult<TransactionResult> operationResult) {
                    TransactionResult result = operationResult.getTransactionResult();
                    handleResult(result);
                }
            });
        });

        findViewById(R.id.sync_with_tms).setOnClickListener(v -> {
            if (!posManager.isReady() || ServiceTerminalMonitor.isBusy()) {
                posTerminalBusy();

                return;
            }

            canBack = false;

            progressDialog.setMessage(getString(R.string.settings_pos_terminal_dialog));
            progressDialog.show();

            posManager.syncWithTMS(new PosManager.AbstractTransactionListener() {
                @Override
                public void onConnectionTimeout() {
                    runOnUiThread(() -> {
                        if (progressDialog != null)
                            progressDialog.dismiss();

                        canBack = true;

                        connectionTimeout();
                    });
                }

                @Override
                public void onResult(@NonNull PosOperationResult<TransactionResult> operationResult) {
                    TransactionResult result = operationResult.getTransactionResult();
                    handleResult(result);
                }
            });
        });

        simpleLseView = (SimpleLseView) findViewById(R.id.simpleLseView);
    }

    @Override
    protected void onResume() {
        log.setText("");
        log.clearFocus();

        mac_address.setText(SharedPreferencesUtils.getPosMacAddress(this));
        port.setText(String.valueOf(SharedPreferencesUtils.getPosPort(this)));

        super.onResume();

        configAccess();

        deferredActionHandler.resume();
    }

    @Override
    protected void onPause() {
        deferredActionHandler.pause();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE: {
                    BluetoothDevice bluetoothDevice = data.getParcelableExtra(BluetoothDeviceSearchActivity.EXTRA_SELECTED_DEVICE);

                    canBack = false;

                    showChoicePosTypeObservable()
                            .subscribe(posType -> changeTerminalMacAddressAndDriver(bluetoothDevice, posType));
                    return;
                }
            }
        } else {
            if (requestCode == REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE) {
                canBack = true;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!canBack || (simpleLseView != null && simpleLseView.isVisible())) {
            return;
        }
        super.onBackPressed();
    }

    /**
     * Метод для изменения MAC адресса POS терминала.
     *
     * @param bluetoothDevice bluetooth устройство POS терминала.
     */
    private void changeTerminalMacAddressAndDriver(BluetoothDevice bluetoothDevice, PosType posType) {
        canBack = false;

        Logger.trace(TAG, "changeTerminalMacAddressAndDriver started");

        Observable
                .defer(() -> {
                    TerminalDayDao terminalDayDao = getLocalDaoSession().getTerminalDayDao();
                    TerminalDay lastTerminalDay = terminalDayDao.getLastTerminalDay();

                    if (lastTerminalDay == null || lastTerminalDay.getEndDateTime() != null) {
                        Logger.trace(TAG, "Day already is closed");
                        return Observable.just(null);
                    } else {
                        return showDialogDayWillBeClosedObservable(lastTerminalDay.getTerminalDayId());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aVoid2 -> {
                    progressDialog.setMessage(getString(R.string.settings_pos_terminal_dialog));
                    progressDialog.show();
                })
                .observeOn(SchedulersCPPK.background())
                .flatMap(aVoid -> {
                    if (posType == PosType.DEFAULT) {
                        throw new IllegalStateException("changeTerminalMacAddressAndDriver, posType == PosType.DEFAULT");
                    }

                    // Заинитим на время POS-терминал с новым mac-адресом
                    Logger.trace(TAG, "changeTerminalMacAddressAndDriver init with mac " + bluetoothDevice.getAddress() + " and type " + posType);
                    return Globals.updatePosTerminal(bluetoothDevice.getAddress(), SharedPreferencesUtils.getPosPort(getApplicationContext()), posType);
                })
                .flatMap(aVoid -> Observable.fromAsync(new Action1<AsyncEmitter<Void>>() {
                    @Override
                    public void call(AsyncEmitter<Void> booleanAsyncEmitter) {
                        posManager.silentDayEnd(new PosManager.AbstractTransactionListener() {

                            @Override
                            public void onConnectionTimeout() {
                                booleanAsyncEmitter.onError(new PrettyException(getResources().getString(R.string.terminal_close_day_error_timeout)));
                            }

                            @Override
                            public void onResult(@NonNull PosOperationResult<TransactionResult> operationResult) {
                                TransactionResult result = operationResult.getTransactionResult();
                                if (result != null && result.isApproved()) {
                                    // Закрылся день
                                    booleanAsyncEmitter.onNext(null);
                                    booleanAsyncEmitter.onCompleted();
                                } else {
                                    booleanAsyncEmitter.onError(new PrettyException(getString(R.string.terminal_operation_rejected, result == null ? "" : result.getBankResponse())));
                                }
                            }
                        });
                    }
                }, AsyncEmitter.BackpressureMode.ERROR))
                .doOnNext(aVoid -> {
                    TerminalDayDao terminalDayDao = getLocalDaoSession().getTerminalDayDao();
                    BankTransactionDao bankTransactionDao = getLocalDaoSession().getBankTransactionDao();
                    TerminalDay lastTerminalDay = terminalDayDao.getLastTerminalDay();

                    if (lastTerminalDay == null || lastTerminalDay != null && lastTerminalDay.getEndDateTime() != null) {
                        // День уже закрыт, не будем добавлять ещё одного события закрытия дня
                        return;
                    }

                    BankTransactionEvent lastSaleTransaction = bankTransactionDao.getLastEventByType(BankOperationType.SALE);
                    if (lastSaleTransaction != null && lastSaleTransaction.getStatus() == BankTransactionEvent.Status.COMPLETED_BUT_NOT_ASSOCIATED_WITH_FISCAL_OPERATION) {
                        lastSaleTransaction.setStatus(BankTransactionEvent.Status.COMPLETED_WITHOUT_POS);
                        bankTransactionDao.update(lastSaleTransaction);
                        Logger.trace(TAG, "Last transaction status changed to COMPLETED_WITHOUT_POS");
                    } else {
                        Logger.trace(TAG, "There is no last transaction with status COMPLETED_BUT_NOT_ASSOCIATED_WITH_FISCAL_OPERATION");
                    }

                    // Запишем в БД событие закрытия дня старого POS-терминала
                    ShiftEvent shiftEvent;
                    if (ShiftManager.getInstance().isShiftOpened()) {
                        shiftEvent = ShiftManager.getInstance().getCurrentShiftEvent();
                    } else {
                        shiftEvent = null;
                    }

                    getLocalDaoSession().getLocalDb().beginTransaction();
                    try {
                        // добавляем информацию о ПТК
                        StationDevice stationDevice = Di.INSTANCE.getDeviceSessionInfo().getCurrentStationDevice();
                        if (stationDevice != null) {
                            getLocalDaoSession().getStationDeviceDao().insertOrThrow(stationDevice);
                        }
                        Event event = Di.INSTANCE.eventBuilder()
                                .setDeviceId(stationDevice.getId())
                                .build();
                        getLocalDaoSession().getEventDao().insertOrThrow(event);

                        TerminalDay terminalDay = new TerminalDayGenerator()
                                .setTerminalDayId(lastTerminalDay.getTerminalDayId())
                                .setStartDateTime(lastTerminalDay.getStartDateTime())
                                .setEndDateTime(new Date())
                                .setReport("") // В будущем: 13.09.2016 Запихать сюда что-нибудь красивое
                                .setTerminalNumber(lastTerminalDay.getTerminalNumber())
                                .setCurrentSaleTransactionId(lastTerminalDay.getCurrentSaleTransactionId())
                                .setEvent(event)
                                .setStartShiftEventId(lastTerminalDay.getStartShiftEventId())
                                .setEndShiftEventId(shiftEvent == null ? null : shiftEvent.getId())
                                .build();
                        getLocalDaoSession().getTerminalDayDao().insertOrThrow(terminalDay);

                        getLocalDaoSession().getLocalDb().setTransactionSuccessful();
                    } finally {
                        getLocalDaoSession().getLocalDb().endTransaction();
                    }
                })
                .doOnNext(aVoid -> {
                    // Сохраним в настройки новый mac-адрес POS-терминала и драйвер
                    Logger.trace(TAG, "changeTerminalMacAddressAndDriver new mac saved to settings");
                    SharedPreferencesUtils.setPosMacAddress(getApplicationContext(), bluetoothDevice.getAddress());
                    SharedPreferencesUtils.setPosTerminalType(getApplicationContext(), posType);
                })
                .onErrorResumeNext(throwable -> {
                    // Заинитим POS-терминал со старым mac-адресом и драйвером
                    String macAddress = SharedPreferencesUtils.getPosMacAddress(getApplicationContext());
                    PosType oldPosType = SharedPreferencesUtils.getPosTerminalType(getApplicationContext());
                    Logger.trace(TAG, "changeTerminalMacAddressAndDriver init onError with old mac " + macAddress + " and type " + oldPosType);
                    return Globals.updatePosTerminal(macAddress, SharedPreferencesUtils.getPosPort(getApplicationContext()), oldPosType)
                            .flatMap((Func1<Void, Observable<Void>>) aVoid -> Observable.error(throwable));
                })
                .subscribeOn(SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        Logger.trace(TAG, "changeTerminalMacAddressAndDriver onCompleted");
                        progressDialog.dismiss();
                        canBack = true;
                    }

                    @Override
                    public void onError(Throwable e) {
                        onFailed(e);
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        Logger.trace(TAG, "changeTerminalMacAddressAndDriver onNext");
                        mac_address.setText(SharedPreferencesUtils.getPosMacAddress(getApplicationContext()));
                        onSuccess(getString(R.string.settings_pos_terminal_msg_success));
                    }
                });
    }


    /**
     * Метод для отображения диалога.
     *
     * @param dayNumber код дня.
     * @return
     */
    private Observable<Void> showDialogDayWillBeClosedObservable(long dayNumber) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                deferredActionHandler.post(() -> {
                    SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                            getString(R.string.settings_pos_terminal_msg_day_will_be_closed, dayNumber),
                            getString(R.string.btnOk),
                            getString(R.string.btnCancel),
                            LinearLayout.HORIZONTAL,
                            0);
                    simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
                    simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                    });
                    simpleDialog.setDialogNegativeBtnClickListener((dialog, dialogId) -> {
                        subscriber.onCompleted();
                    });
                    simpleDialog.setOnCancelListener(dialogInterface -> {
                        subscriber.onCompleted();
                    });
                });
            }
        });
    }

    /**
     * Метод для выполнения действий при успешном результате.
     *
     * @param message сообщение для отображения.
     */
    private void onSuccess(String message) {

        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_SUCCESS);

        stateBuilder.setTextMessage(message);
        stateBuilder.setButton1(R.string.btnOk, v -> simpleLseView.hide());

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    /**
     * Метод для выполнения действий при неуспешном результате.
     *
     * @param e данные об ошибке.
     */
    private void onFailed(Throwable e) {
        Logger.error(TAG, e.getMessage(), e);
        progressDialog.dismiss();
        canBack = true;

        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);

        if (e instanceof PrettyException && e.getMessage() != null)
            stateBuilder.setTextMessage(e.getMessage());
        else
            stateBuilder.setTextMessage(R.string.settings_pos_terminal_msg_error);
        stateBuilder.setButton1(R.string.sev_btn_cancel, v -> simpleLseView.hide());

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    /**
     * Метод для выполнения действий с полученным результатом транзакции.
     *
     * @param transactionResult результат выполнения транзакции.
     */
    private void handleResult(TransactionResult transactionResult) {

        runOnUiThread(() -> {
            if (progressDialog != null)
                progressDialog.dismiss();

            if (transactionResult != null)
                logResult(transactionResult.toString());
        });

        if (transactionResult != null && transactionResult.getReceipt() != null && !transactionResult.getReceipt().isEmpty()) {
            runOnUiThread(() -> {
                progressDialog.setMessage(getString(R.string.terminal_just_printing));
                progressDialog.show();
            });

            printTerminalReceipt(transactionResult.getReceipt());
        } else {
            runOnUiThread(this::printTerminalReceiptEmpty);

            canBack = true;
        }
    }

    /**
     * Настраиваем доступность кнопок
     */
    private void configAccess() {
        findViewById(R.id.test_host).setEnabled(getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(di().getUserSessionInfo().getCurrentUser().getRole(), PermissionDvc.TestHost));
        findViewById(R.id.admin_menu).setEnabled(getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(di().getUserSessionInfo().getCurrentUser().getRole(), PermissionDvc.AdministratorMenu));
    }

    /**
     * Метод для выполнения действий, если на POS терминале, в данный момент, выполняются задачи.
     */
    private void posTerminalBusy() {
        logResult(R.string.terminal_is_busy);
    }

    /**
     * Метод для выполнения действий, если не удалось установить подключение с POS терминалом.
     */
    private void connectionTimeout() {
        logResult(R.string.terminal_no_connection);
    }

    /**
     * Метод для выполнения действий, если удалось напечать образ чека.
     */
    private void printTerminalReceiptSuccess() {
        logResult(R.string.terminal_just_print_success);
    }

    /**
     * Метод для выполнения действий, если нет данных в образе чека.
     */
    private void printTerminalReceiptEmpty() {
        logResult(R.string.terminal_just_print_empty);
    }

    /**
     * Метод для выполнения действий, если не удалось напечать образ чека.
     */
    private void printTerminalReceiptFailed() {
        logResult(R.string.terminal_just_print_failed);
    }

    /**
     * Метод для выполнения действий, если POS терминал доступен.
     */
    private void deviceIsAvailable() {
        logResult(R.string.device_any_test_connection_available);
    }

    /**
     * Метод для выполнения действий, если POS терминал не доступен.
     */
    private void deviceIsNotAvailable() {
        logResult(R.string.device_any_test_connection_not_available);
    }

    /**
     * Метод для выполнения действий при успешном выполнении команды "Тест хоста".
     */
    private void testHostSuccess() {
        logResult(R.string.settings_pos_terminal_test_host_success);
    }

    /**
     * Метод для выполнения действий при неуспешном выполнении команды "Тест хоста".
     */
    private void testHostFailure() {
        logResult(R.string.settings_pos_terminal_test_host_failure);
    }

    /**
     * Метод для логирования результата.
     *
     * @param stringResource строковый ресурс, содержащий сообщение для логирования.
     */
    private void logResult(int stringResource) {
        final String newLogResult = getString(stringResource);
        logResult(newLogResult);
    }

    private void logResult(String text) {
        runOnUiThread(() -> {
            final String newLogResult = text + "\n" + log.getText();
            log.setText(newLogResult);
        });
    }

    /**
     * Метод для печати образа чека.
     *
     * @param receipt образ чека.
     */
    private void printTerminalReceipt(List<String> receipt) {
        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(Observable.fromCallable(() -> {
                    PrinterPrintBankSlipOperation.Params params = new PrinterPrintBankSlipOperation.Params();

                    params.tplParams.slipLines = receipt;

                    return params;
                }))
                .flatMap(params -> Di.INSTANCE.printerManager().getOperationFactory()
                        .getPrintBankSlipOperation(params)
                        .call()
                )
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                               @Override
                               public void onCompleted() {
                                   if (progressDialog != null)
                                       progressDialog.dismiss();

                                   printTerminalReceiptSuccess();

                                   canBack = true;
                               }

                               @Override
                               public void onError(Throwable error) {
                                   Logger.error(TAG, error);

                                   if (progressDialog != null)
                                       progressDialog.dismiss();

                                   printTerminalReceiptFailed();

                                   canBack = true;
                               }

                               @Override
                               public void onNext(Object object) {
                               }
                           }
                );
    }

    /**
     * Показывает диалог с выбором типа POS-терминала
     *
     * @return
     */
    private Observable<PosType> showChoicePosTypeObservable() {
        return Observable.create(subscriber -> {
            deferredActionHandler.post(() -> {
                PosTypeChoiceDialog posTypeChoiceDialog = PosTypeChoiceDialog.newInstance();
                posTypeChoiceDialog.setItemClickListener((dialogFragment, posType) -> {
                    subscriber.onNext(posType);
                    subscriber.onCompleted();
                });
                posTypeChoiceDialog.setBackClickListener(() -> {
                    posTypeChoiceDialog.dismiss();
                    subscriber.onCompleted();
                });
                posTypeChoiceDialog.show(getFragmentManager(), PosTypeChoiceDialog.FRAGMENT_TAG);
            });
        });
    }

}
