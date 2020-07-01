package ru.ppr.cppk.debug;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.io.File;
import java.util.Locale;

import ru.ppr.core.domain.model.EdsType;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.managers.FileCleaner;
import ru.ppr.cppk.managers.db.LocalDbManager;
import ru.ppr.cppk.managers.db.NsiDbManager;
import ru.ppr.cppk.managers.db.SecurityDbManager;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.LoggedActivity;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.ikkm.file.db.PrinterSQLiteHelper;
import ru.ppr.logger.Logger;
import rx.Completable;
import rx.CompletableSubscriber;
import rx.Subscription;

/**
 * Класс представляющий экран в root меню для работы с базами данных.
 */
public class DebugBackupActivity extends LoggedActivity implements View.OnClickListener {

    private static final String TAG = Logger.makeLogTag(DebugBackupActivity.class);

    private static final int ATTENTION_DIALOG_ID = 1;
    private static final int COMPLETED_DIALOG_ID = 2;
    private static final String ATTENTION_DIALOG_TAG = "ATTENTION_DIALOG_TAG";
    private static final String COMPLETED_DIALOG_TAG = "COMPLETED_DIALOG_TAG";

    private Spinner maxBackupFilesCountSpinner;

    private ShiftManager shiftManager;
    private FileCleaner fileCleaner;
    private FilePathProvider filePathProvider;

    /**
     * Тип резервной копии.
     */
    public enum BackupType {
        Nsi,
        Security,
        Local,
        Full,
        Logs,
        Infotecs,
        Printer
    }

    /**
     * Действие.
     */
    public enum ActionType {
        BACKUP,
        RESTORE,
        REPLACE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_backup_activity);

        shiftManager = Dagger.appComponent().shiftManager();
        filePathProvider = Dagger.appComponent().filePathProvider();
        fileCleaner = Dagger.appComponent().fileCleaner();

        //создание бекапа
        findViewById(R.id.backupLocalDb).setOnClickListener(this);
        findViewById(R.id.backupPrinterDb).setOnClickListener(this);
        findViewById(R.id.backupSecurityDb).setOnClickListener(this);
        findViewById(R.id.backupRdsDb).setOnClickListener(this);
        findViewById(R.id.backupLogs).setOnClickListener(this);
        findViewById(R.id.backupInfotecs).setOnClickListener(this);
        findViewById(R.id.backupAll).setOnClickListener(this);

        //восстановление бекапа
        findViewById(R.id.restoreLocalDb).setOnClickListener(this);
        findViewById(R.id.restorePrinterDb).setOnClickListener(this);
        findViewById(R.id.restoreSecurityDb).setOnClickListener(this);
        findViewById(R.id.restoreRdsDb).setOnClickListener(this);
        findViewById(R.id.restoreBackupAll).setOnClickListener(this);
        findViewById(R.id.restoreBackupInfotecs).setOnClickListener(this);

        //подмена файлов баз данных
        findViewById(R.id.replaceLocalDb).setOnClickListener(this);
        findViewById(R.id.replaceSecurityDb).setOnClickListener(this);
        findViewById(R.id.replaceRdsDb).setOnClickListener(this);

        //очистка БД
        findViewById(R.id.eraseLocal).setOnClickListener(this);
        findViewById(R.id.erasePrinter).setOnClickListener(this);
        findViewById(R.id.eraseRDS).setOnClickListener(this);
        findViewById(R.id.eraseSecurity).setOnClickListener(this);

        //настройки
        maxBackupFilesCountSpinner = (Spinner) findViewById(R.id.maxBackupFilesCountSpinner);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //http://agile.srvdev.ru/browse/CPPKPP-42724

        //https://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-wit
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    protected void onResume() {
        super.onResume();
        configMaxFilesCountBlock();
    }

    private void configMaxFilesCountBlock() {
        Logger.trace(TAG, "configMaxFilesCountBlock");
        maxBackupFilesCountSpinner.setSelection(SharedPreferencesUtils.getMaxFileCountInBackupDir(getApplicationContext()) - 1);

        maxBackupFilesCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferencesUtils.setMaxFileCountInBackupDir(getApplicationContext(), position + 1);
                fileCleaner.clearDir(filePathProvider.getBackupsDir(), position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        String message = "Вы действительно хотите\n";

        switch (v.getId()) {
            case R.id.eraseLocal:
                message += "очистить\nЛокальную БД?";
                break;
            case R.id.erasePrinter:
                message += "очистить\nФайлового принтера БД?";
                break;
            case R.id.eraseRDS:
                message += "очистить НСИ?";
                break;
            case R.id.eraseSecurity:
                message += "очистить\nБазу Безопасности?";
                break;
            case R.id.backupPrinterDb:
                message += "создать бекап\nФайлового принтера БД?";
                break;
            case R.id.restorePrinterDb:
                message += "восстановить бекап\nФайлового принтера БД?";
                break;
            case R.id.backupLocalDb:
                message += "создать бекап\nЛокальной БД?";
                break;
            case R.id.restoreLocalDb:
                message += "восстановить бекап\nЛокальной БД?";
                break;
            case R.id.backupSecurityDb:
                message += "создать бекап\nБазы Безопасности?";
                break;
            case R.id.restoreSecurityDb:
                message += "восстановить бекап\nБазы Безопасности?";
                break;
            case R.id.backupRdsDb:
                message += "создать бекап НСИ?";
                break;
            case R.id.restoreRdsDb:
                message += "восстановить бекап\nНСИ?";
                break;
            case R.id.backupAll:
                message += "создать\nПолный бекап?";
                break;
            case R.id.restoreBackupAll:
                message += "восстановить\nПолный бекап?";
                break;
            case R.id.backupInfotecs:
                message += "создать бекап\nБазопасного билета?";
                break;
            case R.id.restoreBackupInfotecs:
                message += "восстановить бекап\nБазопасного билета?";
                break;
            case R.id.backupLogs:
                message += "создать бекап\nЛогов?";
                break;
            case R.id.replaceLocalDb:
                message += "подменить\nЛокальную БД?";
                break;
            case R.id.replaceSecurityDb:
                message += "подменить\nБазу Безопасности?";
                break;
            case R.id.replaceRdsDb:
                message += "подменить\nНСИ?";
                break;
        }

        SimpleDialog attentionDialog = SimpleDialog.newInstance(null, message, "Да", "Нет", LinearLayout.HORIZONTAL, ATTENTION_DIALOG_ID);
        attentionDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> onClickImpl(v));
        attentionDialog.setCancelable(false);
        attentionDialog.show(getFragmentManager(), ATTENTION_DIALOG_TAG);
    }

    private void onClickImpl(View v) {
        Logger.trace(TAG, "onClickImpl()");

        OnDoneListener onDoneListener = (msg) -> {
            SimpleDialog completedDialog = SimpleDialog.newInstance(null, msg, "Ок", null, LinearLayout.HORIZONTAL, COMPLETED_DIALOG_ID);
            completedDialog.setCancelable(false);
            completedDialog.show(getFragmentManager(), COMPLETED_DIALOG_TAG);
        };

        switch (v.getId()) {

            case R.id.eraseLocal:
                Logger.trace(TAG, "case R.id.eraseLocal");
                Di.INSTANCE.localDbManager().closeConnection();
                deleteDb(getDatabasePath(LocalDbManager.DB_NAME));
                Globals.getInstance().resetLocalDaoSession();
                // Проверяем факт обновления ПО и выполняем необходимые действия
                Dagger.appComponent().appVersionUpdateRegister().checkForUpdate();
                Di.INSTANCE.getShiftManager().refreshState();
                //обновим конфиг принтера, т.к. при выполнении миграции производятся манипуляции с SharedPreferences
                Di.INSTANCE.printerManager().updateConfig();
                break;

            case R.id.erasePrinter:
                Logger.trace(TAG, "case R.id.erasePrinter");
                try {
                    Di.INSTANCE.printerManager().getPrinter().disconnect();
                } catch (PrinterException e) {
                    Logger.error(TAG, e);
                }
                deleteDb(getDatabasePath(PrinterSQLiteHelper.DB_NAME));
                break;

            case R.id.eraseRDS:
                Logger.trace(TAG, "case R.id.eraseRDS");
                Di.INSTANCE.nsiDbManager().closeConnection();
                deleteDb(getDatabasePath(NsiDbManager.DB_NAME));
                Globals.getInstance().resetNsiDaoSession();
                break;

            case R.id.eraseSecurity:
                Logger.trace(TAG, "case R.id.eraseSecurity");
                Di.INSTANCE.securityDbManager().closeConnection();
                deleteDb(getDatabasePath(SecurityDbManager.DB_NAME));
                Globals.getInstance().resetSecurityDaoSession();
                break;

            case R.id.backupPrinterDb:
                Logger.trace(TAG, "case R.id.backupPrinterDb");
                startBackupRestoreProgress(BackupType.Printer, false, onDoneListener);
                break;

            case R.id.restorePrinterDb:
                Logger.trace(TAG, "case R.id.setPrinterDb");
                startBackupRestoreProgress(BackupType.Printer, true, onDoneListener);
                break;

            case R.id.backupLocalDb:
                Logger.trace(TAG, "case R.id.backupLocalDb");
                startBackupRestoreProgress(BackupType.Local, false, onDoneListener);
                break;

            case R.id.restoreLocalDb:
                Logger.trace(TAG, "case R.id.setLocalDb");
                startBackupRestoreProgress(BackupType.Local, true, onDoneListener);
                break;

            case R.id.backupSecurityDb:
                Logger.trace(TAG, "case R.id.backupSecurityDb");
                startBackupRestoreProgress(BackupType.Security, false, onDoneListener);
                break;

            case R.id.restoreSecurityDb:
                Logger.trace(TAG, "case R.id.setSecurityDb");
                startBackupRestoreProgress(BackupType.Security, true, onDoneListener);
                break;

            case R.id.backupRdsDb:
                Logger.trace(TAG, "case R.id.backupRdsDb");
                startBackupRestoreProgress(BackupType.Nsi, false, onDoneListener);
                break;

            case R.id.restoreRdsDb:
                Logger.trace(TAG, "case R.id.setRdsDb");
                startBackupRestoreProgress(BackupType.Nsi, true, onDoneListener);
                break;

            case R.id.backupAll:
                Logger.trace(TAG, "case R.id.backupAll");
                startBackupRestoreProgress(BackupType.Full, false, onDoneListener);
                break;
            case R.id.restoreBackupAll:
                Logger.trace(TAG, "case R.id.restoreAll");
                startBackupRestoreProgress(BackupType.Full, true, onDoneListener);
                break;

            case R.id.backupInfotecs:
                Logger.trace(TAG, "case R.id.backupInfotecs");
                startBackupRestoreProgress(BackupType.Infotecs, false, onDoneListener);
                break;
            case R.id.restoreBackupInfotecs:
                Logger.trace(TAG, "case R.id.restoreInfotecs");
                startBackupRestoreProgress(BackupType.Infotecs, true, onDoneListener);
                break;

            case R.id.backupLogs:
                Logger.trace(TAG, "case R.id.backupLogs");
                startBackupRestoreProgress(BackupType.Logs, false, onDoneListener);
                break;

            case R.id.replaceLocalDb:
                Logger.trace(TAG, "case R.id.replaceLocalDb");
                startReplaceFileProgress(BackupType.Local, onDoneListener);
                break;

            case R.id.replaceSecurityDb:
                Logger.trace(TAG, "case R.id.replaceSecurityDb");
                startReplaceFileProgress(BackupType.Security, onDoneListener);
                break;

            case R.id.replaceRdsDb:
                Logger.trace(TAG, "case R.id.replaceRdsDb");
                startReplaceFileProgress(BackupType.Nsi, onDoneListener);
                break;
        }
    }

    /**
     * Метод для инициализации действия с резервной копией.
     *
     * @param type      тип резервной копии.
     * @param isRestore флаг восставноления резервной копии.
     */
    private void startBackupRestoreProgress(@NonNull final BackupType type, final boolean isRestore, @NonNull OnDoneListener onDoneListener) {
        Logger.trace(TAG, "startBackupRestoreProgress(typeBd=" + type.toString() + ", isRestore=" + isRestore + ")");
        final DebugBackupActivity.BackupWorker backupWorker = new DebugBackupActivity.BackupWorker(onDoneListener);
        backupWorker.actionType = isRestore ? ActionType.RESTORE : ActionType.BACKUP;
        backupWorker.type = type;
        backupWorker.executeOnExecutor(SchedulersCPPK.backgroundExecutor());
    }

    /**
     * Метод для инициализации замены файла.
     *
     * @param type тип резервной копии.
     */
    private void startReplaceFileProgress(@NonNull final BackupType type, @NonNull OnDoneListener onDoneListener) {
        Logger.trace(TAG, "startReplaceFileProgress(typeBd=" + type.toString() + ")");
        final DebugBackupActivity.BackupWorker backupWorker = new DebugBackupActivity.BackupWorker(onDoneListener);
        backupWorker.actionType = ActionType.REPLACE;
        backupWorker.type = type;
        backupWorker.executeOnExecutor(SchedulersCPPK.backgroundExecutor());
    }

    /**
     * Метод для создания резервной копии.
     *
     * @param type тип резервной копии.
     * @return результат выполнения.
     */
    private Pair<Boolean, String> makeBackup(@NonNull final BackupType type) {
        Logger.trace(TAG, "makeBackup(type=" + type + ")");
        Pair<Boolean, String> result = null;


        switch (type) {
            case Full: {
                result = Dagger.appComponent().fullBackupCreator().start();
            }
            break;

            case Infotecs: {
                result = Dagger.appComponent().sftBackupCreator().start();
            }
            break;

            case Logs: {
                result = Dagger.appComponent().logBackupCreator().start();
            }
            break;

            case Local: {
                result = Dagger.appComponent().localDbBackupCreator().start();
            }
            break;

            case Security: {
                result = Dagger.appComponent().securityBackupCreator().start();
            }
            break;

            case Nsi: {
                result = Dagger.appComponent().nsiBackupCreator().start();
            }
            break;

            case Printer: {
                result = Dagger.appComponent().printerDbBackupCreator().start();
            }
            break;
        }

        fileCleaner.clearDir(filePathProvider.getBackupsDir(), SharedPreferencesUtils.getMaxFileCountInBackupDir(DebugBackupActivity.this));

        return result;
    }

    /**
     * Метод для восстановления резервной копии.
     *
     * @param type тип резервной копии.
     * @return результат выполнения.
     */
    private Pair<Boolean, String> restoreBackup(@NonNull final BackupType type) {
        Pair<Boolean, String> result = null;
        Logger.trace(TAG, "restoreBackup(type=" + type + ")");

        switch (type) {
            case Full: {
                result = Dagger.appComponent().fullBackupRestorer().start();
            }
            break;

            case Infotecs: {
                result = Dagger.appComponent().sftBackupRestorer().start();
            }
            break;

            case Local: {
                result = Dagger.appComponent().localDbBackupRestorer().start();
            }
            break;

            case Security: {
                result = Dagger.appComponent().securityBackupRestorer().start();
            }
            break;

            case Nsi: {
                result = Dagger.appComponent().nsiBackupRestorer().start();
            }
            break;

            case Printer: {
                result = Dagger.appComponent().printerDbBackupRestorer().start();
            }
            break;
        }

        return result;
    }

    private Pair<Boolean, String> replaceFile(@NonNull final BackupType type) {
        Pair<Boolean, String> result = null;
        Logger.trace(TAG, "replaceFile(type=" + type + ")");

        File dbDir = filePathProvider.getBackupsReplaceDir();
        switch (type) {
            case Local: {
                result = Dagger.appComponent().localDbBackupRestorer().replace(dbDir);
            }
            break;

            case Security: {
                result = Dagger.appComponent().securityBackupRestorer().replace(dbDir);
            }
            break;

            case Nsi: {
                result = Dagger.appComponent().nsiBackupRestorer().replace(dbDir);
            }
            break;
        }

        return result;
    }

    /**
     * Метод для удаления базы данных.
     *
     * @param db путь к файлу базы данных.
     */
    private void deleteDb(@NonNull final File db) {
        Logger.trace(TAG, "deleteDb(db=" + db.getPath() + ")");

        if (db.exists())
            db.delete();
    }

    /**
     * Класс для выполнения работ по созданию или восстановлению резервной копии.
     */
    private class BackupWorker extends AsyncTask<Void, Void, Pair<Boolean, String>> {

        public ActionType actionType = null;
        private FeedbackProgressDialog progress = null;
        public BackupType type = BackupType.Full;
        private OnDoneListener onDoneListener;

        BackupWorker(@NonNull OnDoneListener onDoneListener) {
            this.onDoneListener = onDoneListener;
        }

        @Override
        protected void onPreExecute() {
            Logger.trace(TAG, "BackupWorker.onPreExecute() START");
            super.onPreExecute();
            progress = new FeedbackProgressDialog(DebugBackupActivity.this);
            switch (actionType) {
                case BACKUP:
                    Logger.info(TAG, "BackupWorker.onPreExecute() Запускаем создание бекапа " + type.toString());
                    progress.setTitle("Бекапим " + type.toString());
                    break;
                case RESTORE:
                    Logger.info(TAG, "BackupWorker.onPreExecute() Запускаем восстановление бекапа " + type.toString());
                    progress.setTitle("Восстанавливаем бекап " + type.toString());
                    break;
                case REPLACE:
                    Logger.info(TAG, "BackupWorker.onPreExecute() Запускаем замену файла " + type.toString());
                    progress.setTitle("Заменяем файл " + type.toString());
                    break;
                default:
                    break;
            }
            progress.setMessage("Подождите...");
            progress.setCancelable(false);
            progress.show();
            Logger.trace(TAG, "BackupWorker.onPreExecute() FINISH");
        }

        @Override
        protected Pair<Boolean, String> doInBackground(Void... params) {
            Logger.trace(TAG, "BackupWorker.doInBackground() START");
            Pair<Boolean, String> result;
            switch (actionType) {
                case BACKUP:
                    result = makeBackup(type);
                    break;
                case RESTORE:
                    result = restoreBackup(type);
                    break;
                case REPLACE:
                    result = replaceFile(type);
                    break;
                default:
                    result = Pair.create(false, "");
                    break;
            }
            Logger.trace(TAG, "BackupWorker.doInBackground() FINISH");
            return result;
        }

        @Override
        protected void onPostExecute(Pair<Boolean, String> result) {
            String prefix = "BackupWorker.onPostExecute(result=" + result + ") ";
            Logger.trace(TAG, prefix + "START");
            super.onPostExecute(result);
            progress.dismiss();
            String text = "";
            switch (actionType) {
                case BACKUP:
                    Logger.info(TAG, prefix + "Завершено создание бекапа " + type.toString() + " (" + ((result.first) ? "Успешно" : "Ошибка") + ")");
                    if (result.first) {
                        text = "файл бекапа создан: " + result.second;
                    } else {
                        text = "Ошибка при создании!";
                    }
                    break;
                case RESTORE:
                    Logger.info(TAG, prefix + "Завершено восстановление бекапа " + type.toString() + " (" + ((result.first) ? "Успешно" : "Ошибка") + ")");
                    if (result.first) {
                        text = "успешно!";
                    } else {
                        text = "ошибка при обработке бекапа в папке: " + text;
                    }

                    if (result.first && type == BackupType.Infotecs) {
                        setupNewEcpType(SharedPreferencesUtils.getEdsType(getApplicationContext()));
                    }
                    if (result.first && type == BackupType.Full) {
                        shiftManager.refreshState();
                    }
                    if (result.first && type == BackupType.Local) {
                        shiftManager.refreshState();
                    }
                    if (result.first && (type == BackupType.Full || type == BackupType.Infotecs || type == BackupType.Local)) {
                        Di.INSTANCE.printerManager().updateConfig();
                    }
                    break;
                case REPLACE:
                    Logger.info(TAG, prefix + "Завершена замена файла " + type.toString() + " (" + ((result.first) ? "Успешно" : "Ошибка") + ")");
                    if (result.first) {
                        text = "успешно!";
                    } else {
                        text = "ошибка при замене файла в папке: " + result.second;
                    }

                    if (result.first && type == BackupType.Local) {
                        shiftManager.refreshState();
                        Di.INSTANCE.printerManager().updateConfig();
                    }
                    break;
                default:
                    break;
            }

            Logger.trace(TAG, prefix + "FINISH");
            onDoneListener.onDone(text);

        }
    }

    /**
     * Метод для инициализации ЭЦП.
     *
     * @param edsType тип ЭЦП.
     */
    private void setupNewEcpType(@NonNull final EdsType edsType) {
        Logger.trace(TAG, "setupNewEdsType " + edsType);
        final FeedbackProgressDialog progressDialog = new FeedbackProgressDialog(this);
        progressDialog.setMessage(String.format(Locale.getDefault(), "Инициализация EDS. Новый тип: %s - %s", edsType, edsType.name()));
        progressDialog.setCancelable(false);
        progressDialog.show();

        Completable
                .fromAction(() -> {
                    SharedPreferencesUtils.setEdsType(getApplicationContext(), edsType);
                    Dagger.appComponent().edsManagerConfigSyncronizer().sync();
                })
                .subscribe(new CompletableSubscriber() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.trace(TAG, e);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onSubscribe(Subscription d) {

                    }
                });
    }

    private interface OnDoneListener {
        void onDone(@NonNull String message);
    }

}
