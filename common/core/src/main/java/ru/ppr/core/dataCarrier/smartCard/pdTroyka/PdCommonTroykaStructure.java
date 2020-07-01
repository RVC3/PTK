package ru.ppr.core.dataCarrier.smartCard.pdTroyka;

public class PdCommonTroykaStructure {
        // Размер ПД в байтах
        static final int PD_SIZE = 16;
        // Размер данных ПД в байтах
        static final int PD_DATA_SIZE = 12;
/*        // Код приложения
        static final int CODE_APP_INDEX = 0;
        static final int CODE_APP_LENGTH = 10;*/
        // Тип билета №1 CRDCODE
        static final int TYPE_TICKET_1_INDEX = 10;
        static final int TYPE_TICKET_1_LENGTH = 10;
/*        // Номер билета
        static final int NUMBER_TICKET_INDEX = 20;
        static final int NUMBER_TICKET_LENGTH = 32;*/
        // Формат кодирования
        static final int CODING_FORMAT_VALUE = 14; //стандартное значение
        static final int FORMAT_ENCODING_INDEX = 52;
        static final int FORMAT_ENCODING_LENGTH = 4;
        //Расширения номера формата
        public static final int EXTEND_NUM_FORMAT_VALUE = 6;//стандартное значение
        static final int EXTENSIONS_NUMBER_FORMAT_INDEX = 56;
        static final int EXTENSIONS_NUMBER_FORMAT_LENGT = 5;
        //Тип билета №2 CRDCODE
        static final int TYPE_TICKET_2_INDEX = 61;
        static final int TYPE_TICKET_2_LENGTH = 10;
/*        // Срок годности БСК (Количество дней от 31.12.2018 (до 2041 года))
        static final int SHELF_LIFE_BSK_INDEX = 71;
        static final int SHELF_LIFE_BSK_LENGTH = 13;
       // Тип бланка
        static final int TYPE_BLANK_INDEX = 84;
        static final int TYPE_BLANK_LENGTH = 10;*/
        // Дата и время последнего кодирования DateTime.Now
        static final int DATE_TIME_NOW_INDEX = 94;
        static final int DATE_TIME_NOW_LEGTH = 23;
/*         // Счетчик пополнения +1
        static final int TOP_UP_COUNTER_INDEX = 117;
        static final int TOP_UP_COUNTER_LEGTH = 10;
        // Резерв
        static final int RESERV_INDEX = 127;
        static final int RESERV_LEGTH = 1;*/
    }
