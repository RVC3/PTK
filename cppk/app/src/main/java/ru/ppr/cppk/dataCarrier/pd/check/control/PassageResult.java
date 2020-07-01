package ru.ppr.cppk.dataCarrier.pd.check.control;

/**
 * Перечисление типов ошибок при проверке ПД
 * Результат прохода по ПД (контроля ПД)
 *
 * @author A.Ushakov
 */
public enum PassageResult {

    /**
     * Ошибка. Не валиден. Причина не известна
     */
    Unknown(-1),

    /**
     * Ошибок нет. Билет прошел проверку
     */
    SuccesPassage(0),

    /**
     * Попытка повторного прохода
     */
    BannedByRePass(10),

    /**
     * Попытка прохода по аннулированному ПД
     */
    BannedByCanceled(20),

    /**
     * Билет находится в стоп-листе
     */
    BannedByStopListTickets(40),

    /**
     * Карта находится в стоп-листе
     */
    BannedByStopListCards(50),

    /**
     * Билет не действует на данном маршруте(Несоответствие станции)
     */
    InvalidStation(60),

    /**
     * Тариф не найден
     */
    TariffNotFound(90),

    /**
     * Некорректная категория поезда
     */
    BannedTrainType(110),

    /**
     * Дата действия еще не наступила Срок действия не наступила
     */
    TooEarly(120),

    /**
     * Срок действия истек
     */
    TooLate(130),

    /**
     * ПД действует только в выходные дни
     */
    WeekendOnly(140),

    /**
     * ПД действует только в будни
     */
    WorkingDayOnly(150),

    /**
     * Абонемент не действителен. Количество поездок израсходовано
     */
    NoTrips(160),

    /**
     * Ошибка ЭЦП
     */
    InvalidSign(170),

    /**
     * Некорректная метка прохода(метка прохода устрела)
     */
    PassMarkOutOfDate(190),

    /**
     * Маршрут не подтвержден
     */
    RouteNotFound(200),

    /**
     * Ключ ЭЦП отозван
     */
    SignKeyRevoked(210),

    /**
     * Ошибка записи метки прохода
     */
    FailedToWritePassMark(230);

    private final int code;

    PassageResult(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PassageResult getPassageResultByCode(int code) {
        //return new Pass
        PassageResult result = PassageResult.Unknown;
        for (PassageResult passageResult : PassageResult.values()) {
            if (passageResult.getCode() == code) {
                result = passageResult;
                break;
            }
        }
        return result;
    }

}
