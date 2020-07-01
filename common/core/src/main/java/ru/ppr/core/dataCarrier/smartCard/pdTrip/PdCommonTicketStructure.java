package ru.ppr.core.dataCarrier.smartCard.pdTrip;

public class PdCommonTicketStructure {
        // Размер ПД в байтах
        static final int PD_SIZE = 16;
        // Размер данных ПД в байтах
        static final int PD_DATA_SIZE = 12;
        // Дата и время окончания действия услуги (минут от даты кодирования)
        static final int SERVICE_EXP_DATE_TIME_INDEX = 0;
        static final int SERVICE_EXP_DATE_TIME_LEGHT = 20;
        // Дата и время начала поездки по маршруту
        static final int DATE_TIME_START_TRIP_INDEX = 20;
        static final int DATE_TIME_START_TRIP_LEGHT = 20;
        // Время пересадки (в МСК: ММ/ММТС/МЦК/МЦД МСК)
        static final int TRANSPLANT_TIME_INDEX = 40;
        static final int TRANSPLANT_TIME_LEGHT = 7;
        // Время последнего прохода (минут от «времени начала поездки» или «времени пересадки»)
        static final int LASS_PAS_TIME_INDEX = 47;
        static final int LASS_PAS_TIME_LEGHT = 7;
        //Число оставшихся / совершенных поездок
        static final int NUMBER_REMAINING_PERFORMED_TRIPS_INDEX= 54;
        static final int NUMBER_REMAINING_PERFORMED_TRIPS_LEGHT = 7;
        //Номер валидатора/вестибюля
        static final int VALIDATOR_LOBBY_NUMBER_INDEX = 61;
        static final int VALIDATOR_LOBBY_NUMBER_LEGHT = 16;
        // Признак аннулирования
        static final int SIGN_CANCALLETION_INDEX = 77;
        static final int SIGN_CANCALLETION_LEGHT = 1;
        // Активный тип билета
        static final int ACTIVE_TICKET_TYPE_INDEX = 78;
        static final int ACTIVE_TICKET_TYPE_LEGHT = 1;
        // Число оставшихся проходов за день
        static final int NUMBER_REMAINING_PASSES_DAY_INDEX = 79;
        static final int NUMBER_REMAINING_PASSES_DAY_LEGHT = 5;
        // Код маршрута
        static final int ROUTE_CODE_INDEX = 84;
        static final int ROUTE_CODE_LEGHT = 12;
        // Резерв
        static final int RESERVE_INDEX = 96;
        static final int RESERVE_LEGHT = 0;
        // Специальный код
        static final int SPECIAL_CODE_INDEX = 96;
        static final int SPECIAL_CODE_LEGHT = 32;
    }
