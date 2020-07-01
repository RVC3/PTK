package ru.ppr.shtrih;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import ru.ppr.ikkm.model.OfdDocsState;
import ru.ppr.logger.Logger;

/**
 * Класс для проверки необходимости отправки данных в ОФД.
 *
 * @author Aleksandr Brazhkin
 */
public class SyncChecker {

    private static final String TAG = Logger.makeLogTag(SyncChecker.class);

    private final ConfigProvider configProvider;

    public SyncChecker(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    /**
     * Провряет необходимость включения отправки данных в ОФД.
     *
     * @return {@code true} если требуется отправкаданных в ОФД, {@code false} иначе
     * @throws Exception В случае ошибки выполнения операции
     */
    boolean isSyncRequired(PrinterShtrih printer) throws Exception {
        // Получаем информацию с принтера
        OfdDocsState ofdDocsState = printer.getOfdDocsStateImpl();

        // Проверяем количество документов
        int docsCount = ofdDocsState.getUnsentDocumentsCount();
        if (docsCount == 0) {
            Logger.trace(TAG, "Sync doesn't required: unsent documents count = 0");
            return false;
        }
        if (docsCount >= configProvider.getCountTrigger()) {
            Logger.trace(TAG, "Sync required: unsent documents count = " + docsCount);
            return true;
        }

        // Проверяем время печати самого старого документа
        Date firstDocDateTime = ofdDocsState.getFirstUnsentDocumentDateTime();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+3"));
        calendar.setTime(firstDocDateTime);
        calendar.add(Calendar.HOUR_OF_DAY, configProvider.getPeriodTrigger());

        if (calendar.getTime().before(new Date())) {
            Logger.trace(TAG, "Sync required: first doc date = " + firstDocDateTime.toString());
            return true;
        }

        Logger.trace(TAG, "Sync doesn't required");

        return false;
    }


    public interface ConfigProvider {
        /**
         * Количество документов для включения отправки данных в ОФД.
         */
        int getCountTrigger();

        /**
         * Срок хранения документов в ФП для включения отправки данных в ОФД (в часах).
         */
        int getPeriodTrigger();
    }
}
