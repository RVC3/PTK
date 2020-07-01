package ru.ppr.security.entity;

/**
 * Список разрешений для разных ролей ПТК [PermissionDvc].Code
 */
public enum PermissionDvc {

    /**
     * Неизвестная функция
     */
    Unknown("Ptk_Unknown"),

    /**
     * Авторизация
     */
    Auth("Ptk_Auth"),

    /**
     * Резервная полная копия
     */
    CreateFullBackup("Ptk_CreateFullBackup"),

    /**
     * Обновление ПО
     */
    UpdatePo("Ptk_UpdatePo"),

    /**
     * Обновление НСИ
     */
    UpdateNsi("Ptk_UpdateNsi"),

    /**
     * Обновление стоп-листов
     */
    UpdateStoplist("Ptk_UpdateStoplist"),

    /**
     * Обновление белого списка ПД
     */
    UpdateWhiteList("Ptk_UpdateWhiteList"),

    /**
     * Обновление пакета открытых ключей и файлов конфигурации
     */
    UpdateKeysAndConfigFiles("Ptk_UpdateKeysAndConfigFiles"),

    /**
     * Открытие смены
     */
    OpenShift("Ptk_OpenShift"),

    /**
     * Продажа ПД без места
     */
    SalePd("Ptk_SalePd"),

    /**
     * Продажа ПД без места по доплате
     */
    SalePdSurchange("Ptk_SalePdSurchange"),

    /**
     * Продажа трансферов
     */
    SaleTransfer("Ptk_SaleTransfer"),

    /**
     * Аннулирование
     */
    Annulment("Ptk_Annulment"),

    /**
     * Продажа квитанции на багаж
     */
    SaleBaggage("Ptk_SaleBaggage"),

    /**
     * Взимание штрафа
     */
    FineSale("Ptk_FineSale"),

    /**
     * Чтение информации с БСК/NFC
     */
    ReedBskNfc("Ptk_ReedBskNfc"),

    /**
     * Чтение информации с ПД по штрих-коду
     */
    ReedPdBarcode("Ptk_ReedPdBarcode"),

    /**
     * Контроль ПД с местом
     */
    ControlPdPlace("Ptk_ControlPdPlace"),

    /**
     * Контроль ПД других операторов (ЭК)
     */
    ControlPdOverOper("Ptk_ControlPdOverOper"),

    /**
     * Получение списка электронной регистрации
     */
    GetEtickets("Ptk_GetEtickets"),

    /**
     * Закрытие смены
     */
    CloseShift("Ptk_CloseShift"),

    /**
     * Закрытие дня на терминале
     */
    ClosePOSTerminalDay("Ptk_ClosePOSTerminalDay"),

    /**
     * Закрытие месяца
     */
    CloseMonth("Ptk_CloseMonth"),

    /**
     * Настройки IPos-терминала
     */
    PosTerminal("Ptk_PosTerminal"),

    /**
     * Формирование отчетности
     */
    CreateReporting("Ptk_CreateReporting"),

    /**
     * Пробный ПД
     */
    TestPd("Ptk_TestPd"),

    /**
     * Пробная сменная ведомость
     */
    TestShiftShit("Ptk_TestShiftShit"),

    /**
     * Льготная сменная ведомость
     */
    DiscountedShiftShit("Ptk_DiscountedShiftShit"),

    /**
     * Сменная ведомость
     */
    ShiftSheet("Ptk_ShiftSheet"),

    /**
     * Ведомость гашения смены и z-отчет
     */
    SheetShiftBlankingZreport("Ptk_SheetShiftBlankingZreport"),

    /**
     * Льготная месячная ведомость
     */
    DiscountedMonthlySheet("Ptk_DiscountedMonthlySheet"),

    /**
     * Печать пробной месячной ведомости
     */
    TestMonthlySheet("Ptk_TestMonthlySheet"),

    /**
     * Месячная ведомость
     */
    MonthlySheet("Ptk_MonthlySheet"),

    /**
     * Ведомость гашения месяца
     */
    SheetBlankingMonth("Ptk_SheetBlankingMonth"),

    /**
     * Печать контрольного журнала
     */
    PrintControlJournal("Ptk_PrintControlJournal"),

    /**
     * Печать журнала оформления по ЭТТ
     */
    JournalETT("Ptk_JournalETT"),

    /**
     * Печать отчета по операциям POS-терминала
     */
    ReportPOSOperations("Ptk_ReportPOSOperations"),

    /**
     * Печать отчета о закрытии дня на POS-терминале
     */
    ReportPOSCloseDay("Ptk_ReportPOSCloseDay"),

    /**
     * Печать месячного отчета по операциям POS-терминала
     */
    ReportPOSMonth("Ptk_ReportPOSMonth"),

    /**
     * Передача данных о продажах и контроле ПД
     */
    TransferSalesControlPd("Ptk_TransferSalesControlPd"),

    /**
     * Информационные сервисы
     */
    InfoService("Ptk_InfoService"),

    /**
     * Учет билетной ленты
     */
    AccountingTicketTape("Ptk_AccountingTicketTape"),

    /**
     * прием оплаты за ПД
     */
    getPayForPd("Ptk_getPayForPd"),

    /**
     * Изменение кода дня
     */
    ChangeDayCode("Ptk_ChangeDayCode"),

    /**
     * Изменение даты и времени <= 5 минутам (а на само деле не 5 минутам а
     * времени заданным в общих настройках ПТК)
     */
    ChangeDateTime("Ptk_ChangeDateTime"),

    /**
     * Изменение даты и времени > 5 минут (а на само деле более чем на время
     * разрешенное в общих настройках ПТК)
     */
    ChangeDateTimeMoreThen5Minutes("Ptk_ChangeDateTimeMoreThen5Min"),

    /**
     * Изменение категории поезда контроля
     */
    ChangeTrainCategoryCode("Ptk_ChangeTrainCategoryCode"),

    /**
     * Изменение Участка обслуживания
     */
    ChangeWorkingPlace("Ptk_ChangeWorkingPlace"),

    /**
     * Изменение Станции привязки ПТК
     */
    ChangeBindingStation("Ptk_ChangeFinancialStation"),

    /**
     * Активация режима. Работа ПТК в режиме «Мобильной кассы» на выход
     */
    ActivateMobileCashierMode("Ptk_ActivateMobileCashierMode"),

    /**
     * Синхронизация времени. Включение/отключение функционала автоматической
     * синхронизации времени в ПО ПТК
     */
    TimeSync("Ptk_TimeSync"),

    /**
     * Разрешение на превышение допустимого периода изменения времени.
     * Включение/отключение возможности в рамках синхронизации времени в ПО ПТК
     * изменить время на период, превышающий разрешенный (в настоящее время –
     * более 5 минут)
     */
    ChangeAutoTimeSync("Ptk_ChangeAutoTimeSync"),

    /**
     * Период времени для вывода предупреждений о закрытии смены. Указание
     * периода времени, за который на экране ПТК выведено предупреждение о
     * необходимости закрытия смены
     */
    ChangeTimeToCloseShiftMessage("Ptk_ChangeTimeToCloseShiftMsg"),

    /**
     * Время на аннулирование ПД. Настройка доступного времени аннулирования
     */
    ChangeTimeForAnnulate("Ptk_ChangeTimeForAnnulate"),

    /**
     * Изменение срока действия стоп-листов, дни
     */
    StopListValidDays("Ptk_StopListValidDays"),

    /**
     * Доступность кнопки настройки wi-fi
     */
    ConfigWiFi("Ptk_ConfigWiFi"),

    /**
     * Доступность кнопки настройки фискальника
     */
    ConfigFiscalRegister("Ptk_ConfigFiscalRegister"),

    /**
     * Доступность кнопки настройки pos-терминала (POS)
     */
    ConfigPosTerminal("Ptk_ConfigPosTerminal"),

    /**
     * Доступность кнопки Тест хоста (POS)
     */
    TestHost("Ptk_TestHost"),

    /**
     * Доступность кнопки Меню администратора (POS)
     */
    AdministratorMenu("Ptk_AdministratorMenu"),

    /**
     * Доступность кнопки "Отчет о непереданных ФД"
     */
    PrintNotSentDocsSheet("Ptk_PrintNotSentDocsSheet"),

    /**
     * Возможность редактировать список штрафов
     */
    FineListEdit("Ptk_FineListEdit"),

    /**
     * Изменение режима контроля
     */
    ChangeControlMode("Ptk_ChangeControlMode"),

    /**
     * Изменение маршрута контроля в режиме трансфера
     */
    ChangeTransferRoute("Ptk_ChangeTransferRoute"),

    /**
     * Изменение разрешения на продажу трансфера
     */
    ChangeTransferSellingPossibility("Ptk_ChangeTransferSell");

    private String code;

    PermissionDvc(String code) {
        this.code = code;
    }

    public static PermissionDvc getPermissionDvc(String code) {
        for (PermissionDvc type : PermissionDvc.values())
            if (type.getCode().equals(code))
                return type;

        return PermissionDvc.Unknown;
    }

    public String getCode() {
        return code;
    }
}