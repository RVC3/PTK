package ru.ppr.nsi.entity;


/**
 * числа - эта SmartCardType в таблице SmartCardStopListItem С карты мы читает
 * значения описанные в файле ТСОППД_структура_ПД
 * <p/>
 * 2015-07-17 переименован из SmartCardType в TicketStorageType вслед за тем что прходит в составе НСИ DCV 26
 *
 * @author Григорий
 */
public enum TicketStorageType {

    Unknown(0, ""),

    /**
     * Термолента для кассовых аппаратов 44 мм
     */
    Paper(1, ""),

    /**
     * Социальная карта москвича. 0x0C - не соответствует действительности. пока не понятно.
     */
    SKM(2, "СКМ"),

    /**
     * Универсальная электронная карта, нельготная
     */
    UEC(4, ""),

    /**
     * Универсальная электронная карта, льготная
     */
    UECExemption(5, ""),

    /**
     * Социальная карта жителя Московской области. 0x0C - не соответствует действительности. пока не понятно.
     */
    SKMO(6, "СКМО"),

    /**
     * Студенческая карта. 0x0C - не соответствует действительности. ИПК как и СКМ 0x30 и дальше уточняющая логика
     */
    IPK(7, "ИПК"),

    /**
     * Электронное транспортное требование
     */
    ETT(8, "ЭТТ"),

	/*
     * Архитектор Абрамов Андрей говорит, что ЭТТ2 не существует, удаляем этот
	 * тип //Электронное транспортное требование, с новой структурой хранения
	 * данных [Description("ЭТТ 2")] ETT2 = 9,
	 */

    /**
     * Электронная карта «Тройка»
     */
    TRK(10, "ТРК"),

    /**
     * Бесконтактная смарт-карта без счетчика, выпускаемая ЦППК. БСК НА ПЕРИОД
     */
    CPPK(11, "БСК"),

    /**
     * Бесконтактная смарт-карта со2 счетчиком, выпускаемая ЦППК
     * 33 для UltralightC, 35 для UltralightEV1
     */
    CPPKCounter(12, "БСК"),

    /**
     * БСК провожающего
     */
    SeeOfCard(13, "ПРВ"),

    /**
     * Электронный кошелек. Эмитирует ДТиРДТИ. Проездной билет, ресурс которого
     * представляется в рублях и расходуется по тарифам транспортных операторов.
     */
    EK(15, ""),

    /**
     * Карта для авторизации.
     */
    Service(16, "АВТ"),

    /**
     * Электронная карта «Стрелка»
     */
    STR(17, "СТР"),

    /**
     * Доплата(В НСИ такой нет)
     */
    FarePaper(1, "");


    /**
     * Числовой код Smart карты
     */
    private int dbCode;
    /**
     * Описание
     */
    private String abbreviation;

    TicketStorageType(int dbCode, String abbreviation) {
        this.dbCode = dbCode;
        this.abbreviation = abbreviation;
    }

    /**
     * type of SmartCard
     *
     * @return int
     */
    public int getDBCode() {
        return dbCode;
    }

    static public TicketStorageType getTypeByDBCode(int dbCode) {
        for (TicketStorageType type : TicketStorageType.values()) {
            if (type.getDBCode() == dbCode) {
                return type;
            }
        }
        return TicketStorageType.Unknown;
    }

    /**
     * abbreviation of SmartCard
     *
     * @return String
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Вернет флаг, должен ли этот тип карты иметь метку прохода
     */
    public boolean isMustHavePassageMark() {
        return this == CPPK || this == CPPKCounter || this == SeeOfCard || this == IPK;
    }

    /**
     * Вернет флаг, может ли ПТК перезаписать метку прохода на этой карте
     */
    public boolean isCanRewritePassageMark() {
        return this == CPPKCounter;
    }

    /**
     * Имеет ли этот тип карт эмисионные данные
     */
    public boolean isMustHaveEmissionDate() {
        return this == IPK || this == SKMO || this == SKM;
    }

    /**
     * Может ли ПТК записывать ПД на такую карту
     */
    public boolean isEnabledWriteToBsc() {
        return this == ETT || this == SKM || this == SKMO || this == IPK || this == TRK || this == CPPK || this == STR;
    }

}
