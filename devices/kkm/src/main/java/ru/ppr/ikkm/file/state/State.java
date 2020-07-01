package ru.ppr.ikkm.file.state;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import ru.ppr.ikkm.exception.SaveStateException;
import ru.ppr.ikkm.file.state.model.Check;
import ru.ppr.ikkm.file.state.model.Operator;
import ru.ppr.ikkm.file.state.model.ShiftInfo;

/**
 * Предоставляет интерфейс для взаимодействия с состоянием принтера
 *
 * Created by Артем on 21.01.2016.
 */
public interface State {

    /**
     * Сохраняет переданный чек, увеличивает СПДН номер
     *
     * @param check
     * @throws SaveStateException
     */
    void saveCheck(@NonNull Check check) throws SaveStateException;

    /**
     * Открывает смену.
     *
     * @param operator оператор, который производит открытие смены
     * @throws SaveStateException если произошла ошибка во время сохранения данных
     */
    void open(Operator operator) throws SaveStateException;

    /**
     * Закрывает смену.
     *
     * @throws SaveStateException если произошла ошибка во время сохранения данных
     */
    void close() throws SaveStateException;

    /**
     * Возвращает состояние смены: true если открыта, false если закрыта.
     *
     * @return
     */
    boolean isOpened();

    /**
     * Возвращает информацию о текущем операторе
     *
     * @return
     */
    Operator getOperator();

    /**
     * Задает оператора. ВНИМАНИЕ: НЕ ОТКРЫВАЕТ СМЕНУ.
     *
     * @throws SaveStateException если произошла ошибка во время сохранения данных
     */
    void setOperator(Operator operator) throws SaveStateException;

    /**
     * Возвращает СПДН последнего билета, если билетов нет, возвращает 0
     *
     * @return
     */
    int getLastSPDN();

    /**
     * Возвращает номер последней смены, если смен нет, то возвращает 0
     *
     * @return
     */
    int getLastShiftNum();

    /**
     * @param setHeadersLine
     * @throws SaveStateException если произошла ошибка во время сохранения данных
     */
    void setHeadersLine(List<String> setHeadersLine) throws SaveStateException;

    List<String> getHeaderLines();

    /**
     * Возвращает ККМ номер принтера
     *
     * @return
     */
    String getKkmNumber();

    /**
     * Устанавливает значение налоговой ставки
     *
     * @param index индекс
     * @param value значение от 1 до 999
     */
    void setVatValue(int index, int value) throws SaveStateException;

    /**
     * Возвращает значение налоговой ставки по индексу.
     * Если переданный индекс не попадает в зиапазон установленных, то вохвращается -1
     *
     * @param index
     * @return
     */
    int getVatValue(int index);

    /**
     * Возвращает дату на принтере
     *
     * @return
     */
    Date getPrinterDate();

    /**
     * Возвращает дату печати последнего чего, с точностью о минут
     *
     * @return
     */
    Date getLastCheckTime();

    /**
     * Возвращает ИНН принетра
     *
     * @return
     */
    String getInn();

    /**
     * Возвразает регистрационный номер (РН) принетра
     *
     * @return
     */
    String getRegisterNumber();

    /**
     * Возвращает время открытия смены
     *
     * @return дата открытия, либо null если смена не было
     */
    @Nullable
    Date getOpenDate();

    /**
     * Возвращает номер ЭКЛЗ
     *
     * @return
     */
    String getEklz();

    /**
     * Возвращает сумму по фискальнику за последнюю смену
     *
     * @return
     */
    BigDecimal getTotalForShift();

    /**
     * Возвращает всю сумму по фискальнику
     *
     * @return
     */
    BigDecimal getTotal();

    /**
     * Возвращает длину израсходованной ленты
     *
     * @return
     */
    long getOdometerValue();

    /**
     * Возвращает количество оставшихся свободных записей в фискальной памяти
     *
     * @return
     */
    long getAvailableSpaceForDocs();

    /**
     * Возвращает количество оставшихся свободных записей(смен) в фискальной памяти
     *
     * @return
     */
    long getAvailableSpaceForShifts();

    /**
     * Добавляет количество израсходованной ленты к существующему
     *
     * @param millimeter количество израсходованной ленты в милиметрах
     */
    void appendOdometrValue(long millimeter);

    /**
     * Уменьшает показание счетчика доступных документов
     */
    void decrementAvailableSpaceForDocs() throws SaveStateException;

    /**
     * Уменьшает показание счетчика доступных смен
     */
    void decrementAvailableSpaceForShifts() throws SaveStateException;

    /**
     * Возвращает инфомрмацию по смена за промежуток времени
     *
     * @param startDate время с которого необходимо получить информацию,
     *                  если null то время начала не учитывается
     * @param endDate   время до которого необходимо получить информацию,
     *                  если null то время окончания не учитывается
     * @return неизменяемый список с информацией по сменам
     */
    List<ShiftInfo> getShiftInfo(@Nullable Date startDate, @Nullable Date endDate);

    /**
     * Производит очистку ресурсов, используемых для состояния. Необходимо вызывать когда завершаем
     * работу с принтером.
     */
    void closeState();
}
