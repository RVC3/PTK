package ru.ppr.cppk.debug;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Callable;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.logic.DocumentTestPd;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.model.BluetoothDevice;
import ru.ppr.cppk.printer.rx.operation.PrinterGetShiftsInfo;
import ru.ppr.cppk.printer.rx.operation.openShift.OpenShiftOperation;
import ru.ppr.cppk.printer.rx.operation.printZReport.PrintZReportOperation;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.LoggedActivity;
import ru.ppr.cppk.ui.activity.BluetoothDeviceSearchActivity;
import ru.ppr.cppk.ui.activity.MoebiusTestActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class PrinterDebugActivity extends LoggedActivity implements OnClickListener {

    private static final String TAG = Logger.makeLogTag(PrinterDebugActivity.class);

    private static final int REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE = 101;

    // private static final String COMMAND_FOR_BARCODE =
    // "!0 200 200 210 1\r\nB PDF-417 10 20 XD 3 YD 12 C 3 S 2\r\nPDF DATA\r\nABCDE12345\r\nENDPDF T 4 0 10 120 PDF Data\r\nT 4 0 10 170 ABCDE12345\r\nFORM\r\nPRINT\r\n";
    // private static final String COMMAND_FOR_BARCODE =
    // "!500 200 200 210 1\r\nTEXT 520 0 30 40 Hello World\r\nFORM\r\nPRINT\r\n";

    private static final String COMMAND_FOR_BARCODE = "! 300 200 200 110 1/nB PDF-417 10 1 XD 3 YD 12 C 3 S 2/nPDF DATA/nABCDE12345/nENDPDF T 4 0 10 120 PDF Data/nT 4 0 10 170 ABCDE12345/nFORM/nPRINT/n";
    private static final String pdData = "014c000024590055c03606000000460000c05c";
    private static final String pdDataBug = "011000014ca18154805e03000000290000c0704e";
    private static final String pdData2 = "011000014ca18154805e03000000290000c0704e";
    private static final String pdData3 = "50444620444154410d0a011e001065a453544b0d0d0300000013000000";
    private static final String pdData4 = "78797a7B7C7D7F8081828384";

    private Globals g;
    private EditText macEt, outET;
    private TextView dateFromTW, dateToTW;
    private EditText shiftFromET, shiftToET;

    private Date getShiftInfoDateFrom = new Date();
    private Date getShiftInfoDateTo = new Date();
    private int getShiftInfoShiftFrom = 0;
    private int getShiftInfoShiftTo = 1;

    private Holder<PrivateSettings> privateSettingsHolder;

    private FeedbackProgressDialog progressDialog;

    private String getConfigLabel(String str) {

        // String dataPdString = Opos.decode(pdData);

        Logger.info("BARCODE", pdData);

        StringBuilder print = new StringBuilder("! 300 200 200 80 1\r\n");
        print.append("B PDF-417 10 1 XD 2 YD 18 C 3 S 2\r\n");
        print.append(pdData).append("\r\n");
        print.append("ENDPDF\r\n");
        print.append("FORM\r\n");
        print.append("PRINT\r\n");

        return print.toString();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();

        progressDialog = new FeedbackProgressDialog(this);
        setContentView(R.layout.debug_test_zebra);
        g = (Globals) getApplication();

        macEt = (EditText) findViewById(R.id.macAddress);
        outET = (EditText) findViewById(R.id.out);
        dateFromTW = (TextView) findViewById(R.id.dateFrom);
        dateToTW = (TextView) findViewById(R.id.dateTo);
        shiftFromET = (EditText) findViewById(R.id.shiftFrom);
        shiftToET = (EditText) findViewById(R.id.shiftTo);

        findViewById(R.id.openBluetoothSystemSettings).setOnClickListener(this);
        findViewById(R.id.startSearchActivity).setOnClickListener(this);
        findViewById(R.id.printProbniyPd).setOnClickListener(this);
        findViewById(R.id.printBarcode).setOnClickListener(this);
        findViewById(R.id.getDate).setOnClickListener(this);
        findViewById(R.id.printText).setOnClickListener(this);
        findViewById(R.id.disconntFromPrinter).setOnClickListener(this);
        findViewById(R.id.connectToPrinter).setOnClickListener(this);
        findViewById(R.id.connectFromUiThread).setOnClickListener(this);
        findViewById(R.id.settingsBarcode).setOnClickListener(this);
        findViewById(R.id.transferShift).setOnClickListener(this);
        findViewById(R.id.debugPrinterOfdSettings).setOnClickListener(this);
        findViewById(R.id.openShift).setOnClickListener(this);
        findViewById(R.id.closeShift).setOnClickListener(this);
        findViewById(R.id.clearLog).setOnClickListener(this);
        findViewById(R.id.stressTests).setOnClickListener(this);
        findViewById(R.id.getShiftInfoBtn).setOnClickListener(this);
        dateFromTW.setOnClickListener(this);
        dateToTW.setOnClickListener(this);
        macEt.setText(Di.INSTANCE.printerManager().getPrinterMacAddress());

    }

    @Override
    protected void onResume() {
        super.onResume();
        outET.setText("");
    }

    private void showProgress() {
        progressDialog.show();
    }

    private void hideProgress() {
        progressDialog.dismiss();
    }

    public void printBarcode() {

        byte[] barcodeData = CommonUtils.hexStringToByteArray(pdDataBug);

        showProgress();

        Di.INSTANCE.printerManager().getOperationFactory().getPrintBarcodeOperation(barcodeData)
                .call()
                //Т.к. нас не интересует результат выполнения печати штрихкода, то проглотим ошибку
                .onErrorReturn(throwable -> null)
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                    hideProgress();
                }, throwable -> {
                    addLog(throwable.getMessage());
                    hideProgress();
                });
    }

    public void getDate() {

        showProgress();

        Di.INSTANCE.printerManager().getOperationFactory().getGetDateOperation().call()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(date -> {
                    addLog("Date: " + date);
                })
                .observeOn(SchedulersCPPK.printer())
                .flatMap(date -> Di.INSTANCE.printerManager().getOperationFactory().getGetOdometerValue().call())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(result -> {
                    addLog("OdometerValue: " + result.getOdometerValue());
                })
                .observeOn(SchedulersCPPK.printer())
                .flatMap(result -> Di.INSTANCE.printerManager().getOperationFactory().getGetStateOperation().call())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(result -> {
                    addLog("isShiftOpened: " + result.isShiftOpened());
                    addLog("EKLZNumber: " + result.getEKLZNumber());
                    addLog("FNSerial: " + result.getFNSerial());
                    addLog("ShiftNum: " + result.getShiftNum());
                    addLog("LastSPND: " + result.getSPND());
                    addLog("Model: " + result.getModel());
                    addLog("CashInFR: " + result.getCashInFR());
                })
                .observeOn(SchedulersCPPK.printer())
                .flatMap(result -> Di.INSTANCE.printerManager().getOperationFactory().getOfdSettingsOperation().call())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(result -> {
                    addLog(result.toString());
                })
                .subscribeOn(SchedulersCPPK.printer())
                .subscribe(result -> {
                    hideProgress();
                }, throwable -> {
                    addLog(throwable.getMessage());
                    hideProgress();
                });
    }

    public void getShiftInfo() {
        int shiftFrom = 0;
        int shiftTo = 0;
        try {
            shiftFrom = Integer.parseInt(shiftFromET.getText().toString());
        } catch (NumberFormatException nfe) {
            addLog("Could not parse " + nfe);
        }
        try {
            shiftTo = Integer.parseInt(shiftToET.getText().toString());
        } catch (NumberFormatException nfe) {
            addLog("Could not parse " + nfe);
        }


        PrinterGetShiftsInfo.Params params = new PrinterGetShiftsInfo.Params();
        params.startShiftNum = shiftFrom;
        params.endShiftNum = shiftTo;

        showProgress();

        Di.INSTANCE.printerManager().getOperationFactory().getGetShiftsInfo().setParams(params).call()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(closedShiftInfos -> {
                    addLog("ShiftInfo: " + ((closedShiftInfos.size() > 0) ? closedShiftInfos.size() : "null"));
                    addLog("--------------");
                    addLog("shiftTo: " + params.endShiftNum);
                    addLog("shiftFrom: " + params.startShiftNum);
                })
                .subscribeOn(SchedulersCPPK.printer())
                .subscribe(result -> {
                    hideProgress();
                }, throwable -> {
                    addLog(throwable.getMessage());
                    hideProgress();
                });
    }

    private void printText(String text) {

        showProgress();

        Di.INSTANCE.printerManager().getOperationFactory().getPrintLinesOperation()
                .setTextLines(Collections.singletonList(text))
                .setAddSpaceAtTheEnd(true)
                .call()
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                    hideProgress();
                }, throwable -> {
                    addLog(throwable.getMessage());
                    hideProgress();
                });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.printBarcode:
                printBarcode();
                break;

            case R.id.printProbniyPd:
                printTestPD();
                break;

            case R.id.dateFrom:
                DialogFragment newFragment1 = new DatePickerFragment(DateType.FROM);
                newFragment1.show(getFragmentManager(), "Дата начала");
                break;

            case R.id.dateTo:
                DialogFragment newFragment2 = new DatePickerFragment(DateType.TO);
                newFragment2.show(getFragmentManager(), "Дата конца");
                break;

            case R.id.getShiftInfoBtn:
                getShiftInfo();
                break;

            case R.id.startSearchActivity:
                Navigator.navigateToBluetoothDeviceSearchActivity(this, null, REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE, Di.INSTANCE.printerManager().getBluetoothDevice());
                break;

            case R.id.getDate:
                getDate();
                break;

            case R.id.printText:
                printText("русский english 1234567890");
                break;

            case R.id.disconntFromPrinter:
                Single
                        .fromCallable((Callable<Void>) () -> {
                            Di.INSTANCE.printerManager().getPrinter().prepareResources();
                            Di.INSTANCE.printerManager().getPrinter().disconnect();
                            return null;
                        })
                        .doAfterTerminate(() -> {
                            try {
                                Di.INSTANCE.printerManager().getPrinter().freeResources();
                            } catch (PrinterException e) {
                                addLog(e.getMessage());
                            }
                        })
                        .subscribeOn(SchedulersCPPK.printer())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleSubscriber<Void>() {
                            @Override
                            public void onSuccess(Void value) {
                                addLog("onSuccess");
                            }

                            @Override
                            public void onError(Throwable error) {
                                addLog(error.getMessage());
                            }
                        });
                break;

            case R.id.connectToPrinter:
                Single
                        .fromCallable((Callable<Void>) () -> {
                            Di.INSTANCE.printerManager().getPrinter().prepareResources();
                            Di.INSTANCE.printerManager().getPrinter().connect();
                            return null;
                        })
                        .doAfterTerminate(() -> {
                            try {
                                Di.INSTANCE.printerManager().getPrinter().freeResources();
                            } catch (PrinterException e) {
                                addLog(e.getMessage());
                            }
                        })
                        .subscribeOn(SchedulersCPPK.printer())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleSubscriber<Void>() {
                            @Override
                            public void onSuccess(Void value) {
                                addLog("onSuccess");
                            }

                            @Override
                            public void onError(Throwable error) {
                                addLog(error.getMessage());
                            }
                        });
                break;

            case R.id.openBluetoothSystemSettings:
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentOpenBluetoothSettings);
                break;

            case R.id.connectFromUiThread:
                break;

            case R.id.settingsBarcode:
                startActivity(new Intent(getApplicationContext(), BarcodeParametrsActivity.class));
                break;

            case R.id.transferShift:
                transferShift();
                break;

            case R.id.openShift:
                openShift();
                break;

            case R.id.closeShift:
                closeShift();
                break;

            case R.id.clearLog:
                outET.setText("");
                break;

            case R.id.stressTests:
                startActivity(new Intent(getApplicationContext(), MoebiusTestActivity.class));
                break;

            case R.id.debugPrinterOfdSettings:
                Navigator.navigateToOfdSettingsActivity(PrinterDebugActivity.this);
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
                    BluetoothDevice bluetoothDevice = data.getParcelableExtra(BluetoothDeviceSearchActivity.EXTRA_SELECTED_DEVICE);
                    FeedbackProgressDialog progressDialog = new FeedbackProgressDialog(this);
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    Di.INSTANCE.printerManager()
                            .changePrinterMacAddress(bluetoothDevice.getAddress())
                            .subscribeOn(SchedulersCPPK.background())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleSubscriber<Void>() {
                                @Override
                                public void onSuccess(Void value) {
                                    progressDialog.dismiss();
                                    macEt.setText(Di.INSTANCE.printerManager().getPrinterMacAddress());
                                    Globals.getInstance().getToaster().showToast(R.string.printer_binding_msg_changing_success);
                                }

                                @Override
                                public void onError(Throwable error) {
                                    Logger.error(TAG, error);
                                    progressDialog.dismiss();
                                    Globals.getInstance().getToaster().showToast(R.string.printer_binding_msg_changing_failed);
                                }
                            });
                    break;
                }
                default: {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void closeShift() {
        FeedbackProgressDialog progressDialog = new FeedbackProgressDialog(PrinterDebugActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.shift_close_progress_printing_z_report));
        progressDialog.show();

        PrintZReportOperation.Params params = new PrintZReportOperation.Params(false);
        Di.INSTANCE.printerManager().getOperationFactory().getPrintZReportOperation(params)
                .call()
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintZReportOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        addLog("Смена закрыта успешно!");
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        addLog("Не удалось закрыть смену: " + e.getMessage());
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onNext(PrintZReportOperation.Result result) {
                        progressDialog.dismiss();
                    }
                });
    }

    private void openShift() {

        FeedbackProgressDialog progressDialog = new FeedbackProgressDialog(PrinterDebugActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.shift_open_progress_opening_shift));
        progressDialog.show();

        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(Observable
                        .create((Observable.OnSubscribe<OpenShiftOperation.Params>) subscriber -> {
                            OpenShiftOperation.Params params = new OpenShiftOperation.Params();

                            params.userName = Di.INSTANCE.getUserSessionInfo().getCurrentUser().getName().trim();
                            params.userId = 1;

                            params.headerParams = Di.INSTANCE.fiscalHeaderParamsBuilder().build();

                            subscriber.onNext(params);

                        })
                        .flatMap(params -> Di.INSTANCE.printerManager().getOperationFactory().getOpenShiftOperation(params).call())
                )
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<OpenShiftOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                        addLog("Открытие смены завершено успешно!");
                    }

                    @Override
                    public void onError(Throwable e) {
                        addLog("Открытие смены завершилось с ошибкой: " + e.getMessage());
                        progressDialog.dismiss();

                        if (e instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                            Navigator.navigateToActivityTicketTapeIsNotSet(PrinterDebugActivity.this);
                        }
                    }

                    @Override
                    public void onNext(OpenShiftOperation.Result result) {
                        addLog("сработал onNext()!");
                        progressDialog.dismiss();
                    }
                });

    }

    private void printTestPD() {
        FeedbackProgressDialog progressDialog = new FeedbackProgressDialog(PrinterDebugActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.shift_open_progress_printing_test_pd));
        progressDialog.show();

        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(Dagger.appComponent().fiscalDocStateSynchronizer().rxSyncCheckState())
                .flatMapCompletable(aBoolean -> Dagger.appComponent().printTestCheckInteractor().print())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DocumentTestPd>() {
                               @Override
                               public void onCompleted() {
                                   progressDialog.dismiss();
                                   addLog("Печать тестового ПД успешно завершена!");
                               }

                               @Override
                               public void onError(Throwable e) {
                                   addLog("Печать тестового ПД завершилась ошибкой: " + e.getMessage());
                                   progressDialog.dismiss();

                                   if (e instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                                       Navigator.navigateToActivityTicketTapeIsNotSet(PrinterDebugActivity.this);
                                   }
                               }

                               @Override
                               public void onNext(DocumentTestPd documentTestPd) {
                                   progressDialog.dismiss();
                               }
                           }

                );
    }

    private void transferShift() {
        try {
            Di.INSTANCE.printerManager().getPrinter().prepareResources();
            Di.INSTANCE.printerManager().getPrinter().connect();
            Di.INSTANCE.printerManager().getPrinter().setCashier(1, "Петров П.П.");
            addLog("success");
        } catch (Exception e) {
            addLog(e.getMessage());
        } finally {
            try {
                Di.INSTANCE.printerManager().getPrinter().freeResources();
            } catch (PrinterException e) {
                addLog(e.getMessage());
            }
        }
    }

    private void addLog(String text) {
        outET.setText(text + "\n" + outET.getText());
        Logger.info(PrinterDebugActivity.class.getSimpleName(), text == null ? "" : text);
    }


    public enum DateType {
        FROM, TO;
    }

    @SuppressLint("ValidFragment")
    private class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {


        private DateType dateType;

        public DatePickerFragment(DateType dateType) {
            this.dateType = dateType;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            if (dateType == DateType.FROM) {
                dateFromTW.setText(DateFormatOperations.getDate(calendar.getTime()));
                getShiftInfoDateFrom = calendar.getTime();
            } else {
                dateToTW.setText(DateFormatOperations.getDate(calendar.getTime()));
                getShiftInfoDateTo = calendar.getTime();
            }
        }
    }

}
