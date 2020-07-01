package ru.ppr.cppk.debug;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;

import com.androidquery.AQuery;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.PathsConstants;
import ru.ppr.cppk.R;
import ru.ppr.cppk.export.Exchange;
import ru.ppr.cppk.export.Response;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.LoggedActivity;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils;

/**
 * Created by Кашка Григорий on 29.04.2016.
 */
public class PullSftTestActivity extends LoggedActivity implements View.OnClickListener {
    private final String DIR = Environment.getExternalStorageDirectory().getPath() + "/CPPKInternal";
    private EditText logView = null;
    public boolean inProgress = false;
    public TestWorker testWorker=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_sft_pull_test);
        AQuery aQuery = new AQuery(this);
        aQuery.id(R.id.startTest).clicked(this);
        aQuery.id(R.id.stopTest).clicked(this);
        aQuery.id(R.id.clearLog).clicked(this);
        logView = aQuery.id(R.id.logET).textSize(10).getEditText();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.startTest: {
                startTest();
                break;
            }
            case R.id.stopTest: {
                stopTest();
                break;
            }
            case R.id.clearLog: {
                logView.setText("");
                break;
            }

            default:
                break;
        }

    }

    private void startTest() {
        if (!isInProgress()) {
            testWorker=new TestWorker();
            inProgress=true;
            testWorker.executeOnExecutor(SchedulersCPPK.backgroundExecutor());
        }
    }

    private void stopTest() {
        if (inProgress) {
            testWorker.cancel(true);
            inProgress=false;
        }
    }

    private boolean isInProgress() {
        if (inProgress) {
            Globals.getInstance().getToaster().showToast("Идет тестирование!");
        }
        return inProgress;
    }

    /**
     * Интерфейс, для получения логи тестирования оптического считывателя ШК
     *
     * @author A.Ushakov
     */
    public interface OnTestFinished {
        /**
         * Вызвается после завершения тестирования оптического считывателя
         *
         * @param log
         */
        public void onTestFinish(String log);
    }



    private class TestWorker extends AsyncTask<Void, String, Boolean> {

        private FeedbackProgressDialog progress = null;
        private String log = "";
        private int countWaitTransmissionCompleteResp = 120;
        private int countWaitState = 120;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progress = new FeedbackProgressDialog(PullSftTestActivity.this);
//            progress.setTitle("Тестирование");
//            progress.setMessage("Подождите...");
//            progress.setCancelable(false);
//            progress.show();
            log=log+"Запускаем тестирование!";
            addLog(log);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            int i=1;
            while(i<=10) {
                publishProgress("----------   Итерация №"+i+"   ----------");

                File inTarGz = new File(Exchange.SFT_inTarGz);
                File filesListDelete = new File(Exchange.SFT_listDelete);
                File trComplete = new File(Exchange.SFT_transmissionCompleted);
                File trCompleteResp = new File(Response.SFT_transmissioncompleted_resp);

                publishProgress("Удаляем файл " + trCompleteResp.getName());
                trCompleteResp.delete();

                publishProgress("Подкладываем файлы in.tar.gz | sftfilestodelete.bin | transmissioncompleted.info в папку SFT");

                FileUtils.copy(getApplicationContext(),new File( DIR+"/TEST/"+inTarGz.getName()), inTarGz);
                FileUtils.copy(getApplicationContext(), new File( DIR + "/TEST/"+filesListDelete.getName()), filesListDelete);
                FileUtils.copy(getApplicationContext(), new File( DIR + "/TEST/" + trComplete.getName()), trComplete);
                publishProgress("Файлы in.tar.gz | sftfilestodelete.bin | transmissioncompleted.info в папку SFT скопированы!");


                int count = 0;
                publishProgress("Ждем файл " + trCompleteResp.getName() + " не более " + countWaitTransmissionCompleteResp+"c ...");
                while (!trCompleteResp.exists() && count<countWaitTransmissionCompleteResp) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
                    if (!inProgress) return false;
                }
                if (trCompleteResp.exists())
                    publishProgress("Файл " + trCompleteResp.getName() + " найден!");
                else {
                    publishProgress("Не удалось дождаться файла " + trCompleteResp.getName());
                    return false;
                }
                publishProgress(FileUtils.getFileContent(trCompleteResp.getAbsolutePath())

                );

                File stateFile = new File(Response.STATE_getStateResp);
                File stateSigFile = new File(Response.STATE_getStateRespSig);

                publishProgress("Удаление файла " + trCompleteResp.getName() + ((trCompleteResp.delete()) ? " - ОК" : " - Ошибка"));
                publishProgress("Удаление файла " + stateFile.getName() + ((stateFile.delete()) ? " - ОК" : " - Ошибка"));

                count = 0;
                publishProgress("Ждем файл " + stateFile.getName() + " не более " + countWaitState+"c ...");
                while (!stateFile.exists() && count<countWaitState) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
                    if (!inProgress) return false;
                }
                if (stateFile.exists())
                    publishProgress("Файл " + stateFile.getName() + " найден!");
                else {
                    publishProgress("Не удалось дождаться файла " + trCompleteResp.getName());
                    return false;
                }
                publishProgress(FileUtils.getFileContent(stateFile.getAbsolutePath()));
                publishProgress(FileUtils.getFileContent(Response.STATE_getStateRespSig));
                publishProgress("Размер файла state.sig: "+stateSigFile.length());

                File sftfileslistFile = new File(Response.STATE_sftfileslist);
                try {
                    Gson gson = new Gson();
                    String json = Exchange.loadJSONFromFile(sftfileslistFile);
                    String[] out = gson.fromJson(json, String[].class);
                    publishProgress("В файле " + sftfileslistFile.getName() + " " + out.length + " элементов");
                } catch (IOException e) {
                    e.printStackTrace();
                    publishProgress("Не удалось получить данные из файл " + sftfileslistFile.getName() + " " + e.getMessage());
                }

                File cppkConnectSftFolder = new File(Exchange.SFT_OUT);
                String[] cppkConnectOutList = cppkConnectSftFolder.list();

                publishProgress("Содержимое папки " + cppkConnectSftFolder.getAbsolutePath() + "/ :" + ((cppkConnectOutList.length == 0) ? "нет файлов!" : ""));
                for (String name : cppkConnectOutList)
                    publishProgress(name);

                i++;
                if (!inProgress) return false;
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(String... log) {
            super.onProgressUpdate(log);
            this.log=this.log+"/n"+log[0];
            addLog(log[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
//            progress.dismiss();
            Globals.getInstance().getToaster().showToast((result)?"Done OK!":"Done Error!");
            Logger.info(getClass(), log);
            inProgress=false;
        }
    }

    @Override
    public void onBackPressed() {
        if (!isInProgress())
            super.onBackPressed();
    }

    private void addLog(String text) {
        Logger.trace("qazak",text);
        logView.setText(text + "\n-----------------------\n" + logView.getText());
    }

}

