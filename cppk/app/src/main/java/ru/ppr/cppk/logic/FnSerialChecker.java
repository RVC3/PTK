
package ru.ppr.cppk.logic;

import android.support.annotation.Nullable;

/**
 * Интерфейс для проверки номера фискального накопителя
 */
public interface FnSerialChecker {

    /**
     * Проверит изменился ли ФН
     *
     * @param fnSerialFromPrinter - номер ФН с принтера
     * @return true, если ФН не менялся
     */
    boolean check(@Nullable String fnSerialFromPrinter);

}
