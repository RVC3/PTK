package ru.ppr.inpas.lib.command.financial;

import android.support.annotation.NonNull;

import java.util.Map;

import ru.ppr.inpas.lib.command.BaseCommand;
import ru.ppr.inpas.lib.protocol.model.OperationCode;
import ru.ppr.inpas.lib.protocol.model.SaField;

/**
 * Операция «Аварийная отмена».
 * Предназначена для отмены последней операции, отправленной на выполнение на POS-терминал.
 * Операция «Аварийная отмена» должна проводиться только для операций со «Статус проведения транцакции» (поле 39) со значением «1».
 * При выполнении аварийной отмены ККМ заменяет код оригинальной операции на код аварийной отмены, все остальные данные запроса не меняются.
 * Код операции: 53.
 */
public class EmergencyCancellationCommand extends FinancialCommand {

    public EmergencyCancellationCommand(@NonNull final BaseCommand cmd) {
        for (Map.Entry<SaField, byte[]> entry : cmd.getPacket().getParams().entrySet()) {
            mSaPacket.putBytes(entry.getKey(), entry.getValue());
        }

        setOperationCode(OperationCode.EMERGENCY_CANCELLATION.getValue());
    }

}
