package ru.ppr.cppk.utils;

import android.content.Context;

import org.lanter.lan4gate.ICommunicationCallback;
import org.lanter.lan4gate.IRequest;
import org.lanter.lan4gate.IResponse;
import org.lanter.lan4gate.IResponseCallback;
import org.lanter.lan4gate.Lan4Gate;
import org.lanter.lan4gate.Messages.Fields.ResponseFieldsList;
import org.lanter.lan4gate.Messages.OperationsList;

import ru.ppr.cppk.PosBindingActivity;
import ru.ppr.cppk.managers.AppNetworkManager;
import ru.ppr.ingenico.core.IngenicoTerminal;
import ru.ppr.inpas.lib.command.nonfinancial.CloseSessionCommand;
import ru.ppr.ipos.IPos;
import ru.ppr.ipos.exception.PosException;
import ru.ppr.ipos.model.FinancialTransactionResult;
import ru.ppr.ipos.model.TransactionResult;
import ru.ppr.logger.Logger;


public class InternalPos9000S implements IPos {

    class ResponseListener implements IResponseCallback {
        @Override
        public void newResponseMessage(IResponse response, Lan4Gate initiator) {
            String lresult = "Результат операции ";
            String lOperation = "";
            for (ResponseFieldsList field : response.getCurrentFieldsList()) {
                // Код для обработки каждого поля
                switch (field.getString()){
                    case "Status":
                        status = response.getStatus().getNumber();
                        break;
                    case "OperationCode":
                        int code = response.getOperationCode().getNumber();
                        switch (code){
                            case 1: lOperation = "Sale"; break;
                            case 817: lOperation = "SelfTest"; break;
                            case 802: lOperation = "Test Host"; break;
                        }
                        break;
                }
            }
            lresult = lresult + lOperation + " = " + status;
            running = false;
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
            Logger.trace(TAG, "Connect with LAN4Tap is established" );
        }

        @Override
        public void disconnected(Lan4Gate initiator) {
            // код для обработки отключения
        }
    }

    private static final String TAG = Logger.makeLogTag(InternalPos9000S.class);
    private final int port;
    private Lan4Gate gate;
    private int status = 0;
    /**
     * Таймаут подключения к терминалу
     */
    private long connectionTimeout;
    /**
     * Менеджер для включения/выключения интернета
     */
    /**
     * Флаг, что операция ещё выполняется
     */
    private volatile boolean running;
    private final AppNetworkManager internetManager;
    private ResponseListener responseListener;
    private CommunicationListener communicationListener;

    @Override
    public void cancel(FinancialTransactionResult saleResult, ResultListener<FinancialTransactionResult> resultListener) {
        /*InpasLogger.info(TAG, "Calling cancel()");

        if (isReady()) {
            final long lastTransactionId = mSettings.getLastTransactionId();

            if (lastTransactionId != saleResult.getId()) {
                if (saleResult.isApproved()) {
                    execute(new CancelCommand(saleResult.getAmount(), saleResult.getId(),
                                    OperationCode.SALE.getValue(),
                                    saleResult.getAuthorizationId(), saleResult.getRRN()
                            ),
                            resultListener
                    );
                } else {
                    publish(resultListener, null);
                }
            } else {
                if (saleResult.isApproved()) {
                    mTechnicalCancelCommand = new SaleCommand(saleResult.getAmount(), saleResult.getId());
                    technicalCancel(resultListener);
                } else {
                    if (mSettings.getLastTransactionApprovedStatus()) {
                        mTechnicalCancelCommand = new SaleCommand(saleResult.getAmount(), saleResult.getId());
                        technicalCancel(resultListener);
                    } else {
                        mIsNeedToModifyCancelResult = true;
                        execute(new TestHostCommand(), resultListener);
                    }
                }
            }
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }*/
    }

    @Override
    public void technicalCancel(ResultListener<FinancialTransactionResult> resultListener) {
/*        InpasLogger.info(TAG, "Calling technicalCancel()");

        if (isReady()) {
            if ((mTechnicalCancelCommand != null) && mSettings.getLastTransactionApprovedStatus()) {
                execute(new EmergencyCancellationCommand(mTechnicalCancelCommand), resultListener);
            } else {
                execute(null, resultListener);
            }
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }*/
    }

    @Override
    public void sale(int price, int saleTransactionId, ResultListener<FinancialTransactionResult> resultListener) {
        Logger.trace(TAG, "Calling sale()");
        IRequest sale;

        if (isReady()) {
            sale = gate.getPreparedRequest(OperationsList.Sale);
            sale.setAmount(price);
            sale.setCurrencyCode(643);
            sale.setEcrMerchantNumber(1);
            running = true;
            gate.sendRequest(sale);
        } else {
            throw new IllegalStateException("Wrong terminal state!");
        }
    }

    @Override
    public void updateSoftware(ResultListener<TransactionResult> resultListener) {
/*        InpasLogger.info(TAG, "Calling updateSoftware()");

        if (isReady()) {
            execute(null, resultListener);
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }*/
    }

    /**
     * В Inpas не предусмотрена данная команда.
     */
    @Override
    public void openSession(ResultListener<TransactionResult> resultListener) {
        Logger.trace(TAG, "Calling openSession()");
//        gate.setPort(20501);


        if (isReady()) {
//            if (!gate.linkIsConnected()) gate.start();
//            testHost(resultListener);
        } else {
            throw new IllegalStateException("Wrong terminal state!");
        }
    }

    @Override
    public void closeSession(ResultListener<TransactionResult> resultListener) {
        Logger.trace(TAG, "Calling closeSession()");

        if (isReady()) {
            resultListener.onResult(null);
//            execute(new CloseSessionCommand(0), resultListener); // В будущем: Check transaction number.
        } else {
            throw new IllegalStateException("Wrong terminal state!");
        }
    }

    @Override
    public void getTransactionsJournal(ResultListener<TransactionResult> resultListener) {
/*        InpasLogger.info(TAG, "Calling getTransactionsJournal()");

        if (isReady()) {
            execute(new TransactionJournalCommand(), resultListener);
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }*/
    }

    @Override
    public void getTransactionsTotal(ResultListener<TransactionResult> resultListener) {
/*        InpasLogger.info(TAG, "Calling getTransactionsTotal()");

        if (isReady()) {
            execute(null, resultListener);
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }*/
    }

    @Override
    public void invokeApplicationMenu(ResultListener<TransactionResult> resultListener) {
/*        InpasLogger.info(TAG, "Calling invokeApplicationMenu()");

        if (isReady()) {
            execute(null, resultListener);
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }*/
    }

    @Override
    public void addConnectionListener(ConnectionListener connectionListener) {
        Logger.trace(TAG, "Calling addConnectionListener()");
//        connectionListeners.add(connectionListener);
        responseListener = new ResponseListener();
        //Создание слушателя для состояния сетевого взаимодействия
        communicationListener = new CommunicationListener();
        gate.addResponseCallback(responseListener);
        gate.addCommunicationCallback(communicationListener);
    }

    @Override
    public void removeConnectionListener(ConnectionListener connectionListener) {
        Logger.trace(TAG, "Calling removeConnectionListener()");
//        connectionListeners.remove(connectionListener);
        gate.removeResponseCallback(responseListener);
        gate.removeCommunicationCallback(communicationListener);
    }

    @Override
    public void prepareResources() throws PosException {
        Logger.trace(TAG, "Calling prepareResources()");
        gate.setPort(20501);
        if (!gate.linkIsConnected()) gate.start();
        if (isReady()){
//            sale(1, 0, null);
//            while (!isReady());
        }
    }
    @Override
    public void testSelf(ResultListener<TransactionResult> resultListener) {
        Logger.trace(TAG, "Calling testSelf()");
        IRequest sale;

        if (isReady()) {
            sale = gate.getPreparedRequest(OperationsList.SelfTest);
            running = true;
            gate.sendRequest(sale);
        } else {
            throw new IllegalStateException("Wrong terminal state!");
        }
    }
    @Override
    public void testHost(ResultListener<TransactionResult> resultListener) {
        Logger.trace(TAG, "Calling testHost()");
        IRequest sale;

        if (isReady()) {
            sale = gate.getPreparedRequest(OperationsList.Test);
            running = true;
            gate.sendRequest(sale);
        } else {
            throw new IllegalStateException("Wrong terminal state!");
        }
    }

    @Override
    public void freeResources() throws PosException {
        Logger.trace(TAG, "Calling freeResources()");
        if (gate.linkIsConnected()) gate.stop();
        running = false;
//        if (!internetManager.disable()) {
//            throw new PosException("Could not disable internet");
//        }
    }

    public InternalPos9000S(Context context,
                            AppNetworkManager internetManager,
//                            String sharedPrefsFileName,
//                            String packageName,
//                            String configFileName,
//                            String macAddress,
                            int port,
                            int connectionTimeout) {

        this.internetManager = internetManager;
        this.port = port;
        setConnectionTimeout(connectionTimeout);
        this.running = false;
        int ecrNumber = 1;
        gate = new Lan4Gate(ecrNumber);
        addConnectionListener(null);
    }

    @Override
    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public void terminate() {
        // TODO
    }

    @Override
    public boolean isReady() {
        return !running;
    }

/*    public interface InternetManager {
        boolean enable();

        boolean disable();
    }*/
}
