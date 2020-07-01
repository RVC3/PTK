package ru.ppr.cppk.ui.activity.settingsPrinter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.helpers.DeferredActionHandler;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.logic.EKLZActivator;
import ru.ppr.cppk.logic.EklzChecker;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.interactor.CheckShiftOpenedInDbInteractor;
import ru.ppr.cppk.logic.interactor.CloseShiftInDbInteractor;
import ru.ppr.cppk.model.BluetoothDevice;
import ru.ppr.cppk.printer.rx.operation.closeShift.CloseShiftOperation;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.BluetoothDeviceSearchActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.PrinterEnterSerialNumberDialog;
import ru.ppr.cppk.ui.dialog.PrinterModeChoiceDialog;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.ikkm.exception.PrinterIsNotConnectedException;
import ru.ppr.logger.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Экран настроек принтера.
 */
public class SettingsPrinterActivity extends SystemBarActivity {

    private static final String TAG = Logger.makeLogTag(SettingsPrinterActivity.class);

    // EXTRAS
    public static final String EXTRA_IS_EKLZ_ACTIVATED = "EXTRA_IS_EKLZ_ACTIVATED";

    private static final int REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE = 101;

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, SettingsPrinterActivity.class);
        return intent;
    }


    //region Di
    SettingsPrinterComponent component;
    CheckShiftOpenedInDbInteractor checkShiftOpenedInDbInteractor;
    CloseShiftInDbInteractor closeShiftInDbInteractor;
    //endregion
    // Views
    private FeedbackProgressDialog progressDialog;
    private TextView macAddress;
    private SimpleLseView simpleLseView;
    private Button activateEKLZBtn;

    // Other
    private final DeferredActionHandler deferredActionHandler = new DeferredActionHandler();

    private volatile boolean isInProgress = false;
    private boolean shiftWasClosed = false;
    private Integer tmpPrinterMode;
    private String tmpSerialNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = new SettingsPrinterComponent(di());
        checkShiftOpenedInDbInteractor = component.checkShiftOpenedInDbInteractor();
        closeShiftInDbInteractor = component.closeShiftInDbInteractor();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings_printer);

        macAddress = (TextView) findViewById(R.id.macAddress);
        macAddress.setText(Di.INSTANCE.printerManager().getPrinterMacAddress());

        findViewById(R.id.testConnection).setOnClickListener(v -> testConnection());
        findViewById(R.id.bindDevice).setOnClickListener(v -> bindDevice());
        findViewById(R.id.printAdjustingTable).setOnClickListener(v -> printAdjustingTable());
        activateEKLZBtn = (Button) findViewById(R.id.activateEKLZ);
        activateEKLZBtn.setOnClickListener(v -> activateEKLZ());

        simpleLseView = (SimpleLseView) findViewById(R.id.simpleLseView);

        progressDialog = new FeedbackProgressDialog(this);
        progressDialog.setCancelable(false);

        updateButtonsVisibility();

    }

    private void updateButtonsVisibility() {
        activateEKLZBtn.setVisibility(di().printerManager().getPrinter().isFederalLaw54Supported() ? View.GONE : View.VISIBLE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        deferredActionHandler.resume();
    }

    @Override
    protected void onPause() {
        deferredActionHandler.pause();
        super.onPause();
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
        if (isInProgress || (simpleLseView != null && simpleLseView.isVisible())) {
            return;
        }

        if (shiftWasClosed) {
            Navigator.navigateToWelcomeActivity(this, false);

            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE: {
                    BluetoothDevice bluetoothDevice = data.getParcelableExtra(BluetoothDeviceSearchActivity.EXTRA_SELECTED_DEVICE);
                    changePrinterMacAddress(bluetoothDevice);
                    return;
                }
            }
        } else {
            if (requestCode == REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE) {
                isInProgress = false;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Запускает экран поиска Bluetooth-устройств.
     */
    private void bindDevice() {

        if (isInProgress) {
            return;
        }

        isInProgress = true;

        Navigator.navigateToBluetoothDeviceSearchActivity(
                this,
                null,
                REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE,
                Di.INSTANCE.printerManager().getBluetoothDevice());

    }

    /**
     * Проверяет наличие подключения к принтеру.
     */
    private void testConnection() {

        if (isInProgress) {
            return;
        }

        isInProgress = true;
        progressDialog.setMessage(getString(R.string.device_any_connection));
        progressDialog.show();

        Di.INSTANCE.printerManager().getOperationFactory().getGetDateOperation()
                .call()
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Date>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                        isInProgress = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        onFailed(e);
                    }

                    @Override
                    public void onNext(Date date) {
                        onSuccess(getString(R.string.device_any_test_connection_available));
                    }
                });
    }

    /**
     * Устанавливает новый mac в настройках ПТК.
     *
     * @param bluetoothDevice
     */
    private void changePrinterMacAddress(BluetoothDevice bluetoothDevice) {
        isInProgress = true;
        tmpPrinterMode = null;
        tmpSerialNumber = null;

        Observable
                .defer(() -> {
                    if (ShiftManager.getInstance().isShiftOpened()) {
                        return showDialogShiftWillBeClosedObservable().flatMap(aVoid -> closeShiftIfNeed());
                    } else {
                        return Observable.just(null);
                    }
                })
                .flatMap(aVoid -> showChoicePrinterTypeObservable())
                .flatMap(printerMode -> {
                    this.tmpPrinterMode = printerMode;

                    return Observable.just(null);
                })
                .flatMap(aVoid -> showEnterPrinterSerialNumberDialogObservable())
                .flatMap(serialNumber -> {
                    this.tmpSerialNumber = serialNumber;

                    return Observable.just(null);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aVoid -> {
                    progressDialog.setMessage(getString(R.string.settings_printer_binding));
                    progressDialog.show();
                })
                .observeOn(SchedulersCPPK.printer())
                .doOnNext(aVoid -> Di.INSTANCE.printerManager().setTempMacAddressAndPrinterMode(bluetoothDevice.getAddress(), tmpPrinterMode))
                .flatMap(aVoid -> Di.INSTANCE.printerManager().getOperationFactory().getGetStateOperation().call())
                .flatMap(result -> {
                    if (result.isShiftOpened()) {
                        return showDialogPrinterTimeObservable(result.getOperationTime())
                                .flatMap(aVoid -> showDialogPrinterShiftShouldBeClosedObservable())
                                .doOnNext(aVoid2 -> {
                                    progressDialog.setMessage(getString(R.string.settings_printer_closing_shift_at_printer));
                                    progressDialog.show();
                                })
                                .observeOn(SchedulersCPPK.printer())
                                .flatMap(aVoid -> {
                                    CloseShiftOperation.Params params = new CloseShiftOperation.Params(Di.INSTANCE.fiscalHeaderParamsBuilder().build(), false);
                                    return Di.INSTANCE.printerManager().getOperationFactory().getCloseShiftOperation(params).call();
                                })
                                .flatMap(result1 -> showDialogPrinterTimeObservable(result.getOperationTime()))
                                .subscribeOn(AndroidSchedulers.mainThread());
                    } else {
                        return showDialogPrinterTimeObservable(result.getOperationTime());
                    }
                })
                .observeOn(SchedulersCPPK.printer())
                .doOnError(throwable -> Di.INSTANCE.printerManager().restoreRealMacAddressAndPrinterMode())
                .doOnNext(aVoid -> Di.INSTANCE.printerManager().restoreRealMacAddressAndPrinterMode())
                .toList()
                .flatMap(voids -> {
                    // ХАК, чтобы вернуть настоящий MAC, если пользователь нажмет кнопку "Отмена" в каком-нибудь диалоге
                    if (voids.isEmpty()) {
                        return Observable
                                .fromCallable(() -> {
                                    Di.INSTANCE.printerManager().restoreRealMacAddressAndPrinterMode();
                                    return null;
                                })
                                .flatMap(o -> Observable.empty());
                    } else {
                        return Observable.just(null);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aVoid2 -> {
                    progressDialog.setMessage(getString(R.string.settings_printer_binding));
                    progressDialog.show();
                })
                .observeOn(SchedulersCPPK.background())
                .flatMap(aVoid -> Di.INSTANCE.printerManager().changePrinterMacAddressAndMode(bluetoothDevice.getAddress(), tmpPrinterMode).toObservable())
                .flatMap(aVoid -> {
                    String serialNumber = tmpSerialNumber;
                    String model = Di.INSTANCE.printerManager().getCashRegister().getModel();
                    //сохраняем заводской номер
                    SharedPreferencesUtils.setSerialNumber(getApplicationContext(), serialNumber);
                    SharedPreferencesUtils.setModel(getApplicationContext(), model);
                    //переположим в DI текущий девайс
                    di().getDeviceSessionInfo().setCurrentStationDevice(StationDevice.getThisDevice());

                    return Observable.just((Void) null);
                })
                // В будущем tmpSerialNumber
                .subscribeOn(SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                        isInProgress = false;
                        tmpPrinterMode = null;
                    }

                    @Override
                    public void onError(Throwable e) {
                        tmpPrinterMode = null;
                        onFailed(e);
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        macAddress.setText(Di.INSTANCE.printerManager().getPrinterMacAddress());
                        onSuccess(getString(R.string.settings_printer_msg_success));
                        updateButtonsVisibility();
                    }
                });

    }

    /**
     * Показывает диалог о предстояющем автоматическом закрытии смены на ПТК.
     *
     * @return
     */
    private Observable<Void> showDialogShiftWillBeClosedObservable() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                deferredActionHandler.post(() -> {
                    SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                            getString(R.string.settings_printer_msg_shift_will_be_closed),
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
     * Показывает диалог с выбором типа принтера
     *
     * @return
     */
    private Observable<Integer> showChoicePrinterTypeObservable() {
        return Observable.create(subscriber -> deferredActionHandler.post(() -> {
            PrinterModeChoiceDialog printerModeChoiceDialog = PrinterModeChoiceDialog.newInstance();
            printerModeChoiceDialog.setItemClickListener((dialogFragment, printerMode) -> {
                subscriber.onNext(printerMode);
                subscriber.onCompleted();
            });
            printerModeChoiceDialog.setBackClickListener(() -> {
                printerModeChoiceDialog.dismiss();
                subscriber.onCompleted();
            });
            printerModeChoiceDialog.show(getFragmentManager(), PrinterModeChoiceDialog.FRAGMENT_TAG);
        }));
    }

    /**
     * Показывает диалог с вводом серийного номера принтера
     */
    private Observable<String> showEnterPrinterSerialNumberDialogObservable() {
        return Observable.create(subscriber -> deferredActionHandler.post(() -> {
            PrinterEnterSerialNumberDialog printerEnterSerialNumberDialog = PrinterEnterSerialNumberDialog.newInstance();
            printerEnterSerialNumberDialog.setSerialNumberEnterListener((dialogFragment, serialNumber) -> {
                subscriber.onNext(serialNumber);
                subscriber.onCompleted();
            });
            printerEnterSerialNumberDialog.show(getFragmentManager(), PrinterEnterSerialNumberDialog.FRAGMENT_TAG);
        }));
    }

    /**
     * Показывает диалог о необходимости закрытия смены на принтере.
     *
     * @return
     */
    private Observable<Void> showDialogPrinterShiftShouldBeClosedObservable() {
        return Observable.create(subscriber -> deferredActionHandler.post(() -> {
            SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                    getString(R.string.settings_printer_msg_printer_shift_should_be_closed),
                    getString(R.string.settings_printer_msg_printer_shift_should_be_closed_yes),
                    getString(R.string.settings_printer_msg_printer_shift_should_be_closed_no),
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
        }));
    }

    /**
     * Показывает диалог для подтверждения времени на принтере.
     *
     * @return
     */
    private Observable<Void> showDialogPrinterTimeObservable(@NonNull Date printerDate) {
        return Observable.create(subscriber -> deferredActionHandler.post(() -> {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String strDate = simpleDateFormat.format(printerDate);
            SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                    getString(R.string.settings_printer_msg_printer_time, strDate),
                    getString(R.string.settings_printer_msg_printer_time_ok),
                    getString(R.string.settings_printer_msg_printer_time_cancel),
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
        }));
    }

    /**
     * Показывает уведомление, что активизация ЭКЛЗ не требуется.
     *
     * @return
     */
    private Observable<Void> showDialogEKLZActivationDoesNotRequiredObservable() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                deferredActionHandler.post(() -> {
                    SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                            getString(R.string.settings_printer_msg_eklz_activation_does_not_required),
                            getString(R.string.btnOk),
                            null,
                            LinearLayout.HORIZONTAL,
                            0);
                    simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
                    simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
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
     * Выполянет печать настроечной таблицы.
     */
    private void printAdjustingTable() {
        if (isInProgress) {
            return;
        }

        isInProgress = true;
        progressDialog.setMessage(getString(R.string.settings_printer_print_adjusting_table));
        progressDialog.show();

        Di.INSTANCE.printerManager().getOperationFactory().getPrintAdjustingTableOperation()
                .call()
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                        isInProgress = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        onFailed(e);
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        onSuccess(getString(R.string.settings_printer_msg_success));
                    }
                });
    }

    /**
     * Запускат процедуру активизации ЭКЛЗ.
     */
    private void activateEKLZ() {

        if (isInProgress) {
            return;
        }

        isInProgress = true;

        Observable
                .fromCallable(() -> {
                    progressDialog.setMessage(getString(R.string.settings_printer_msg_checking_eklz_state));
                    progressDialog.show();
                    return (Void) null;
                })
                .observeOn(SchedulersCPPK.background())
                .flatMap(aVoid -> Di.INSTANCE.printerManager().getOperationFactory().getGetStateOperation().call())
                .flatMap(result -> {
                    EklzChecker eklzChecker = di().printerManager().getEklzChecker();
                    boolean eklzActivationDoesNotRequired = eklzChecker.check(result.getEKLZNumber(), result.getRegNumber());
                    if (eklzActivationDoesNotRequired) {
                        return showDialogEKLZActivationDoesNotRequiredObservable();
                    } else {
                        return Observable.just(null);
                    }
                })
                .flatMap(aVoid -> {
                    if (ShiftManager.getInstance().isShiftOpened()) {
                        return showDialogShiftWillBeClosedObservable().flatMap(aVoid1 -> closeShiftIfNeed());
                    } else {
                        return Observable.just(null);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aVoid2 -> {
                    progressDialog.setMessage(getString(R.string.settings_printer_activate_eklz));
                    progressDialog.show();
                })
                .observeOn(SchedulersCPPK.printer())
                .flatMap(aVoid -> Di.INSTANCE.printerManager().getOperationFactory().getGetStateOperation().call())
                .flatMap(result -> {
                    if (result.isShiftOpened()) {
                        return showDialogPrinterShiftShouldBeClosedObservable()
                                .doOnNext(aVoid2 -> {
                                    progressDialog.setMessage(getString(R.string.settings_printer_closing_shift_at_printer));
                                    progressDialog.show();
                                })
                                .observeOn(SchedulersCPPK.printer())
                                .flatMap(aVoid -> {
                                    CloseShiftOperation.Params params = new CloseShiftOperation.Params(Di.INSTANCE.fiscalHeaderParamsBuilder().build(), false);
                                    return Di.INSTANCE.printerManager().getOperationFactory().getCloseShiftOperation(params).call();
                                })
                                .flatMap(result1 -> Observable.just(null))
                                .subscribeOn(AndroidSchedulers.mainThread());
                    } else {
                        return Observable.just(null);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aVoid2 -> {
                    progressDialog.setMessage(getString(R.string.settings_printer_activating_eklz));
                    progressDialog.show();
                })
                .observeOn(SchedulersCPPK.background())
                .flatMap(aVoid -> {
                    EKLZActivator eklzActivator = new EKLZActivator(getLocalDaoSession(), di().nsiVersionManager(), di().getShiftManager(), Dagger.appComponent().paperUsageCounter());
                    return eklzActivator.activateEKLZ(Di.INSTANCE.printerManager().getPrinter()).toObservable();
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                        isInProgress = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        onFailed(e);
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        setFlagEKLZActivated();
                        onSuccess(getString(R.string.settings_printer_msg_success));
                    }
                });
    }

    /**
     * Отображает экран, уведомляющий об успешном выполнении  операции на принтере.
     *
     * @param message
     */
    private void onSuccess(String message) {

        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_SUCCESS);

        stateBuilder.setTextMessage(message);
        stateBuilder.setButton1(R.string.btnOk, v -> simpleLseView.hide());
        stateBuilder.setButton2(null, null);

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    /**
     * Отображает экран, уведомляющий о неудачном выполнении  операции на принтере.
     *
     * @param e
     */
    private void onFailed(Throwable e) {
        Logger.error(TAG, e.getMessage(), e);
        progressDialog.dismiss();
        isInProgress = false;

        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);

        if (e instanceof PrinterIsNotConnectedException) {
            stateBuilder.setTextMessage(R.string.printer_not_found_msg);
        } else {
            stateBuilder.setTextMessage(R.string.settings_printer_msg_error);
        }
        stateBuilder.setButton1(R.string.sev_btn_cancel, v -> simpleLseView.hide());

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    /**
     * Устанавливает в результат запуска Activity флаг, что произведена активизация ЭКЛЗ.
     */
    private void setFlagEKLZActivated() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_IS_EKLZ_ACTIVATED, true);
        setResult(RESULT_OK, intent);
    }

    /**
     * Выполняет принудительное закрытие смены на ПТК при необходимости. Закрытие смены на ФР при этом не происходит
     *
     * @return
     */
    private Observable<Void> closeShiftIfNeed() {
        return Observable.fromCallable(() -> {

            if (!checkShiftOpenedInDbInteractor.isShiftOpened()) {
                return null;
            }

            closeShiftInDbInteractor.closeShift();

            if (!checkShiftOpenedInDbInteractor.isShiftOpened()) {
                shiftWasClosed = true;
            }

            return (Void) null;
        });
    }

}
