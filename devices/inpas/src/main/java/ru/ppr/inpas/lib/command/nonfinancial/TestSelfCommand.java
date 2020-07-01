package ru.ppr.inpas.lib.command.nonfinancial;

import ru.ppr.inpas.lib.protocol.model.OperationCode;

/**
 * Операция используется для проверки соединения с POS терминалом.
 * Изначально указывается неверный ID POS терминала.
 * Если был получен результат, то он расценивается как успешный.
 * Данная команда не описана в документации от Inpas.
 * Основана на коде команды "Проверка соединения"
 *
 * @see OperationCode#TEST_HOST
 */
public class TestSelfCommand extends NonFinancialCommand {

    public TestSelfCommand() {
        setTerminalId("");
        setOperationCode(OperationCode.TEST_HOST.getValue());
    }

}