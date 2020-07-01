package ru.ppr.ingenico.model.operations;

import java.util.Locale;

import ru.ppr.ingenico.model.responses.Response;
import ru.ppr.ingenico.utils.Arcus2Utils;
import ru.ppr.ipos.model.FinancialTransactionResult;

/**
 * Created by Dmitry Nevolin on 16.11.2015.
 */
public class OperationSale extends OperationResultFinancialTransaction {
    /**
     * Таймаут на выполнение опреации продажи, мс
     */
    private static final long OPERATION_TIMEOUT = 60000;
    /**
     * Сумма оплаты
     */
    private final int price;
    /**
     * Локальный ID транзакции
     */
    private final long localTransactionId;
    /**
     * Флаг, что серверу уже известно о нашей транзакции
     */
    private boolean transactionKnownForServer;
    /**
     * Колбек для операции
     */
    private final Callback callback;

    public OperationSale(int price, long localTransactionId, Callback callback) {
        super(0, 128);
        this.price = price;
        this.localTransactionId = localTransactionId;
        this.callback = callback;
    }

    @Override
    protected byte[] sum() {
        return Arcus2Utils.convertStringToBytes(String.format(Locale.US, "%.2f", price == 0 ? 0f : price / 100f));
    }

    @Override
    protected Response pingResponse() {
        return RESPONSE_OK;
    }

    @Override
    protected Response printResponse() {
        // В этой точке мы понимаем, что сервер знает о данной транзакции,
        // но тем не менее, дальше что-то всё ещё может пойти не так.
        // Нам нужно сохранить у себя флаг, знает ли сервер о тразакции
        // для последующего корректного выполнения техничесой отмены
        if (!transactionKnownForServer) {
            transactionKnownForServer = true;

            callback.onTransactionKnownForServer();
        }

        return RESPONSE_OK;
    }

    @Override
    protected Response set_tagsResponse() {
        // Идентифицировать что мы понимаем, что сервер знает о данной транзакции
        // только после первого PRINT недостаточно, появились случаи, когда PRINT
        // не приходили вообще, см. http://agile.srvdev.ru/browse/CPPKPP-34288
        // поэтому теперь идентефицируем и тут, на случай если PRINT не придет
        if (!transactionKnownForServer) {
            transactionKnownForServer = true;

            callback.onTransactionKnownForServer();
        }

        return RESPONSE_OK;
    }

    @Override
    protected Response store_rcResponse() {
        // Идентифицировать что мы понимаем, что сервер знает о данной транзакции
        // только после первого PRINT недостаточно, появились случаи, когда PRINT
        // не приходили вообще, см. http://agile.srvdev.ru/browse/CPPKPP-34288
        // поэтому теперь идентефицируем и тут, на случай если PRINT не придет
        if (!transactionKnownForServer) {
            transactionKnownForServer = true;

            callback.onTransactionKnownForServer();
        }

        return RESPONSE_OK;
    }

    @Override
    protected Response end_trResponse() {
        complete();

        return RESPONSE_OK;
    }

    @Override
    public FinancialTransactionResult getResult() {
        FinancialTransactionResult result = super.getResult();
        if (result != null) {
            int id = result.getId();
            callback.onServerTransactionIdKnown(id);
        }
        return result;
    }

    @Override
    public long getTimeout() {
        return OPERATION_TIMEOUT;
    }

    public interface Callback {
        void onTransactionKnownForServer();

        void onServerTransactionIdKnown(int serverTransactionId);
    }
}
