package ru.ppr.inpas.lib.command.nonfinancial;

import ru.ppr.inpas.lib.protocol.model.OperationCode;

/**
 * Операция «Сверка итогов»
 * Cлужит для сверки итогов работы пин-пада (POS-терминала) и хоста банка по операциям с безналичным расчетом.
 * После выполнения Сверки, выполнение операции Отмена станет невозможным, т.к. БД пин-пада (POS-терминала) будет очищена.
 * Код операции: 59.
 */
public class CloseSessionCommand extends NonFinancialCommand {

    public CloseSessionCommand(final int transactionNumber) {
        setCurrentDate();
        setOperationCode(OperationCode.CLOSE_SESSION.getValue());
        setTransactionNumber(transactionNumber);
    }

}
