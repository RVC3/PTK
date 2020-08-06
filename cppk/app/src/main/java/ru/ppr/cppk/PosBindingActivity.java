package ru.ppr.cppk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.pos.PosType;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.BluetoothDeviceSearchActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.PosTypeChoiceDialog;
import ru.ppr.logger.Logger;
import rx.Completable;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

// ========================
import org.lanter.lan4gate.*;
import org.lanter.lan4gate.Messages.Fields.ResponseFieldsList;
import org.lanter.lan4gate.Messages.OperationsList;

public class PosBindingActivity extends SystemBarActivity {

    /**
     * Время ожидания коннекта со стороны финансового ПО, мс
     */
    private static final long POS_TERMINAL_CONNECT_TIME = 4000;
    /**
     * Подписка на процесс поиска карты
     */
    private Subscription waitPosSubscription = null;

    class ResponseListener implements IResponseCallback {
        @Override
        public void newResponseMessage(IResponse response, Lan4Gate initiator) {
            for (ResponseFieldsList field : response.getCurrentFieldsList()) {
                // Код для обработки каждого поля
                switch (field.getString()){
                    case "Status":
                        initiator.stop();
                        if (response.getStatus().getNumber() == 1){
                            Logger.trace("LAN4ResponseListener", "Status = 1" );
                            PosBindingActivity.isRunning = false;
                            skip();
/*                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Navigator.navigateToSplashActivity(PosBindingActivity.this, false);
                            finish();*/
                        }
                        break;
                }
            }
        }
    }

    class CommunicationListener implements ICommunicationCallback {
        @Override
        public void communicationStarted(Lan4Gate initiator) {
            //код для обработки запуска соединения
        }

        @Override
        public void communicationStopped(Lan4Gate initiator) {
            //код для обработки остановки соединения
        }

        @Override
        public void connected(Lan4Gate initiator) {
            //Код для обработки подключения. После данного события можно отправлять запросы
            Logger.trace("LAN4CommunicationListener", "Connect with LAN4Tap is established" );
            sale = gate.getPreparedRequest(OperationsList.TestCommunication);
            gate.sendRequest(sale);
        }

        @Override
        public void disconnected(Lan4Gate initiator) {
            // код для обработки отключения
        }
    }

    private Lan4Gate gate;
    private ResponseListener responseListener;
    private CommunicationListener communicationListener;
    IRequest sale;
    public static boolean isRunning = false;

    private static final String TAG = Logger.makeLogTag(PosBindingActivity.class);

    private static final int REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE = 101;

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, PosBindingActivity.class);
        return intent;
    }

    private TextView macAddressView;
    private TextView portView;

    private volatile boolean isInProgress = false;

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_pos_binding);
        // см. http://agile.srvdev.ru/browse/CPPKPP-34713
        resetRegisterReceiver();

        denyScreenLock();

        // Считать модель устройства и, если это МКАССА со встроенным принтером, то выполнить что-то ...
        String PhoneModel = Di.INSTANCE.getDeviceModel();
        if (PhoneModel.equals("i9000S")) {
            View SelectBuiltPosBtn = findViewById(R.id.selectBuiltDeviceBtn);
            SelectBuiltPosBtn.setVisibility(View.VISIBLE);
        }

        macAddressView = (TextView) findViewById(R.id.macAddress);
        portView = (TextView) findViewById(R.id.port);
        portView.setText(String.valueOf(SharedPreferencesUtils.getPosPort(this)));
    }


// Если нет входящего соединения от LAN4Tap, отправить implicit intent для определения, запущено ли оно на устройстве
    void send_implicit_activity_to_Lanter(){
        Uri address = Uri.parse("http://developer.alexanderklimov.ru");
        Intent openLinkIntent = new Intent(Intent.ACTION_VIEW, address);

        if (openLinkIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(openLinkIntent);
        } else {
            Globals.getInstance().getToaster().showToast(R.string.pos_binding_msg_changing_failed);
        }
    }

    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.applyBtn:
                applyMAC();
                break;

            case R.id.selectDeviceBtn:
                navigateToBluetoothDeviceSearchActivity();
                break;

            case R.id.selectBuiltDeviceBtn:
                responseListener = new ResponseListener();
                //Создание слушателя для состояния сетевого взаимодействия
                communicationListener = new CommunicationListener();

                int ecrNumber = 1;
                gate = new Lan4Gate(ecrNumber);
                //Добавление обратных вызовов в список слушателей
                gate.addResponseCallback(responseListener);
                gate.addCommunicationCallback(communicationListener);
                //Установка порта взаимодействия
                gate.setPort(20501);
                this.isRunning = true;
                waitPosSubscription = Completable
                        .fromAction(() -> gate.start())
                        .observeOn(SchedulersCPPK.posTerminal())
                        .andThen(Completable.fromAction(() -> {
                            while (this.isRunning) ;
                                }))
                        .timeout(POS_TERMINAL_CONNECT_TIME, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                                    Logger.trace("subscribe", "Терминал привязан" );
//                                    Globals.getInstance().getToaster().showToast(R.string.pos_binding_msg_changing_success);
                                    waitPosSubscription = null;
                                },
                                error -> {
                                    Logger.error(TAG, error);
                                    waitPosSubscription = null;
//                                    Globals.getInstance().getToaster().showToast(R.string.pos_binding_msg_changing_failed);
//                                    send_implicit_activity_to_Lanter();
                                }
                        );
                break;

            case R.id.skipBtn:
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

        if (isInProgress) {
            return;
        }

        isInProgress = true;
        Observable
                .fromCallable((Callable<Void>) () -> {
                    //запретим возможность использования если пропустили https://aj.srvdev.ru/browse/CPPKPP-32582
                    Globals.getInstance().getPrivateSettingsHolder().get().setIsPosEnabled(false);
                    Dagger.appComponent().privateSettingsRepository().savePrivateSettings(Globals.getInstance().getPrivateSettingsHolder().get());
                    SharedPreferencesUtils.setPosMacAddress(getApplicationContext(), "");
                    return null;
                })
                .flatMap(aVoid -> Globals.updatePosTerminal(
                        SharedPreferencesUtils.getPosMacAddress(getApplicationContext()),
                        SharedPreferencesUtils.getPosPort(getApplicationContext()),
                        SharedPreferencesUtils.getPosTerminalType(getApplicationContext())
                ))
                .subscribeOn(SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                    disablePeriferals();
                    Navigator.navigateToSplashActivity(PosBindingActivity.this, false);
                    finish();
                    isInProgress = false;
                }, throwable -> {
                    Logger.trace(TAG, throwable);
                    isInProgress = false;
                    Globals.getInstance().getToaster().showToast(R.string.error);
                });

    }

    private void applyMAC() {

        if (isInProgress) {
            return;
        }

        String macAddress = macAddressView.getText().toString();
        int port = Integer.valueOf(portView.getText().toString());

        if (isValidMAC(macAddress) && isValidPort(port)) {
            isInProgress = true;

            showChoicePosTypeObservable()
                    .flatMap(posType -> {
                        if (!isValidPosType(posType)) {
                            throw new IllegalStateException("isValidPosType(posType) == false, posType = " + posType);
                        }

                        return Observable.just(posType);
                    })
                    .flatMap(posType -> {
                        SharedPreferencesUtils.setPosMacAddress(PosBindingActivity.this, macAddress);
                        SharedPreferencesUtils.setPosPort(PosBindingActivity.this, port);
                        SharedPreferencesUtils.setPosTerminalType(this, posType);

                        return Observable.just(null);
                    })
                    .flatMap(aVoid -> Globals.updatePosTerminal(
                            SharedPreferencesUtils.getPosMacAddress(getApplicationContext()),
                            SharedPreferencesUtils.getPosPort(getApplicationContext()),
                            SharedPreferencesUtils.getPosTerminalType(getApplicationContext())
                    ))
                    .subscribeOn(SchedulersCPPK.background())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aVoid -> {
                        disablePeriferals();
                        Navigator.navigateToSplashActivity(this, false);
                        finish();
                        isInProgress = false;
                    }, throwable -> {
                        Logger.trace(TAG, throwable);
                        isInProgress = false;
                        Globals.getInstance().getToaster().showToast(R.string.error);
                    });
        }
    }

    private void disablePeriferals() {
        Globals.getInstance().disablePeriferal();
        //bluetooth отключим отдельно из-за https://aj.srvdev.ru/browse/CPPKPP-27967
        Di.INSTANCE.bluetoothManager().disable(null);
    }

    private boolean isValidMAC(String macAddress) {
        String macRegEx = "^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$";
        boolean isValid = macAddress.matches(macRegEx);

        if (!isValid)
            Globals.getInstance().getToaster().showToast(R.string.printer_binding_invalid_MAC);

        return macAddress.matches(macRegEx);
    }

    private boolean isValidPort(int port) {
        boolean isValid = port > 1 && port <= 65535;

        if (!isValid)
            Globals.getInstance().getToaster().showToast(R.string.pos_binding_invalid_port);

        return isValid;
    }

    private boolean isValidPosType(PosType posType) {
        boolean isValid = false;

        if (posType == PosType.INGENICO || posType == PosType.INPAS) {
            isValid = true;
        }

        if (!isValid) {
            di().getApp().getToaster().showToast(R.string.printer_binding_invalid_mode);
        }

        return isValid;
    }

    /**
     * Показывает диалог с выбором типа POS-терминала
     *
     * @return
     */
    private Observable<PosType> showChoicePosTypeObservable() {
        return Observable.create(subscriber -> {
            PosTypeChoiceDialog posTypeChoiceDialog = PosTypeChoiceDialog.newInstance();
            posTypeChoiceDialog.setItemClickListener((dialogFragment, posType) -> {
                subscriber.onNext(posType);
                subscriber.onCompleted();
            });
            posTypeChoiceDialog.setBackClickListener(() -> {
                subscriber.onCompleted();
                posTypeChoiceDialog.dismiss();
            });
            posTypeChoiceDialog.show(getFragmentManager(), PosTypeChoiceDialog.FRAGMENT_TAG);
        });
    }

    @Override
    public void onBackPressed() {

    }

}
