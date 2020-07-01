package ru.ppr.inpas.lib.command.nonfinancial;

import ru.ppr.inpas.lib.protocol.model.OperationCode;

/**
 * Операция используется для проверки соединения от POS терминала до хоста.
 * Код операции: 26.
 */
public class TestHostCommand extends NonFinancialCommand {

    public TestHostCommand() {
        setOperationCode(OperationCode.TEST_HOST.getValue());
    }

}