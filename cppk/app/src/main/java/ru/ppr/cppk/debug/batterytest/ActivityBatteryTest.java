package ru.ppr.cppk.debug.batterytest;

import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ru.ppr.cppk.PathsConstants;
import ru.ppr.cppk.R;
import ru.ppr.cppk.debug.batterytest.core.ExecutionResult;
import ru.ppr.cppk.debug.batterytest.core.Task;
import ru.ppr.cppk.debug.batterytest.core.TaskManager;
import ru.ppr.cppk.debug.batterytest.core.TasksConfig;
import ru.ppr.cppk.debug.batterytest.impl.TaskBarcodeRead;
import ru.ppr.cppk.debug.batterytest.impl.TaskCpuLoad;
import ru.ppr.cppk.debug.batterytest.impl.TaskScreenDisable;
import ru.ppr.cppk.debug.batterytest.impl.TaskScreenEnable;
import ru.ppr.cppk.debug.batterytest.impl.TaskSmartCardRead;
import ru.ppr.cppk.debug.batterytest.impl.TaskWait;
import ru.ppr.cppk.systembar.LoggedActivity;

/**
 * Created by nevolin on 11.07.2016.
 */
public class ActivityBatteryTest extends LoggedActivity {

    private interface NextStepOfPreparing {
        void wrappedCode();
    }

    private static final String ARG_TASK_CONFIG = "ARG_TASK_CONFIG";

    private FeedbackProgressDialog preparing;
    private Button action;
    private TextView prepared;
    private View need_prepare;
    private TextView prepare_these;
    private View test_last_result_holder;
    private TextView test_last_result;

    private boolean canBack;
    private List<Integer> preparingErrors;
    private NextStepOfPreparing afterDeviceAdminStep;

    private Gson gson;
    private Timer dataCheckTimer;
    private TimerTask dataCheckTask;

    private TasksConfig tasksConfig;

    public static Intent getCallingIntent(Context context, TasksConfig tasksConfig) {
        return new Intent(context, ActivityBatteryTest.class).putExtra(ARG_TASK_CONFIG, tasksConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_battery_test);

        if(getIntent() == null) {
            finish();

            return;
        }

        tasksConfig = getIntent().getParcelableExtra(ARG_TASK_CONFIG);

        if(tasksConfig == null) {
            finish();

            return;
        }

        canBack = false;

        gson = new Gson();

        dataCheckTimer = new Timer();

        preparing = new FeedbackProgressDialog(this);
        preparing.setMessage(getString(R.string.battery_test_still_preparing));
        preparing.setCancelable(false);
        preparing.show();

        prepared = (TextView) findViewById(R.id.prepared);

        need_prepare = findViewById(R.id.need_prepare);

        prepare_these = (TextView) findViewById(R.id.prepare_these);

        test_last_result_holder = findViewById(R.id.test_last_result_holder);
        test_last_result_holder.setVisibility(View.GONE);

        test_last_result = (TextView) findViewById(R.id.test_last_result);

        action = (Button) findViewById(R.id.action);
        action.setOnClickListener(v -> {
            if(preparingErrors.size() == 0) {
                if(TaskManager.INSTANCE.stillExecuting()) {
                    TaskManager.INSTANCE.terminate();

                    action.setEnabled(false);
                } else {
                    runTest();
                }
            } else {
                prepareManual();
            }
        });

        showLastExecutionResult();

        prepareAuto();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(isDeviceAdminEnabled() != null)
            preparingErrors.add(R.string.battery_test_need_prepare_device_admin);

        if(afterDeviceAdminStep != null) {
            afterDeviceAdminStep.wrappedCode();
            afterDeviceAdminStep = null;
        }
    }

    @Override
    public void onBackPressed() {
        if(canBack)
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        dataCheckTimer.cancel();
    }

    private void prepareBluetooth(NextStepOfPreparing nextStepOfPreparing) {
        if(nextStepOfPreparing != null)
            nextStepOfPreparing.wrappedCode();
        //https://aj.srvdev.ru/browse/CPPKPP-27161 - временно решили просто закомментировать и включать вручную.
//        WirelessNetworksManager.Bluetooth.enable(this, new WirelessNetworksManager.StateListener() {
//            @Override
//            public void onChanged() {
//                if(nextStepOfPreparing != null)
//                    nextStepOfPreparing.wrappedCode();
//            }
//
//            @Override
//            public void onFailed() {
//                preparingErrors.add(R.string.battery_test_need_prepare_bluetooth);
//
//                if(nextStepOfPreparing != null)
//                    nextStepOfPreparing.wrappedCode();
//            }
//        });
    }

    private ComponentName isDeviceAdminEnabled() {
        final ComponentName deviceAdminReceiver = new ComponentName(getApplicationContext(), DeviceAdminReceiver.class);

        return ((DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE)).isAdminActive(deviceAdminReceiver) ? null : deviceAdminReceiver;
    }

    private void prepareDeviceAdmin(NextStepOfPreparing nextStepOfPreparing) {
        final ComponentName deviceAdminReceiver = isDeviceAdminEnabled();

        if(deviceAdminReceiver != null) {
            afterDeviceAdminStep = nextStepOfPreparing;

            startActivityForResult(new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminReceiver), 0);
        }
    }

    /**
     * Запускает автоматическую подготовку системы для выполнения тестов.
     * Если не вызвать перед выполнением тестов, что-нибудь может пойти не так.
     */
    private void prepareAuto() {
        initPreparingResult();
        //убираем локскрин, при включении экрана во время теста он не нужен
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        prepareBluetooth(() -> runOnUiThread(() -> {
            if(isDeviceAdminEnabled() != null) {
                preparingErrors.add(R.string.battery_test_need_prepare_device_admin);
            }

            showPreparingResult();
        }));
    }

    /**
     * Запускает ручную подготовку системы для выполнения тестов.
     * Если не вызвать перед выполнением тестов, что-нибудь может пойти не так.
     */
    private void prepareManual() {
        preparing.show();

        initPreparingResult();
        //убираем локскрин, при включении экрана во время теста он не нужен
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        prepareBluetooth(() -> prepareDeviceAdmin(() -> runOnUiThread(this::showPreparingResult)));
    }

    private void initPreparingResult() {
        preparingErrors = new ArrayList<>();

        prepared.setVisibility(View.GONE);

        need_prepare.setVisibility(View.GONE);

        action.setEnabled(false);
        action.setVisibility(View.GONE);
    }

    private void showPreparingResult() {
        if(preparingErrors.size() == 0) {
            prepared.setVisibility(View.VISIBLE);
            prepared.setText(R.string.battery_test_prepared);

            action.setEnabled(true);
            action.setVisibility(View.VISIBLE);
            action.setText(R.string.battery_test_action_run_test);
        } else {
            need_prepare.setVisibility(View.VISIBLE);

            String errors = "";

            for(int error : preparingErrors)
                errors += getString(error);

            prepare_these.setText(errors);

            action.setEnabled(true);
            action.setVisibility(View.VISIBLE);
            action.setText(R.string.battery_test_action_prepare);
        }

        preparing.dismiss();

        canBack = true;
    }

    /**
     * Запускает тест аккумулятора
     */
    private void runTest() {
        canBack = false;

        action.setEnabled(false);
        action.setVisibility(View.VISIBLE);

        prepared.setVisibility(View.VISIBLE);
        prepared.setText(R.string.battery_test_still_running);

        need_prepare.setVisibility(View.GONE);

        List<Task.Builder> tasksBuilders = new ArrayList<>();

        for(int totalCycleLoop = 0; totalCycleLoop < tasksConfig.getTotalCycleLoops(); totalCycleLoop++) {
            tasksBuilders.add(new TaskScreenEnable.Builder().setContext(getApplicationContext()));

            for(int awakenedCycleLoop = 0; awakenedCycleLoop < tasksConfig.getAwakenedCycleLoops(); awakenedCycleLoop++) {
                tasksBuilders.add(new TaskSmartCardRead.Builder().setMillis(tasksConfig.getSmartCardTimeoutMillis()));
                tasksBuilders.add(new TaskCpuLoad.Builder().setMillis(tasksConfig.getCpuLoadMillis()));
                tasksBuilders.add(new TaskWait.Builder().setMillis(tasksConfig.getWaitMillis()));
                tasksBuilders.add(new TaskBarcodeRead.Builder().setMillis(tasksConfig.getBarcodeTimeoutMillis()));
                tasksBuilders.add(new TaskCpuLoad.Builder().setMillis(tasksConfig.getCpuLoadMillis()));
                tasksBuilders.add(new TaskWait.Builder().setMillis(tasksConfig.getWaitMillis()));
            }

            tasksBuilders.add(new TaskScreenDisable.Builder().setContext(getApplicationContext()));
            tasksBuilders.add(new TaskWait.Builder().setMillis(tasksConfig.getAwakenedLoopEndSleepMillis()));
        }

        tasksBuilders.add(new TaskScreenEnable.Builder().setContext(getApplicationContext()));

        final double currentBatteryLevel = getCurrentBatteryLevel();
        final long currentMillis = System.currentTimeMillis();

        ExecutionResult executionResult = new ExecutionResult();
        executionResult.setMillisAtStart(currentMillis);
        executionResult.setMillisAtEnd(currentMillis);
        executionResult.setBatteryAtStart(currentBatteryLevel);
        executionResult.setBatteryAtEnd(currentBatteryLevel);

        saveExecutionResult(executionResult);

        dataCheckTimer.schedule(dataCheckTask = new TimerTask() {
            @Override
            public void run() {
                if(this == dataCheckTask) {
                    ExecutionResult lastExecutionResult = getLastExecutionResult();

                    final long millisAtStart = lastExecutionResult == null ? System.currentTimeMillis() : lastExecutionResult.getMillisAtStart();
                    final double batteryAtStart = lastExecutionResult == null ? getCurrentBatteryLevel() : lastExecutionResult.getBatteryAtStart();

                    if (lastExecutionResult == null)
                        lastExecutionResult = new ExecutionResult();

                    lastExecutionResult.setMillisAtStart(millisAtStart);
                    lastExecutionResult.setMillisAtEnd(System.currentTimeMillis());
                    lastExecutionResult.setBatteryAtStart(batteryAtStart);
                    lastExecutionResult.setBatteryAtEnd(getCurrentBatteryLevel());

                    saveExecutionResult(lastExecutionResult);
                    runOnUiThread(() -> showLastExecutionResult());
                }
            }
        }, 1000 * 60, 1000 * 60);

        action.setEnabled(true);
        action.setText(R.string.battery_test_action_end_test);

        TaskManager.INSTANCE.set(tasksBuilders);
        TaskManager.INSTANCE.execute(() -> runOnUiThread(() -> {
            prepared.setVisibility(View.VISIBLE);
            prepared.setText(R.string.battery_test_prepared);

            dataCheckTask.cancel();

            ExecutionResult lastExecutionResult = getLastExecutionResult();

            final long millisAtStart = lastExecutionResult == null ? System.currentTimeMillis() : lastExecutionResult.getMillisAtStart();
            final double batteryAtStart = lastExecutionResult == null ? getCurrentBatteryLevel() : lastExecutionResult.getBatteryAtStart();

            if (lastExecutionResult == null)
                lastExecutionResult = new ExecutionResult();

            lastExecutionResult.setMillisAtStart(millisAtStart);
            lastExecutionResult.setMillisAtEnd(System.currentTimeMillis());
            lastExecutionResult.setBatteryAtStart(batteryAtStart);
            lastExecutionResult.setBatteryAtEnd(getCurrentBatteryLevel());

            saveExecutionResult(lastExecutionResult);

            showLastExecutionResult();

            action.setEnabled(true);
            action.setVisibility(View.VISIBLE);
            action.setText(R.string.battery_test_action_run_test);

            canBack = true;
        }));
    }

    private void showLastExecutionResult() {
        ExecutionResult executionResult = getLastExecutionResult();

        if(executionResult == null) {
            test_last_result_holder.setVisibility(View.GONE);
        } else {
            long executionTimeSecs = (executionResult.getMillisAtEnd() - executionResult.getMillisAtStart()) / 1000;

            String executionTime;

            //если меньше минуты
            if(executionTimeSecs < 60) {
                executionTime = executionTimeSecs + "s";
            } //если меньше часа
            else if(executionTimeSecs < 60 * 60) {
                long mins = executionTimeSecs / 60;
                long secs = executionTimeSecs % 60;

                executionTime = mins + "m " + secs + "s";
            } else {
                long hours = (executionTimeSecs / 60) / 60;
                long mins = (executionTimeSecs / 60) % 60;
                long secs = executionTimeSecs % 60;

                executionTime = hours + "h " + mins + "m " + secs + "s";
            }

            test_last_result_holder.setVisibility(View.VISIBLE);
            test_last_result.setText(getString(R.string.battery_test_last_result_data,
                    executionTime,
                    Double.valueOf(executionResult.getBatteryAtStart() - executionResult.getBatteryAtEnd()).intValue()));
        }
    }

    private double getCurrentBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int level = batteryIntent == null ? -1 : batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent == null ? -1 : batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if(level == -1 || scale == -1) {
            return 50;
        }

        return ((double) level / (double) scale) * 100;
    }

    private ExecutionResult getLastExecutionResult() {
        File log = new File(PathsConstants.LOG_BATTERY_TEST + File.separator + "log.json");

        if(log.exists()) {
            try {
                return gson.fromJson(new FileReader(log), ExecutionResult.class);
            } catch (FileNotFoundException exception) {
                return null;
            }
        }

        return null;
    }

    private void saveExecutionResult(ExecutionResult executionResult) {
        File logDir = new File(PathsConstants.LOG_BATTERY_TEST);

        if(!logDir.exists()) {
            logDir.mkdirs();
        }

        File log = new File(PathsConstants.LOG_BATTERY_TEST + File.separator + "log.json");

        if(log.exists()) {
            log.delete();
        }

        try {
            if(!log.exists()) {
                log.createNewFile();
            }

            if(log.exists()) {
                FileWriter fileWriter = new FileWriter(log, true);
                fileWriter.write(gson.toJson(executionResult));
                fileWriter.flush();
                fileWriter.close();
            }
        } catch (IOException exception) {
            if(log.exists()) {
                log.delete();
            }
        }
    }

}
