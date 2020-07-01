package ru.ppr.cppk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.File;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.export.ArmSyncEvents;
import ru.ppr.cppk.export.Exchange.Error;
import ru.ppr.cppk.export.Exchange.ExchangeListener;
import ru.ppr.cppk.export.model.request.GetEventsReq;
import ru.ppr.cppk.export.model.request.PtkStartSyncRequest;
import ru.ppr.logger.Logger;

public class ConnectService extends Service {

    public static final String TAG = Logger.makeLogTag(ConnectService.class);

    public static class ServiceAction {
        public static final int START = 1;
        public static final int STOP = 2;
    }

    private void addLog(String log) {
        long id = Di.INSTANCE.getPrivateSettings().get().getTerminalNumber();
        Logger.info(TAG, " (userId = " + id + ")" + log);
    }

    public void actStart(int startId) {
        addLog("action start startId:" + startId);
        final Globals g = (Globals) getApplication();

        g.getExchange().startListen(new ExchangeListener() {

            @Override
            public void onStopped(String text) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.Stopped, "", new Error());
                addLog("onStopped");
            }

            @Override
            public void onSetTimeReq(long timeInMilliseconds, long eventTimeMilliseconds, Error error, long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.SetTimeReqDetected, "", error);
                addLog("onSetTimeReq timeInMilliseconds:" + timeInMilliseconds + " errorCode:" + error.code + " ErrorMessage: " + error.getMessage() + ", requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().createSetTimeResp(timeInMilliseconds, eventTimeMilliseconds, error, requestTimeStamp);
            }

            @Override
            public void onSetSettingsReq(PrivateSettings overridedSettings, Error error, long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.SetPrivateSettingsReqDetected, "", error);
                addLog("onSetSettingsReq errorCode:" + error.code + " ErrorMessage: " + error.getMessage() + ", requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().createSetSettingsResp(overridedSettings, error, requestTimeStamp);
            }

            @Override
            public void onSecurityDbUpdataReq(File zipFile, Error error, long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.SecurityDbUpdataReqDetected, "", error);
                addLog("onSecurityDbUpdataReq zipFile: " + zipFile.getAbsolutePath() + " errorCode:" + error.code + " ErrorMessage: " + error.getMessage() + ", requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().createSecurityDbUpdateResp(zipFile, error, requestTimeStamp);
            }

            @Override
            public void onRdsDbUpdataReq(File zipFile, Error error, long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.RdsDbUpdataReqDetected, "", error);
                addLog("onRdsDbUpdataReq zipFile: " + zipFile.getAbsolutePath() + " errorCode:" + error.code + " ErrorMessage: " + error.getMessage() + ", requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().createRdsDbUpdateResp(zipFile, error, requestTimeStamp);
            }

            @Override
            public void onGetTimeReq(Error error, long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.GetTimeReqDetected, "", error);
                addLog("onGetTimeReq errorCode:" + error.code + " ErrorMessage: " + error.getMessage() + ", requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().createGetTimeResp(error, requestTimeStamp);
            }

            @Override
            public void onGetSettingsReq(Error error, long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.GetPrivateSettingsReqDetected, "", error);
                addLog("onGetSettingsReq errorCode:" + error.code + " ErrorMessage: " + error.getMessage() + ", requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().createGetSettingsResp(error, requestTimeStamp);
            }

            @Override
            public void onGetLastShiftReq(Error error, long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.GetLastShiftReqDetected, "", error);
                addLog("onGetLastShiftReq errorCode:" + error.code + " ErrorMessage: " + error.getMessage() + ", requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().createGetLastShiftResp(error, requestTimeStamp);
            }

            @Override
            public void onGetEventsReq(GetEventsReq getEventsReq, Error error, long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.GetEventsReqDetected, getEventsReq.toString(), error);
                addLog(getEventsReq.toString() + " errorCode:" + error.code + " ErrorMessage: "
                        + error.getMessage() + ", requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().createGetEventsResp(getEventsReq, error, requestTimeStamp);
            }

            @Override
            public void onGetStateReq(long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.GetStateReqDetected, "", new Error());
                addLog("onGetStateReq, requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().createGetStateResp(requestTimeStamp);
            }

            @Override
            public void onArmConnected(boolean oldRequest, boolean isEcpOk, Error error, PtkStartSyncRequest ptkStartSyncRequest, long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.Connected, "", new Error());
                addLog("onArmConnected, requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().armConnected(oldRequest, isEcpOk,  error,  ptkStartSyncRequest,  requestTimeStamp);
            }

            @Override
            public void onNewVersionDetected(File apkFile, Error error, long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.UpdatePoReqDetected, "", error);
                addLog("onApkFileDetectedNew apkFile: " + apkFile.getAbsolutePath() + " errorCode:" + error.code + " ErrorMessage: " + error.getMessage() + ", requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().createNewVersionResp(apkFile, error, requestTimeStamp);
            }

            @Override
            public void onCommonSettingsDetected(File xmlFile, Error error, long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.SetCommonSettingsReqDetected, "", error);
                addLog("onCommonSettingsDetected errorCode:" + error.code + " ErrorMessage: " + error.getMessage() + ", requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().createCommonSettingsResp(xmlFile, error, requestTimeStamp);
            }
  
            @Override
            public void onArmDisconnected(long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.Disconnected, "", new Error());
                addLog("onArmDisconnected, requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().armDisconnected(requestTimeStamp);
            }

            @Override
            public void onTransmissionCompleteDetected(File inTarGzFile, File jsonToDeleteFile, long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.TransmissionCompleteDetected, "", new Error());
                addLog("onTransmissionCompleteDetected (in.tar.gz и sftfilestodelete.bin), requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().createTransmissionCompleteResp1(inTarGzFile, jsonToDeleteFile, requestTimeStamp);
            }

            @Override
            public void onSyncFinishedDetected(long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.SyncFinishedDetected, "", new Error());
                addLog("onSyncFinishedDetected, requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().createOnSyncFinishedResp(requestTimeStamp);
            }

            @Override
            public void onGetBackupReq(Error error, long requestTimeStamp) {
                g.getBroadcasts().newArmSyncEvent(ConnectService.this, ArmSyncEvents.GetBackupReqDetected, "", error);
                addLog("onGetBackupReq errorCode:" + error.code + " ErrorMessage: " + error.getMessage() + ", requestTimeStamp = " + requestTimeStamp);
                Globals.getInstance().getResponse().createGetBackupResp(error, requestTimeStamp);
            }

        });
    }

    @Override
    public void onCreate() {
        addLog("onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        addLog("onStartCommand() startId: " + startId);
        actStart(startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        addLog("onDestroy()");
        Globals g = (Globals) getApplication();
        g.getExchange().stop(TAG + ".onDestroy()");
        //Globals.getInstance().getResponse().clearStateFolder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
