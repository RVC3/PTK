package ru.ppr.inpas.lib.command.nonfinancial;

import ru.ppr.inpas.lib.protocol.model.CustomMode;
import ru.ppr.inpas.lib.protocol.model.OperationCode;
import ru.ppr.inpas.lib.protocol.model.SaField;

/**
 * Пользовательская команда 21 «Запрос полного отчета»
 * Код операции: 63.
 */
public class TransactionJournalCommand extends NonFinancialCommand {

    public TransactionJournalCommand() {
        setOperationCode(OperationCode.CUSTOM_COMMAND.getValue());
        mSaPacket.putInteger(SaField.SAF_CMD_MODE2, CustomMode.FULL_REPORT.getValue());
    }

}
