package ru.ppr.utils;

import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.ppr.logger.Logger;

/**
 * Команда shell.
 *
 * @author Aleksandr Brazhkin
 */
public class ShellCommand {

    private final static String TAG = Logger.makeLogTag(ShellCommand.class);

    private final Object LOCK = new Object();

    /**
     * Стандартный поток ввода-вывода.
     */
    public enum StdOutputStream {
        /**
         * Стандартный вывод
         */
        STD_OUT,
        /**
         * Стандартный вывод ошибок
         */
        STD_ERR
    }

    private final String command;
    private final EnumSet<StdOutputStream> stdOutputStreams;
    final StringBuilder outputStringBuilder;
    private final boolean runAsSu;
    private final AtomicBoolean alreadyExecuted = new AtomicBoolean(false);
    private boolean finished = false;
    private Process process = null;
    private OutputWriterThread inputStreamWriterThread;
    private OutputWriterThread errorStreamWriterThread;

    ShellCommand(@NonNull String command, @NonNull EnumSet<StdOutputStream> stdOutputStreams, boolean runAsSu) {
        this.command = command;
        this.stdOutputStreams = stdOutputStreams;
        this.outputStringBuilder = new StringBuilder();
        this.runAsSu = runAsSu;
    }

    /**
     * Запускает команду на выполнение.
     * Допускается только 1 вызов данного метода.
     */
    public void run() throws IOException, InterruptedException {
        Logger.trace(TAG, "run() called");

        if (alreadyExecuted.getAndSet(true)) {
            throw new IllegalStateException("Command is already executed");
        }

        DataOutputStream os = null;

        try {
            synchronized (LOCK) {
                if (finished) {
                    Logger.trace(TAG, "Already is finished");
                    return;
                }

                process = Runtime.getRuntime().exec(runAsSu ? "su" : "sh");
            }

            Logger.trace(TAG, "run() working");

            if (stdOutputStreams.contains(StdOutputStream.STD_OUT)) {
                if (inputStreamWriterThread != null && !inputStreamWriterThread.isInterrupted()) {
                    inputStreamWriterThread.interrupt();
                    inputStreamWriterThread = null;
                }

                inputStreamWriterThread = new OutputWriterThread(process.getInputStream());
                inputStreamWriterThread.start();
            }

            if (stdOutputStreams.contains(StdOutputStream.STD_ERR)) {
                if (errorStreamWriterThread != null && !errorStreamWriterThread.isInterrupted()) {
                    errorStreamWriterThread.interrupt();
                    errorStreamWriterThread = null;
                }

                errorStreamWriterThread = new OutputWriterThread(process.getErrorStream());
                errorStreamWriterThread.start();
            }

            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command);
            os.writeChar('\n');
            os.flush();
            os.writeBytes("exit\n");
            os.flush();

            process.waitFor();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Logger.error(TAG, e);
                }
            }

            synchronized (LOCK) {
                if (inputStreamWriterThread != null && !inputStreamWriterThread.isInterrupted()) {
                    inputStreamWriterThread.interrupt();
                    inputStreamWriterThread = null;
                }

                if (errorStreamWriterThread != null && !errorStreamWriterThread.isInterrupted()) {
                    errorStreamWriterThread.interrupt();
                    errorStreamWriterThread = null;
                }

                if (process != null) {
                    process.destroy();
                    process = null;
                }

                finished = true;
            }
        }
    }

    public void stop() {
        Logger.trace(TAG, "stop() called");

        synchronized (LOCK) {
            if (inputStreamWriterThread != null && !inputStreamWriterThread.isInterrupted()) {
                inputStreamWriterThread.interrupt();
                inputStreamWriterThread = null;
            }

            if (errorStreamWriterThread != null && !errorStreamWriterThread.isInterrupted()) {
                errorStreamWriterThread.interrupt();
                errorStreamWriterThread = null;
            }

            if (process != null) {
                process.destroy();
                process = null;
            }

            finished = true;
        }
    }

    public boolean isFinished() {
        synchronized (LOCK) {
            return finished;
        }
    }

    /**
     * Добавляет строку к общему результату выполнения команды.
     *
     * @param line Строка
     */
    void appendNewLineToOutput(String line) {
        synchronized (this) {
            outputStringBuilder.append(line);
            outputStringBuilder.append('\n');
        }
    }

    /**
     * Возвращает результат выполнения команды.
     *
     * @return Вывод команды
     */
    public String getOutput() {
        return outputStringBuilder.toString();
    }

    /**
     * Считыватель потока данных из {@link Process}
     */
    private class OutputWriterThread extends Thread {

        private final InputStream inputStream;

        private OutputWriterThread(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    appendNewLineToOutput(s);
                }
            } catch (IOException e) {
                Logger.error(TAG, e);
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Logger.error(TAG, e);
                    }
                }
            }
        }
    }

    /**
     * Билдер для {@link ShellCommand}
     */
    public static class Builder {

        private String command;
        private EnumSet<ShellCommand.StdOutputStream> stdOutputStreams = EnumSet.of(StdOutputStream.STD_OUT, StdOutputStream.STD_ERR);
        private boolean runAsSu = false;


        public Builder(String command) {
            this.command = command;
        }

        /**
         * Устанавливает стандартные потоки вывода, из которых требуется получать результат.
         *
         * @param stdOutputStreams Стандартные потоки вывода,
         * @return Билдер
         */
        public Builder setStdOutputStreams(EnumSet<ShellCommand.StdOutputStream> stdOutputStreams) {
            this.stdOutputStreams = stdOutputStreams;
            return this;
        }

        /**
         * Устанавливает флаг необходимости запуска команды от имени суперпользователя.
         *
         * @param runAsSu {@code true}, если от имени суперпользователя, {@code false} иначе.
         * @return Билдер
         */
        public Builder setRunAsSu(boolean runAsSu) {
            this.runAsSu = runAsSu;
            return this;
        }

        /**
         * Возвращает объект {@link ShellCommand}
         *
         * @return Команда shell
         */
        public ShellCommand build() {
            return new ShellCommand(command, stdOutputStreams, runAsSu);
        }
    }
}
