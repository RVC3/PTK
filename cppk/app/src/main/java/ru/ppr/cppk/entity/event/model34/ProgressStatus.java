package ru.ppr.cppk.entity.event.model34;

import java.util.Arrays;
import java.util.List;

/**
 * Структура:
 * <pre>
 *             CREATED
 *               |
 *          PrePrinting
 *         |           |
 * CheckPrinted     Broken
 *      |
 *  Completed
 * </pre>
 */
public enum ProgressStatus {

    CREATED(0), //начальный статус, используется только при аннулировании
    Completed(1), //успешно завершено (то есть полноценная продажа) - выгружать в цод
    PrePrinting(2), //статус перед отправкой на ФР - грохнуть при закрытии смены
    CheckPrinted(64), //успешно напечатан - выгружать в цод
    /**
     * Статус после синхронизации чека, когда нам известно, что данный чек не лёг на фискальник
     */
    Broken(16);

    private int code;

    public static ProgressStatus get(int value) {
        for(ProgressStatus progressStatus : ProgressStatus.values())
            if(progressStatus.getCode() == value)
                return progressStatus;

        return null;
    }

    ProgressStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /** Вернет список статусов пригодных для выгрузки в ЦОД */
    public static List<ProgressStatus> getStatusesToCod() {
        return Arrays.asList(ProgressStatus.CheckPrinted, ProgressStatus.Completed);
    }

}
