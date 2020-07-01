package ru.ppr.cppk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.helpers.DeferredActionHandler;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.printer.rx.operation.closeShift.CloseShiftOperation;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.BluetoothDeviceSearchActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.PrinterEnterSerialNumberDialog;
import ru.ppr.cppk.ui.dialog.PrinterModeChoiceDialog;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.logger.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import ru.ppr.ikkm.IPrinter;

public class PrinterBindingActivity extends SystemBarActivity {

    private static final String TAG = Logger.makeLogTag(PrinterBindingActivity.class);

    private static final int REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE = 101;

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, PrinterBindingActivity.class);
        return intent;
    }

    private final DeferredActionHandler deferredActionHandler = new DeferredActionHandler();
    private FeedbackProgressDialog progressDialog;
    private TextView macAddressView;
    private TextView serialNumberView;
    private Button skipBtn;
    private Integer tmpPrinterMode;
    private String tmpSerialNumber;
    boolean builtinSelected = false;

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_printer_binding);
        // см. http://agile.srvdev.ru/browse/CPPKPP-34713
        resetRegisterReceiver();

        denyScreenLock();

        // Считать модель устройства и, если это МКАССА со встроенным принтером, то выполнить что-то ...
        String PhoneModel = Di.INSTANCE.getDeviceModel();
        if (PhoneModel.equals("i9000S")) {
            View SelectBuiltPrinterBtn = findViewById(R.id.selectBuiltinDeviceBtn);
            SelectBuiltPrinterBtn.setVisibility(View.VISIBLE);
        }

        macAddressView = (TextView) findViewById(R.id.macAddress);
        serialNumberView = (TextView) findViewById(R.id.serialNumber);
        skipBtn = (Button) findViewById(R.id.skipBtn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        deferredActionHandler.resume();
        //спрячем кнопку пропустить для релизной сборки: https://aj.srvdev.ru/browse/CPPKPP-29271
        //skipBtn.setVisibility((BuildConfig.DEBUG) ? View.VISIBLE : View.GONE);
        //покажем кнопку пропустить для релизной сборки: http://agile.srvdev.ru/browse/CPPKPP-34110
        skipBtn.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        deferredActionHandler.pause();
        super.onPause();
    }

    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.applyBtn:
                applyMAC(false);
                break;

            case R.id.selectDeviceBtn:
                builtinSelected = false;
                navigateToBluetoothDeviceSearchActivity();
                break;

            case R.id.selectBuiltinDeviceBtn:
                if(!builtinSelected) {
                    android.device.PrinterManager printer = new android.device.PrinterManager();
                    printer.prn_open();
                    printer.prn_setupPage(384, -1);
                    int lStatus = printer.prn_getStatus();
                    if (lStatus == android.device.PrinterManager.PRNSTS_OK) {
                        Globals.getInstance().getToaster().showToast(R.string.printer_binding_msg_changing_success);
                        macAddressView.setText("00:00:00:00:00:00");
                        builtinSelected = true;
                        tmpPrinterMode = PrinterManager.PRINTER_MODE_BUILTIN;
//                        printer.prn_paperForWard(2);
                        tmpSerialNumber = StationDevice.STUB_SERIAL_NUMBER;
                        applyMAC(false);
                    } else
                        Globals.getInstance().getToaster().showToast(R.string.printer_binding_msg_changing_failed);

                    printer.prn_close();
                }
                break;

            case R.id.skipBtn:
                builtinSelected = false;
                skip();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE: {
                    ru.ppr.cppk.model.BluetoothDevice bluetoothDevice = data.getParcelableExtra(BluetoothDeviceSearchActivity.EXTRA_SELECTED_DEVICE);
                    macAddressView.setText(bluetoothDevice.getAddress());
                    tmpPrinterMode = null;

                    showChoicePrinterTypeObservable()
                            .flatMap(printerMode -> {
                                tmpPrinterMode = printerMode;

                                return showEnterPrinterSerialNumberDialogObservable();
                            })
                            .subscribe(serialNumber -> {
                                tmpSerialNumber = serialNumber;
                                serialNumberView.setText(tmpSerialNumber);
                            });
                    return;
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void navigateToBluetoothDeviceSearchActivity() {
        Navigator.navigateToBluetoothDeviceSearchActivity(
                this,
                null,
                REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE,
                null);
    }

    private void skip() {
        macAddressView.setText("00:00:00:00:00:00");
        tmpPrinterMode = PrinterManager.PRINTER_MODE_FILE;
        tmpSerialNumber = StationDevice.STUB_SERIAL_NUMBER;
        applyMAC(true);
    }

    private void applyMAC(boolean skip) {

        String macAddress = macAddressView.getText().toString();

        if ((isValidMAC(macAddress) && isValidPrinterMode(tmpPrinterMode)) || builtinSelected) {
            progressDialog = new FeedbackProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.show();

            Observable
                    .fromCallable(() -> {
                        Di.INSTANCE.printerManager().setTempMacAddressAndPrinterMode(macAddress, tmpPrinterMode);
                        return null;
                    })
                    .flatMap(aVoid -> Di.INSTANCE.printerManager().getOperationFactory().getGetStateOperation().call())
                    .flatMap(result -> {
                        Observable<Void> obs = Observable.create(subscriber -> {
                            subscriber.onNext(null);
                            subscriber.onCompleted();
                        });
                        if (result.isShiftOpened()) {
                            if (!skip) {
                                obs = showDialogPrinterTimeObservable(result.getOperationTime());
                            }
                            return obs
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
                        } else if (!skip) {
                            return showDialogPrinterTimeObservable(result.getOperationTime());
                        }
                        return obs;
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
                    .flatMap(aVoid -> Di.INSTANCE.printerManager().changePrinterMacAddressAndMode(macAddress, tmpPrinterMode).toObservable())
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
                    .subscribeOn(SchedulersCPPK.printer())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Void>() {
                        @Override
                        public void onCompleted() {
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.error(TAG, e);
                            progressDialog.dismiss();
                            Globals.getInstance().getToaster().showToast(R.string.printer_binding_msg_changing_failed);
                        }

                        @Override
                        public void onNext(Void aVoid) {
                            progressDialog.dismiss();
                            disablePeriferals();
                            Navigator.navigateToSplashActivity(PrinterBindingActivity.this, false);
                            finish();
                        }
                    });
        }
    }

    private void disablePeriferals() {
        Globals.getInstance().disablePeriferal();
        //bluetooth отключим отдельно из-за https://aj.srvdev.ru/browse/CPPKPP-27967
        Di.INSTANCE.bluetoothManager().disable(null);
    }

    private boolean isValidMAC(String mac) {
        boolean isValid = false;
        if (!TextUtils.isEmpty(mac)) {
            String macAddress = mac.toUpperCase();
            String macRegEx = "^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$";
            isValid = macAddress.matches(macRegEx);
        }
        if (!isValid) {
            Globals.getInstance().getToaster().showToast(R.string.printer_binding_invalid_MAC);
        }

        return isValid;
    }

    private boolean isValidPrinterMode(Integer printerMode) {
        boolean isValid = false;

        if (printerMode != null) {
            isValid = true;
        }

        if (!isValid) {
            di().getApp().getToaster().showToast(R.string.printer_binding_invalid_mode);
        }

        return isValid;
    }

    /**
     * Показывает диалог с выбором типа принтера
     *
     * @return
     */
    private Observable<Integer> showChoicePrinterTypeObservable() {
        return Observable.create(subscriber -> {
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
        });
    }

    /**
     * Показывает диалог с вводом серийного номера принтера
     */
    private Observable<String> showEnterPrinterSerialNumberDialogObservable() {
        return Observable.create(subscriber -> {
            PrinterEnterSerialNumberDialog printerEnterSerialNumberDialog = PrinterEnterSerialNumberDialog.newInstance();
            printerEnterSerialNumberDialog.setSerialNumberEnterListener((dialogFragment, serialNumber) -> {
                subscriber.onNext(serialNumber);
                subscriber.onCompleted();
            });
            printerEnterSerialNumberDialog.show(getFragmentManager(), PrinterEnterSerialNumberDialog.FRAGMENT_TAG);
        });
    }

    /**
     * Показывает диалог о необходимости закрытия смены на принтере.
     *
     * @return
     */
    private Observable<Void> showDialogPrinterShiftShouldBeClosedObservable() {
        return Observable.create(subscriber -> deferredActionHandler.post(() -> {
            SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                    getString(R.string.printer_binding_msg_printer_shift_should_be_closed),
                    getString(R.string.printer_binding_msg_printer_shift_should_be_closed_yes),
                    getString(R.string.printer_binding_msg_printer_shift_should_be_closed_no),
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
                    getString(R.string.printer_binding_msg_printer_time, strDate),
                    getString(R.string.printer_binding_msg_printer_time_ok),
                    getString(R.string.printer_binding_msg_printer_time_cancel),
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

    @Override
    public void onBackPressed() {

    }

}
