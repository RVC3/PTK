package ru.ppr.ikkm.file.state.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.ppr.ikkm.exception.SaveStateException;
import ru.ppr.ikkm.file.state.model.Check;
import ru.ppr.ikkm.file.state.model.Operator;
import ru.ppr.ikkm.file.state.model.PrinterSettings;
import ru.ppr.ikkm.file.state.model.Shift;

/**
 * Предоставляет интерфейс для сохранения данных в хранилище состояния.
 *
 * Created by Артем on 21.01.2016.
 */
public interface PrinterStateStorage {

    /**
     * Добавляет чек в текущую смену
     *
     * @param check
     * @throws SaveStateException если произошла ошибка во время сохранения данных
     */
    void addCheck(@NonNull Check check) throws SaveStateException;

    /**
     * Сохраняет оператора
     *
     * @param operator
     * @throws SaveStateException если произошла ошибка во время сохранения данных
     */
    void setOperator(@NonNull Operator operator) throws SaveStateException;

    /**
     * Возвращает последюю смену
     *
     * @return
     */
    @Nullable
    Shift getShift();

    /**
     * Сохраняет или обновляет инфомрацию о смене
     *
     * @param shift
     * @throws SaveStateException если произошла ошибка во время сохранения данных
     */
    void saveShift(Shift shift) throws SaveStateException;

    /**
     * Возвращает смены за все время на принетере
     *
     * @return
     */
    List<Shift> getAllShift();

    /**
     * Возвращает настройки принтера. Если настроек нет, то создает дефолтные настройки
     *
     * @return
     */
    @NonNull
    PrinterSettings getPrinterSetting();

    /**
     * Сохраняет настройки принетра
     *
     * @param printerSettings
     * @throws SaveStateException если произошла ошибка во время сохранения данных
     */
    void savePrinterSetting(@NonNull PrinterSettings printerSettings) throws SaveStateException;

    /**
     * Производит закрытие хранилища состояния принтера
     */
    void close();
}
