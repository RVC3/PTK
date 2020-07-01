package ru.ppr.inpas.lib.parser.model;

import android.support.annotation.NonNull;

/**
 * Модель чека.
 */
public class Receipt {
    // Look at SaleResult.
//Receipt Data:           0xDF^^           ДЕМО РЕЖИМ
//                        Программный продукт
//                        ПРОЦЕССИНГОВЫЙ ЦЕНТР
//                        МОСКВА УЛ.ОКТЯБРЬСКАЯ Д.72
//                        Т. 721-36-21
//
//                        ЧЕК КЛИЕНТА                 0007
//                        ОПЛАТА ПОКУПКИ
//                        29.06.16                18:23:15
//                        ТЕРМИНАЛ:               40000116
//
//                        КАРТА               VISA Classic
//                                **** **** **** **** 0884
//                        СРОК ДЕЙСТВИЯ :            25/10
//
//                        СУММА (RUB)
//                        0.01
//                        ОДОБРЕНО
//
//
//                        КОД ОТВЕТА                    00
//                        КОД АВТОРИЗАЦИИ:          165153
//                        № ССЫЛКИ:           856666681053
//

    private String mReceiptNumber;

    /**
     * Метод возвращающий номер чека.
     *
     * @return номер чека.
     */
    public String getReceiptNumber() {
        return mReceiptNumber;
    }

    /**
     * Метод для установки образа чека.
     *
     * @param number номер чека.
     */
    public void setReceiptNumber(@NonNull final String number) {
        mReceiptNumber = number;
    }

}