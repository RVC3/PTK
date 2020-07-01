package ru.ppr.cppk.utils;

import android.content.Context;

import org.lanter.lan4gate.ICommunicationCallback;
import org.lanter.lan4gate.IResponse;
import org.lanter.lan4gate.IResponseCallback;
import org.lanter.lan4gate.Lan4Gate;
import org.lanter.lan4gate.Messages.Fields.ResponseFieldsList;

import ru.ppr.cppk.managers.AppNetworkManager;
import ru.ppr.ipos.IPos;
import ru.ppr.ipos.exception.PosException;
import ru.ppr.ipos.model.FinancialTransactionResult;
import ru.ppr.ipos.model.TransactionResult;
import ru.ppr.logger.Logger;

class ResponseListener implements IResponseCallback {
    @Override
    public void newResponseMessage(IResponse response, Lan4Gate initiator) {
        for (ResponseFieldsList field : response.getCurrentFieldsList()) {
            // Код для обработки каждого поля
            switch (field.getString()){
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
    }

    @Override
    public void disconnected(Lan4Gate initiator) {
        // код для обработки отключения
    }
}

public class InternalPos9000S implements IPos {

    private final int port;
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
/*        InpasLogger.info(TAG, "Calling sale()");

        if (isReady()) {
            execute(new SaleCommand(price, saleTransactionId), resultListener);
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }*/
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
/*        InpasLogger.info(TAG, "Calling openSession()");

        if (isReady()) {
            testHost(resultListener);
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }*/
    }

    @Override
    public void closeSession(ResultListener<TransactionResult> resultListener) {
/*        InpasLogger.info(TAG, "Calling closeSession()");

        if (isReady()) {
            execute(new CloseSessionCommand(0), resultListener); // В будущем: Check transaction number.
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }*/
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
//        connectionListeners.add(connectionListener);
    }

    @Override
    public void removeConnectionListener(ConnectionListener connectionListener) {
//        connectionListeners.remove(connectionListener);
    }

    @Override
    public void prepareResources() throws PosException {
//        if (!internetManager.enable()) {
//            throw new PosException("Could not enable internet");
//        }
    }
    @Override
    public void testSelf(ResultListener<TransactionResult> resultListener) {
/*        InpasLogger.info(TAG, "Calling testSelf()");

        if (isReady()) {
            setTerminalState(InpasTerminal.TerminalState.BUSY);

            mResultListener = result -> {
                if (resultListener != null) {
                    resultListener.onResult(result);
                }

                mCountDownLatch.countDown();
            };

            try {
                startConnectionTimer();
                mPosDriver.process(new TestSelfCommand().getPacket());
                mCountDownLatch = new CountDownLatch(1);
                mCountDownLatch.await();
            } catch (InterruptedException ex) {
                InpasLogger.error(TAG, ex);
            } finally {
                setTerminalState(InpasTerminal.TerminalState.ACTIVE);
            }
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }*/
    }
    @Override
    public void testHost(ResultListener<TransactionResult> resultListener) {
/*        InpasLogger.info(TAG, "Calling testHost()");

        if (isReady()) {
            execute(new TestHostCommand(), resultListener);
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }*/
    }

    @Override
    public void freeResources() throws PosException {
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
        this.connectionTimeout = connectionTimeout;
        this.running = false;
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
