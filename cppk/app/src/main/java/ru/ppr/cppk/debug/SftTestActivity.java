package ru.ppr.cppk.debug;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import java.io.File;
import java.util.Date;
import java.util.EnumSet;

import ru.ppr.core.manager.eds.CheckSignResult;
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.export.Exchange;
import ru.ppr.cppk.helpers.EmergencyModeHelper;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.LoggedActivity;
import ru.ppr.edssft.LicType;
import ru.ppr.edssft.model.GetKeyInfoResult;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils;
import ru.ppr.utils.MD5Utils;
import ru.ppr.utils.MtpUtils;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;

public class SftTestActivity extends LoggedActivity implements OnClickListener {

    private Globals g;
    public static final String TAG = Logger.makeLogTag(SftTestActivity.class);
    private EditText userIdEt;
    private Handler handler;
    private EditText et;

    private FeedbackProgressDialog progressDialog;

    private Holder<PrivateSettings> privateSettingsHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();

        setContentView(R.layout.debug_test_sft);
        g = (Globals) getApplication();

        progressDialog = new FeedbackProgressDialog(this);
        progressDialog.setMessage("Подождите");
        progressDialog.setCancelable(false);

        userIdEt = (EditText) findViewById(R.id.userId);
        et = (EditText) findViewById(R.id.out);

        long userId = Di.INSTANCE.getPrivateSettings().get().getTerminalNumber();
        userIdEt.setText(String.valueOf(userId));

        findViewById(R.id.openProcessorNative).setOnClickListener(this);
        findViewById(R.id.getStateNative).setOnClickListener(this);
        findViewById(R.id.closeProcessorNative).setOnClickListener(this);

        findViewById(R.id.set).setOnClickListener(this);
        findViewById(R.id.testSig).setOnClickListener(this);
        findViewById(R.id.getKeyInfoBtn).setOnClickListener(this);
        findViewById(R.id.pull).setOnClickListener(this);
        findViewById(R.id.lastError).setOnClickListener(this);
        findViewById(R.id.getState).setOnClickListener(this);
        findViewById(R.id.updateFolders).setOnClickListener(this);
        findViewById(R.id.createCheckRequest).setOnClickListener(this);
        findViewById(R.id.createSellRequest).setOnClickListener(this);
        findViewById(R.id.createAllRequest).setOnClickListener(this);
        findViewById(R.id.takeLic).setOnClickListener(this);
        findViewById(R.id.clearLog).setOnClickListener(this);
        findViewById(R.id.pullTest).setOnClickListener(this);
        findViewById(R.id.ecpInitDisableBluetooth).setOnClickListener(this);
        findViewById(R.id.ecpInitEnableBluetooth).setOnClickListener(this);
        findViewById(R.id.close_sft).setOnClickListener(this);
        findViewById(R.id.takeAndPull).setOnClickListener(this);

        findViewById(R.id.breakSft).setOnClickListener(v -> breakSft());
    }

    private void breakSft() {
        Logger.trace(TAG, "breakSft called");
        Completable
                .fromAction(() -> {
                    File workingDir = Dagger.appComponent().edsManager().getEdsDirs().getEdsWorkingDir();
                    File iksDir = new File(workingDir, "iks");
                    File[] inIks = iksDir.listFiles();
                    if (inIks != null && inIks.length != 0) {
                        Logger.trace(TAG, "Files in iks:");
                        for (File file : inIks) {
                            Logger.trace(TAG, "File " + file.getName()
                                    + ", size = " + file.length()
                                    + ", changed = " + new Date(file.lastModified()));
                        }
                    } else {
                        Logger.trace(TAG, "Files in iks: no any file");
                    }

                    Dagger.appComponent().edsManagerWrapper().pingEdsBlocking();
                    EmergencyModeHelper.startEmergencyMode(new Throwable("SFT breaking test"));
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    @Override
    protected void onResume() {
        handler = new Handler();
        super.onResume();
    }

    @Override
    public void onClick(View v) {

        int userId = 0;
        try {
            userId = Integer.parseInt(userIdEt.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (userId < 1)
            return;

        switch (v.getId()) {
            case R.id.set:
                // rez = "" + g.getEcpChecker().setUserId(userId);
                break;
            case R.id.testSig:
                testSig();
                break;
            case R.id.getKeyInfoBtn:
                getKeyInfoBtnPress();
                break;
            case R.id.pull:
                startPull();
                break;
            case R.id.lastError:
                // rez = "lastError rez: " + g.getEcpChecker().getLastError();
                break;
            case R.id.getState:
                Observable
                        .fromCallable(() -> Di.INSTANCE.getEdsManager().getState())
                        .observeOn(SchedulersCPPK.eds())
                        .subscribeOn(SchedulersCPPK.eds())
                        .subscribe(state -> addLogToEditText("onStateReturned: " + state), error -> Logger.trace(TAG, error));
                break;

            case R.id.openProcessorNative:
                // rez = "openProcessorNative: " +
                // g.getEcpChecker().openProcessor(workingString, transportString);
                break;
            case R.id.getStateNative:
                // rez = "getStateNative: " + g.getEcpChecker().getState();
                break;
            case R.id.closeProcessorNative:
                // rez = "closeProcessorNative: " +
                // g.getEcpChecker().closeProcessorJava();
                break;

            case R.id.updateFolders:
                FileUtils.updateFolderMtp(g, Exchange.SFT);
                break;

            case R.id.createCheckRequest:
                createRequest(EnumSet.of(LicType.CHECK));
                break;

            case R.id.createSellRequest:
                createRequest(EnumSet.of(LicType.SELL));
                break;

            case R.id.createAllRequest:
                createRequest(EnumSet.of(LicType.CHECK, LicType.SELL));
                break;

            case R.id.takeLic:
                takeLic();
                break;

            case R.id.clearLog:
                et.setText("");
                break;

            case R.id.pullTest:
                startActivity(new Intent(SftTestActivity.this, PullSftTestActivity.class));
                break;

            case R.id.ecpInitEnableBluetooth:
                initEcpWithEnableBluetooth();
                break;

            case R.id.ecpInitDisableBluetooth:
                initEcpWithDisableBluetooth();
                break;

            case R.id.close_sft:
                closeSft();
                break;

            case R.id.takeAndPull:
                takeAndPull();
                break;

            default:
                break;
        }

    }

    private void takeAndPull() {

        progressDialog.show();

        Single
                .fromCallable(() -> {
                    // копируем файлы из папки in во временную папку
                    final File folderWithKeys = new File(Di.INSTANCE.getEdsManager().getEdsDirs().getEdsTransportInDir().getAbsolutePath());
                    final File cacheDir = new File(getCacheDir().getPath() + File.separator + "tmp_in");
                    if (!cacheDir.exists()) {
                        cacheDir.mkdir();
                    }

                    final File[] files = folderWithKeys.listFiles();
                    if (files == null) throw new IllegalStateException("File is not dir");

                    Log.d(TAG, "Копируем ключи во временную папку");

                    String tmpDir = cacheDir.getAbsolutePath() + File.separator;
                    for (File file : files) {
                        final File dst = new File(tmpDir + file.getName());
                        FileUtils.copyWithOutMtp(file, dst);
                    }

                    Log.d(TAG, "Скопированного файлов - " + files.length);

                    // очищаем папку in
                    for (File file : files) {
                        file.delete();
                    }

                    // копируем из временной папки, в папку in

                    Logger.debug(TAG, "Восстанавливаем ключи");

                    String dstKeyDir = folderWithKeys.getAbsolutePath() + File.separator;
                    final File[] cacheKeys = cacheDir.listFiles();
                    for (File cacheKey : cacheKeys) {
                        final File dst = new File(dstKeyDir + cacheKey.getName());
                        FileUtils.copyWithOutMtp(cacheKey, dst);
                    }

                    Logger.debug(TAG, "Ключей восстановленно - " + cacheKeys.length);
                    Logger.debug(TAG, "Take lic");

                    Di.INSTANCE.getEdsManager().takeLicenses(new File(Exchange.SFT_LIC));

                    return null;
                })
                .flatMap(o -> Single.fromCallable(() -> {
                    Log.d(TAG, "Pull");
                    Di.INSTANCE.getEdsManager().pullEdsChecker();

                    return null;
                }))
                .flatMap(o -> Single.fromCallable(() -> Di.INSTANCE.getEdsManager().getState()))
                .subscribeOn(SchedulersCPPK.eds())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(state -> {
                            addLogToEditText("onStateReturned: " + state);
                            progressDialog.hide();
                        },
                        error -> {
                            addLogToEditText(error.getMessage());
                            progressDialog.hide();
                        });
    }

    private void closeSft() {
        Single
                .fromCallable(() -> Di.INSTANCE.getEdsManager().close())
                .subscribeOn(SchedulersCPPK.eds())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> addLogToEditText("Close done: " + String.valueOf(result)), error -> Logger.error(TAG, error));
    }

    /**
     * Инициация события перезапуска SFT
     */
    private void startPull() {
        addLogToEditText("очищаем папку out и запускаем функцию pull, подождите...");
        Observable
                .fromCallable(() -> {
                    final String lineSeparator = System.getProperty("line.separator");
                    final EdsManager edsManager = Di.INSTANCE.getEdsManager();
                    final File outDir = edsManager.getEdsDirs().getEdsTransportOutDir();
                    final boolean result = edsManager.pullEdsChecker();
                    final StringBuilder sb = new StringBuilder("startPull: " + result);
                    sb.append(lineSeparator).append("Содержимое папки out: ").append(lineSeparator);

                    final File[] files = outDir.listFiles();

                    if (files != null) {
                        for (File file : files) {
                            sb.append(file).append(lineSeparator);
                        }
                    }

                    return sb.toString();
                })
                .subscribeOn(SchedulersCPPK.eds())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::addLogToEditText, error -> Logger.error(TAG, error));
    }

    /**
     * Создает файл запроса лицензии определенного типа
     */
    private void createRequest(@NonNull final EnumSet<LicType> licTypes) {
        Completable
                .fromAction(() -> Di.INSTANCE.getEdsManager().createLicRequest(licTypes))
                .subscribeOn(SchedulersCPPK.eds())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                            MtpUtils.refreshDir(g, Di.INSTANCE.getEdsManager().getEdsDirs().getEdsUtilDstDir().getParentFile());
                            addLogToEditText("createRequest: complete");
                        },
                        error -> {
                            Logger.error(TAG, error);
                            addLogToEditText("Error create request lic file");
                        }
                );
    }

    private void takeLic() {
        Completable
                .fromAction(() -> Di.INSTANCE.getEdsManager().takeLicenses(new File(Exchange.SFT_LIC)))
                .subscribeOn(SchedulersCPPK.eds())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> addLogToEditText("Take command done."), error -> Logger.error(TAG, error));
    }

    private void getKeyInfoBtnPress() {
        getKeyInfo(3221225477L);
    }

    private void testSig() {
        final byte[] data = new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xFF};

        Observable
                .fromCallable(() -> Di.INSTANCE.getEdsManager().signData(data, new Date()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(SchedulersCPPK.eds())
                .subscribe(signDataResult -> {
                            final String lineSeparator = System.getProperty("line.separator");
                            addLogToEditText("onSignFinished: "
                                    + "data=" + MD5Utils.convertHashToString(data) + lineSeparator
                                    + "rez=" + signDataResult.isSuccessful() + lineSeparator
                                    + "sign=" + MD5Utils.convertHashToString(signDataResult.getSignature()) + lineSeparator
                                    + "numberEcp=" + signDataResult.getEdsKeyNumber() + lineSeparator
                            );

                            getKeyInfo(signDataResult.getEdsKeyNumber())
                                    .subscribe(keyInfoResult -> {
                                                addLogToEditText(keyInfoResult.toString());

                                                testVerify(signDataResult.getData(), signDataResult.getSignature(), signDataResult.getEdsKeyNumber())
                                                        .subscribe(checkSignResult -> {
                                                            addLogToEditText("onSignChecked: "
                                                                    + "data=" + MD5Utils.convertHashToString(data) + lineSeparator
                                                                    + "sign=" + MD5Utils.convertHashToString(signDataResult.getSignature()) + lineSeparator
                                                                    + "numberEcp=" + signDataResult.getEdsKeyNumber() + lineSeparator
                                                                    + "checkSignResult=" + checkSignResult + lineSeparator
                                                            );
                                                        });
                                            },
                                            error -> Logger.error(TAG, error)
                                    );
                        },
                        error -> Logger.error(TAG, error)
                );

//				byte[] data1 = CommonUtils.hexStringToByteArray("05b000002cd5b854805e030000000000000005b100012cd5b854885e03000000df7ce10b000000000000000000000000");
//				byte[] sign1 =  CommonUtils.hexStringToByteArray("6d83e42ed83aeeee4b23c5f8fe0d808e81080668f0df025d8f4ae6ef1de663c549b9967a037a250ce92a245a5646f05985dfb8d825b3558787301efac11203c3");
//				numberEcp = -1073741740;
//				testVerify(data1,sign1,numberEcp);

    }

    @NonNull
    private Observable<GetKeyInfoResult> getKeyInfo(final long keyNumber) {
        return Observable
                .fromCallable(() -> Di.INSTANCE.getEdsManager().getKeyInfo(keyNumber))
                .subscribeOn(SchedulersCPPK.eds())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @NonNull
    private Observable<CheckSignResult> testVerify(final byte[] data, final byte[] sign, final long keyNumber) {
        return Observable
                .fromCallable(() -> Di.INSTANCE.getEdsManager().verifySign(data, sign, keyNumber))
                .subscribeOn(SchedulersCPPK.eds())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void addLogToEditText(final String text) {
        Logger.info(TAG, text);
        handler.post(() -> et.setText(text + "\n-----------------------\n" + et.getText()));
    }

    private void initEcpWithEnableBluetooth() {
        progressDialog.show();

        Di.INSTANCE.bluetoothManager().enable(enabled -> {
            if (enabled) {
                Observable
                        .fromCallable(() -> Di.INSTANCE.getEdsManager().getState())
                        .subscribeOn(SchedulersCPPK.eds())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(getStateResult -> {
                                    addLogToEditText("Инициализация с включеным блютуз - " + getStateResult.isSuccessful());
                                    progressDialog.hide();
                                },
                                error -> {
                                    Logger.error(TAG, error);
                                    progressDialog.hide();
                                }
                        );
            }
        });
    }

    private void initEcpWithDisableBluetooth() {

        progressDialog.show();

        Di.INSTANCE.bluetoothManager().disable(enabled -> {
            if (!enabled) {
                Observable
                        .fromCallable(() -> Di.INSTANCE.getEdsManager().getState())
                        .subscribeOn(SchedulersCPPK.eds())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(getStateResult -> {
                                    addLogToEditText("Инициализация с выключенным блютуз - " + getStateResult.isSuccessful());
                                    progressDialog.hide();
                                },
                                error -> {
                                    Logger.error(TAG, error);
                                    progressDialog.hide();
                                }
                        );
            }
        });
    }

}
