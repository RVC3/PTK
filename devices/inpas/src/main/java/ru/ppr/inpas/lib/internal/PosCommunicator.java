package ru.ppr.inpas.lib.internal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.ppr.inpas.lib.logger.InpasLogger;
import ru.ppr.inpas.lib.packer.MessagePacker;
import ru.ppr.inpas.lib.protocol.model.PosSignalType;
import ru.ppr.inpas.lib.utils.ByteUtils;

/**
 * Класс для обмена данными с POS терминалом.
 */
public class PosCommunicator {
    private static final String TAG = InpasLogger.makeTag(PosCommunicator.class);
    private static final String CONNECTION_TIMER_NAME = "Inpas Connection timer (communicator)";

    private static final long DEFAULT_CONNECTION_TIMEOUT = 10_000L;
    private static final long DEFAULT_RECEIVE_TIMEOUT = 30_000L;
    private static final long ACK_TIMEOUT = 5_000L;
    private static final int ACK_COUNTER = 3;

    private final Object LOCK = new Object();

    private final String mDevice;
    private final List<IPosListener> mPosListeners;

    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    private boolean mError, mIsReceiveTimeout;

    private long mConnectionTimeout;
    private Timer mConnectionTimer;

    public PosCommunicator(@NonNull final String mac) {
        if (mac.isEmpty()) {
            throw new IllegalArgumentException("MAC address is empty.");
        }

        mDevice = mac;
        mPosListeners = new CopyOnWriteArrayList<>();
        mConnectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    }

    /**
     * Метод для остановки таймаута на соединение.
     */
    private void stopConnectionTimer() {
        synchronized (LOCK) {
            InpasLogger.info(TAG, CONNECTION_TIMER_NAME + ": stop");

            if (mConnectionTimer != null) {
                mConnectionTimer.cancel();
                mConnectionTimer = null;
            }
        }
    }

    /**
     * Метод для запуска таймаута на соединение.
     */
    private void startConnectionTimer() {
        synchronized (LOCK) {
            InpasLogger.info(TAG, CONNECTION_TIMER_NAME + ": start");

            mConnectionTimer = new Timer(CONNECTION_TIMER_NAME);
            mConnectionTimer.schedule(new TimerTask() {
                                          @Override
                                          public void run() {
                                              synchronized (LOCK) {
                                                  InpasLogger.info(TAG, CONNECTION_TIMER_NAME + ": connection timeout.");
                                                  mError = true;

                                                  if ((mConnectionTimer != null) && !isConnected()) {
                                                      for (IPosListener listener : mPosListeners) {
                                                          listener.onError(IPosListener.Error.POS_CONNECTION_TIMEOUT, null);
                                                      }
                                                  }
                                              }
                                          }
                                      },
                    mConnectionTimeout);
        }
    }

    /**
     * Метод для проверки состояния соединения.
     *
     * @return результат проверки.
     */
    public boolean isConnected() {
        return !mError
                && (mSocket != null) && mSocket.isConnected()
                && (mOutputStream != null) && (mInputStream != null);
    }

    /**
     * Метод устанавливающий соединение с POS терминалом.
     */
    public void connect() {
        onChange(IPosListener.State.POS_CONNECTING);
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                final Set<BluetoothDevice> bluetoothDevices = bluetoothAdapter.getBondedDevices();

                if (bluetoothDevices.isEmpty()) {
                    onError(IPosListener.Error.NO_PAIRED_DEVICES_ERROR);
                } else {
                    final BluetoothDevice bluetoothDevice = findBluetoothDevice(bluetoothDevices);

                    if (bluetoothDevice != null) {
                        bluetoothAdapter.cancelDiscovery();
                        try {
                            connectTo(bluetoothDevice);
                        } catch (IOException ioEx) {
                            InpasLogger.error(TAG, ioEx);
                            onError(IPosListener.Error.POS_CONNECTION_ERROR);
                        }
                    } else {
                        onError(IPosListener.Error.DEVICE_NOT_FOUND_ERROR);
                    }
                }
            } else {
                onError(IPosListener.Error.ADAPTER_DISABLED_ERROR);
            }
        } else {
            onError(IPosListener.Error.ADAPTER_NOT_FOUND_ERROR);
        }
    }

    /**
     * Метод закрывающий соединение с POS терминалом.
     */
    public void disconnect() {
        onChange(IPosListener.State.POS_DISCONNECTING);

        try {
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException ioEx) {
            onError(IPosListener.Error.POS_CONNECTION_ERROR);
        } finally {
            mSocket = null;
            mOutputStream = null;
            mInputStream = null;
        }

        onChange(IPosListener.State.POS_DISCONNECTED);
    }

    /**
     * Метод для подключения к POS терминалу.
     *
     * @param bluetoothDevice устройство представляющее POS терминал.
     * @throws IOException в случае, если не удалось установить соединение.
     */
    private void connectTo(@NonNull final BluetoothDevice bluetoothDevice) throws IOException {
        startConnectionTimer();

        try {
            final long startTime = System.currentTimeMillis();
//            mSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            mSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            mSocket.connect();
            InpasLogger.info(TAG, "Total bluetooth connection time: " + String.valueOf(System.currentTimeMillis() - startTime) + " ms.");

            synchronized (LOCK) {
                if (!mError) {
                    stopConnectionTimer();

                    mInputStream = mSocket.getInputStream();
                    mOutputStream = mSocket.getOutputStream();
                }
            }

            if (isConnected()) {
                onChange(IPosListener.State.POS_CONNECTED);
            } else {
                onError(IPosListener.Error.POS_CONNECTION_ERROR);
            }
        } catch (IOException ioEx) {
            stopConnectionTimer();
            throw new IOException(ioEx);
        }
    }

    /**
     * Метод для записи данных в поток.
     *
     * @param data данные для записи.
     */
    private void write(@NonNull final byte[] data) {
        InpasLogger.info(TAG, "Writing.", data, InpasLogger.DataFormat.HEX);

        try {
            mOutputStream.write(data);
        } catch (IOException ioEx) {
            InpasLogger.error(TAG, ioEx);
            onError(IPosListener.Error.SEND_ERROR);
        }
    }

    /**
     * Метод для приема ACK.
     *
     * @return результат приема.
     * @see PosSignalType#ACK
     */
    private boolean receiveAck() {
        InpasLogger.info(TAG, "Receiving Ack.");

        final byte[] ack = new byte[1];
        final long ackTimeout = System.currentTimeMillis() + ACK_TIMEOUT;

        while (!mError && (ackTimeout > System.currentTimeMillis())) {
            final int bytesAvailable = available();

            if (bytesAvailable > 0) {
                read(ack);

                if ((PosSignalType.ACK.getValue() == ack[0]) || (PosSignalType.NAK.getValue() == ack[0])) {
                    break;
                }

            }

            delay(100L);
        }

        final boolean ackReceived = (PosSignalType.ACK.getValue() == ack[0]);
        InpasLogger.info(TAG, "Ack has been received: " + String.valueOf(ackReceived));

        return ackReceived;
    }

    /**
     * Метод устанавливающий желаемую задержку.
     *
     * @param value значение задержки в миллисекундах.
     */
    public void delay(final long value) {
        InpasLogger.info(TAG, "Delay: " + String.valueOf(value));

        if (0 >= value) {
            throw new IllegalArgumentException("Delay value must be greater than 0.");
        }

        try {
            Thread.sleep(value);
        } catch (InterruptedException ex) {
            InpasLogger.error(TAG, ex);
            onError(IPosListener.Error.DELAY_ERROR);
        }
    }

    /**
     * Метод для отправки данных.
     *
     * @param data    данные для отправки.
     * @param waitAck флаг ожидания ответа от терминала.
     */
    public void send(@NonNull final byte[] data, final boolean waitAck) {
        onChange(IPosListener.State.POS_SENDING);
        InpasLogger.info(TAG, "Sending.", data);

        try {
            if (waitAck) {
                int nak = 0;
                boolean ackReceived = false;

                while (!mError && (nak < ACK_COUNTER) && !ackReceived) {
                    write(data);

                    if (mError) {
                        break;
                    }

                    ackReceived = receiveAck();
                    nak++;
                }

                if (!ackReceived) {
                    onError(IPosListener.Error.NAK_ERROR);
                }
            } else {
                write(data);
            }
        } catch (IllegalArgumentException ex) {
            InpasLogger.error(TAG, ex);
            onError(IPosListener.Error.PACKET_ERROR);
        } catch (Exception ex) {
            InpasLogger.error(TAG, ex);
            onError(IPosListener.Error.SEND_ERROR);
        }
    }

    /**
     * Метод для считывания данных.
     *
     * @param destination хранилище для считываемых данных.
     * @return количество считанных данных.
     */
    private int read(@NonNull final byte[] destination) {
        onChange(IPosListener.State.POS_RECEIVING);
        InpasLogger.info(TAG, "Reading.");

        int bytesRead = 0;

        try {
            bytesRead = mInputStream.read(destination);
        } catch (IOException ioEx) {
            InpasLogger.error(TAG, ioEx);
            onError(IPosListener.Error.READ_ERROR);
        }

        InpasLogger.info(TAG, "Read " + String.valueOf(bytesRead) + " byte(s) ", destination);

        return bytesRead;
    }

    /**
     * Метод позводяющий узанть о наличии данных.
     *
     * @return количество байт для чтения.
     */
    private int available() {
        int n = 0;

        try {
            n = mInputStream.available();
        } catch (IOException ioEx) {
            InpasLogger.error(TAG, ioEx);
            onError(IPosListener.Error.ERROR);
        }

        InpasLogger.info(TAG, "Available: " + String.valueOf(n) + " byte(s).");

        return n;
    }

    private boolean isTimeout(final long timeout) {
        return (timeout < System.currentTimeMillis());
    }

    public boolean isReceiveTimeout() {
        return mIsReceiveTimeout;
    }

    /**
     * Метод для приема данных.
     *
     * @return принятые и распакованные данные.
     */
    @NonNull
    public byte[] receive() {
        onChange(IPosListener.State.POS_RECEIVING);
        InpasLogger.info(TAG, "Receiving.");

        byte[] data = new byte[0];
        int nakErrorCounter = 0;
        long receiveTimeout = System.currentTimeMillis() + DEFAULT_RECEIVE_TIMEOUT;
        mIsReceiveTimeout = isTimeout(receiveTimeout);

        while (!mError && !mIsReceiveTimeout) {
            final int availableBytes = available();

            if (availableBytes > 0) {
                final byte[] buffer = new byte[availableBytes];
                read(buffer);

                final byte[] fullData = new byte[data.length + buffer.length];
                System.arraycopy(data, 0, fullData, 0, data.length);
                System.arraycopy(buffer, 0, fullData, data.length, buffer.length);
                data = fullData;

                receiveTimeout += DEFAULT_RECEIVE_TIMEOUT;
                nakErrorCounter = 0;
            } else {
                if (data.length > 0) {
                    try {
                        if (isValidPart(data)) {
                            write(new byte[]{PosSignalType.ACK.getAsByte()});
                        }
                    } catch (Exception ex) {
                        InpasLogger.error(TAG, ex);
                        InpasLogger.error(TAG, "Request: resend packet, attempt " + String.valueOf(nakErrorCounter));

                        data = new byte[0];
                        nakErrorCounter++;
                        write(new byte[]{PosSignalType.NAK.getAsByte()});

                        if (nakErrorCounter >= 3) {
                            onError(IPosListener.Error.CRC_ERROR);
                        }
                    }
                    break;
                }
            }

            delay(400L);
            mIsReceiveTimeout = isTimeout(receiveTimeout);
        }

        if (mIsReceiveTimeout) {
            onError(IPosListener.Error.POS_RECEIVE_TIMEOUT_ERROR);
        }

        return data;
    }

    /**
     * Метод для валидации полученных данных.
     *
     * @param data данные для проверки.
     * @return результат проверки.
     */
    private boolean isValidPart(@NonNull final byte[] data) {
        return ByteUtils.isValidCrc16(data) && MessagePacker.isValidPart(data);
    }

    /**
     * Метод для поиска bluetooth устройства.
     *
     * @param bluetoothDevices список bluetooth устройств в которых будет осуществлен поиск.
     * @return bluetooth устройство.
     */
    @Nullable
    private BluetoothDevice findBluetoothDevice(@NonNull final Set<BluetoothDevice> bluetoothDevices) {
        BluetoothDevice bluetoothDevice = null;

        for (BluetoothDevice device : bluetoothDevices) {
            if (BluetoothDevice.BOND_BONDED == device.getBondState()
                    && device.getAddress().equals(mDevice)) {
                bluetoothDevice = device;

                break;
            }
        }

        return bluetoothDevice;
    }

    /**
     * Метод возвращает текущее значение таймаута на соедиенение в миллисекундах.
     */
    public long getConnectionTimeout() {
        return mConnectionTimeout;
    }

    /**
     * Метод для установления таймаута на соедиенение.
     *
     * @param value значение таймаута в миллисекундах.
     */
    public void setConnectionTimeout(final long value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Incorrect POS connection timeout value.");
        }

        mConnectionTimeout = value;
    }

    /**
     * Метод для добавления подписчика на событие {@link IPosListener}
     *
     * @param listener подписчик.
     */
    public void addPosListener(@NonNull final IPosListener listener) {
        mPosListeners.add(listener);
    }

    /**
     * Метод для удаления подписчика на событие {@link IPosListener}
     *
     * @param listener подписчик.
     */
    public void removePosListener(@NonNull final IPosListener listener) {
        mPosListeners.remove(listener);
    }

    private void onChange(@NonNull final IPosListener.State state) {
        InpasLogger.info(TAG, String.valueOf(state));

        for (IPosListener listener : mPosListeners) {
            listener.onChanged(state);
        }
    }

    private void onError(@NonNull final IPosListener.Error error) {
        synchronized (LOCK) {
            InpasLogger.error(TAG, String.valueOf(error));
            mError = true;
        }

        for (IPosListener listener : mPosListeners) {
            listener.onError(error, null);
        }
    }

}