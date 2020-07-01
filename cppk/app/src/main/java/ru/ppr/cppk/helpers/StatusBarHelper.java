package ru.ppr.cppk.helpers;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import ru.ppr.logger.Logger;
import ru.ppr.utils.Empty;
import ru.ppr.utils.ShellCommand;
import rx.Single;
import rx.Subscription;

/**
 * Класс-помощник для включения и отключения статус-бара.
 *
 * @author Aleksandr Brazhkin
 */
public class StatusBarHelper {

    private static final String TAG = Logger.makeLogTag(StatusBarHelper.class);

    /**
     * Выполняет попытку отключения статус-бара.
     *
     * @param timeout Время на выполнение операции, мс.
     * @return {@link Single} с результатом выполнения операции.
     */
    public Single<Empty> disableStatusBar(int timeout) {
        return Single
                .create((Single.OnSubscribe<Empty>) singleSubscriber -> {

                    String command = "service call activity 42 s16 com.android.systemui";
                    ShellCommand shellCommand = new ShellCommand.Builder(command).setRunAsSu(true).build();

                    singleSubscriber.add(new Subscription() {
                        @Override
                        public void unsubscribe() {
                            shellCommand.stop();
                        }

                        @Override
                        public boolean isUnsubscribed() {
                            return shellCommand.isFinished();
                        }
                    });

                    try {
                        shellCommand.run();
                        String shellCommandOutput = shellCommand.getOutput();
                        Logger.trace(TAG, "disableStatusBar output:\n" + shellCommandOutput);
                        if (shellCommandOutput.contains("Parcel")) {
                            singleSubscriber.onSuccess(Empty.INSTANCE);
                        } else {
                            singleSubscriber.onError(new Throwable("Could not disable status bar"));
                        }
                    } catch (IOException | InterruptedException e) {
                        Logger.error(TAG, e);
                        singleSubscriber.onError(e);
                    }
                })
                .timeout(timeout, TimeUnit.MILLISECONDS, SchedulersCPPK.background());
    }

    /**
     * Выполняет попытку включения статус-бара.
     *
     * @param timeout Время на выполнение операции, мс.
     * @return {@link Single} с результатом выполнения операции.
     */
    public Single<Empty> enableStatusBar(int timeout) {
        return Single
                .create((Single.OnSubscribe<Empty>) singleSubscriber -> {

                    String command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib am startservice -n com.android.systemui/.SystemUIService";
                    ShellCommand shellCommand = new ShellCommand.Builder(command).setRunAsSu(true).build();

                    singleSubscriber.add(new Subscription() {
                        @Override
                        public void unsubscribe() {
                            shellCommand.stop();
                        }

                        @Override
                        public boolean isUnsubscribed() {
                            return shellCommand.isFinished();
                        }
                    });

                    try {
                        shellCommand.run();
                        Logger.trace(TAG, "enableStatusBar output:\n" + shellCommand.getOutput());
                        singleSubscriber.onSuccess(Empty.INSTANCE);
                    } catch (IOException | InterruptedException e) {
                        Logger.error(TAG, e);
                        singleSubscriber.onError(e);
                    }
                })
                .timeout(timeout, TimeUnit.MILLISECONDS, SchedulersCPPK.background());
    }
}
