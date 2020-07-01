package ru.ppr.inpas.lib.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

import ru.ppr.inpas.lib.logger.InpasLogger;

/**
 * Класс для обмена данными по сети.
 */
public class NetworkCommunicator {
    private static final String TAG = InpasLogger.makeTag(NetworkCommunicator.class);

    private static final long DEFAULT_CONNECTION_TIMEOUT = 10_000L;
    private static final long DEFAULT_RECEIVE_TIMEOUT = 30_000L;

    private static final int PORT_RANGE_MIN = 1024;
    private static final int PORT_RANGE_MAX = 65535;

    private final List<IPosListener> mPosListeners = new LinkedList<>();

    private long mConnectionTimeout, mReceiveTimeout;

    private boolean mHasEndpoint;
    private String mIpAddress;
    private int mPort;
    private Socket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    public NetworkCommunicator() {
        mConnectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        mReceiveTimeout = DEFAULT_RECEIVE_TIMEOUT;
    }

    /**
     * Метод для установления удаленной точки доступа.
     *
     * @param ip   IP-адрес.
     * @param port порт.
     */
    public void setEndPoint(@NonNull final String ip, final int port) {
        if (ip.isEmpty()) {
            throw new IllegalArgumentException("IP address must be not empty.");
        }

        if ((port < PORT_RANGE_MIN) || (port > PORT_RANGE_MAX)) {
            throw new IllegalArgumentException("Port is out of range.");
        }

        mIpAddress = ip;
        mPort = port;
        mHasEndpoint = true;
    }

    /**
     * Метод позволяющий провериьь было ли установлено соединение.
     *
     * @return результат проверки.
     */
    public boolean isConnected() {
        return (mSocket != null) && mSocket.isConnected()
                && (mInputStream != null) && (mOutputStream != null);
    }

    /**
     * Метод устанавливающий соединение с удаленной точкой доступа {@link #setEndPoint(String, int)}
     *
     * @throws IOException в случает если неудалось установиьь соединение.
     */
    public void connect() throws IOException {
        onChange(IPosListener.State.NETWORK_CONNECTING);

        try {
            if (!mHasEndpoint) {
                throw new IllegalArgumentException("No endpoint data.");
            }

            mSocket = new Socket();
            mSocket.setSoTimeout((int) mReceiveTimeout);
            mSocket.connect(new InetSocketAddress(mIpAddress, mPort), (int) mConnectionTimeout);

            mInputStream = mSocket.getInputStream();
            mOutputStream = mSocket.getOutputStream();

            if (isConnected()) {
                onChange(IPosListener.State.NETWORK_CONNECTED);
            } else {
                onError(IPosListener.Error.NETWORK_CONNECTION_ERROR);
            }
        } catch (SocketTimeoutException stEx) {
            onError(IPosListener.Error.NETWORK_CONNECTION_TIMEOUT);
            throw new IOException(stEx);
        } catch (IOException ioEx) {
            onError(IPosListener.Error.NETWORK_CONNECTION_ERROR);
            throw new IOException(ioEx);
        }
    }

    /**
     * Метод закрывающий соединение с удаленной точкой доступа.
     */
    public void disconnect() {
        InpasLogger.info(TAG, "Disconnecting from network.");
        onChange(IPosListener.State.NETWORK_DISCONNECTING);

        if (isConnected()) {
            try {
                if (mSocket != null) {
                    mSocket.close();
                }
            } catch (IOException ioEx) {
                onError(ioEx, IPosListener.Error.NETWORK_CONNECTION_ERROR);
            } finally {
                mSocket = null;
                mInputStream = null;
                mOutputStream = null;
            }
        }

        onChange(IPosListener.State.NETWORK_DISCONNECTED);
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

    /**
     * Метод для отправки данных.
     *
     * @param data данные для отправки.
     * @throws Exception в случае, если данные не удалось отправить.
     */
    public void send(@NonNull final byte[] data) throws Exception {
        onChange(IPosListener.State.NETWORK_SENDING);
        InpasLogger.info(TAG, "Writing via network: ", data);
        mOutputStream.write(data);
    }

    /**
     * Метод для приема данных.
     *
     * @return принятые данные.
     * @throws Exception в случае, если данные не удалось принять.
     */
    @Nullable
    public byte[] receive() throws Exception {
        onChange(IPosListener.State.NETWORK_RECEIVING);
        InpasLogger.info(TAG, "Reading from network.");

        byte[] buffer = null;
        boolean isDone = false;
        long receiveTimeout = System.currentTimeMillis() + mReceiveTimeout;
        boolean isTimeout = (receiveTimeout < System.currentTimeMillis());

        while (!isDone && !isTimeout) {
            final int bytesAvailable = available();

            if (bytesAvailable > 0) {
                buffer = new byte[bytesAvailable];
                int bytesRead = mInputStream.read(buffer);

                while (bytesRead < bytesAvailable) {
                    bytesRead += mInputStream.read(buffer, bytesRead, buffer.length - bytesRead);
                }

                isDone = true;
                InpasLogger.info(TAG, "Read", buffer);
            } else {
                InpasLogger.info(TAG, "No bytes to read.");
            }

            delay(400L);
            isTimeout = (receiveTimeout < System.currentTimeMillis());
        }

        if (isTimeout) {
            InpasLogger.info(TAG, "Timeout exit: NETWORK_RECEIVE_TIMEOUT_ERROR");
            onError(IPosListener.Error.NETWORK_RECEIVE_TIMEOUT_ERROR);
        }

        return buffer;
    }

    /**
     * Метод устанавливающий желаемую задержку.
     *
     * @param value значение задержки в миллисекундах.
     */
    private void delay(final long value) {
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
     * Метод для добавления подписчика на событие {@link IPosListener}
     *
     * @param listener подписчик.
     */
    public void addIPosListener(@NonNull final IPosListener listener) {
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
        InpasLogger.error(TAG, String.valueOf(error));

        for (IPosListener listener : mPosListeners) {
            listener.onError(error, null);
        }
    }

    private void onError(@NonNull final Exception exception, @NonNull final IPosListener.Error error) {
        InpasLogger.error(TAG, exception);
        onError(error);
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
            throw new IllegalArgumentException("Incorrect Network connection timeout value.");
        }

        mConnectionTimeout = value;
    }

    /**
     * Метод возвращает текущее значение таймаута на прием данных в миллисекундах.
     *
     * @return значение таймаута в миллисекундах.
     */
    public long getReceiveTimeout() {
        return mReceiveTimeout;
    }

    /**
     * Метод для установления таймаута на прием данных в миллисекундах.
     *
     * @param value
     */
    public void setReceiveTimeout(final long value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Incorrect Network receive timeout value.");
        }

        mReceiveTimeout = value;
    }

}