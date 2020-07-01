package ru.ppr.cppk.entity.settings;

/**
 * Класс типы отчетов ПТК
 *
 * @author Григорий
 */
public enum ReportType {

    /**
     * разовый пробный
     */
    TestPd(1, "РАЗОВЫЙ ПРОБНЫЙ"),

    /**
     * пробная сменная ведомость
     */
    TestShiftShit(2, "ПРОБНАЯ СМЕННАЯ ВЕДОМОСТЬ"),

    /**
     * льготная сменная ведомость
     */
    DiscountedShiftShit(3, "ЛЬГОТНАЯ СМЕННАЯ ВЕДОМОСТЬ"),

    /**
     * ведомость по ЭТТ
     */
    EttShit(4, "Ведомость по ЭТТ"),

    /**
     * сменная ведомость
     */
    ShiftShit(5, "СМЕННАЯ ВЕДОМОСТЬ"),

    /**
     * ведомость гашения смены
     */
    SheetShiftBlanking(6, "ВЕДОМОСТЬ ГАШЕНИЯ СМЕНЫ"),

    /**
     * контрольный журнал
     */
    AuditTrail(7, "КОНТРОЛЬНЫЙ ЖУРНАЛ"),

    /**
     * льготная месячная ведомость
     */
    DiscountedMonthlySheet(8, "ЛЬГОТНАЯ МЕСЯЧНАЯ ВЕДОМОСТЬ"),

    /**
     * месячная ведомость
     */
    MonthlySheet(9, "МЕСЯЧНАЯ ВЕДОМОСТЬ"),

    /**
     * ведомость гашения месяца
     */
    SheetBlankingMonth(10, "ВЕДОМОСТЬ ГАШЕНИЯ МЕСЯЦА"),

    /**
     * ведомость гашения месяца
     */
    BTMonthlySheet(11, "МЕСЯЧНЫЙ ОТЧЁТ ПО ОПЕРАЦИЯМ POS-ТЕРМИНАЛА"),

    /**
     * ведомость гашения месяца
     */
    TestMonthShit(12, "ПРОБНАЯ МЕСЯЧНАЯ ВЕДОМОСТЬ"),

    /**
     * Журнал оформления по ЭТТ
     */
    SalesForEttLog(13, "ЖУРНАЛ ОФОРМЛЕНИЯ ПО ЭТТ");


    /**
     * Код документа
     */
    private int code;
    /**
     * Наимнование документа
     */
    private String name;

    ReportType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Метод возвращает код документа.
     */
    public int getCode() {
        return code;
    }

    /**
     * Метод возвращает наимнование документа.
     */
    public String getName() {
        return name;
    }

    /**
     * Метод возвращает тип документа по коду.
     */
    public static ReportType getByCode(int code) {

        for (ReportType type : ReportType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

    /**
     * Метод возвращает тип документа по наименованитю документа.
     */
    public static ReportType getByVarName(String name) {
        for (ReportType type : ReportType.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return null;
    }

}
