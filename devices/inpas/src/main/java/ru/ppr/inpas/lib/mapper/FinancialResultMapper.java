package ru.ppr.inpas.lib.mapper;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ru.ppr.inpas.lib.parser.ReceiptFieldParser;
import ru.ppr.inpas.lib.parser.ReceiptParser;
import ru.ppr.inpas.lib.parser.model.Receipt;
import ru.ppr.inpas.lib.parser.model.ReceiptField;
import ru.ppr.inpas.lib.parser.model.ReceiptTag;
import ru.ppr.inpas.lib.protocol.SaPacket;
import ru.ppr.inpas.lib.protocol.model.SaField;
import ru.ppr.inpas.lib.protocol.model.TransactionStatus;
import ru.ppr.inpas.lib.utils.DateFormatter;
import ru.ppr.ipos.model.FinancialTransactionResult;
import ru.ppr.ipos.model.TransactionResult;

/**
 * Класс для преобразования в {@link FinancialTransactionResult}
 */
public class FinancialResultMapper {

    /**
     * Метод для преобразования SA пакета.
     *
     * @param packet пакет для преобразования.
     * @return результат преобразования.
     * @see FinancialTransactionResult
     */
    @NonNull
    public static FinancialTransactionResult from(@NonNull final SaPacket packet) {
        final FinancialTransactionResult result = new FinancialTransactionResult();
        final String amount = packet.getString(SaField.SAF_TRX_AMOUNT); // Поле 0 | Сумма операции, выраженная в минимальных единицах валюты.
        final String currencyCode = packet.getString(SaField.SAF_TRX_CURRENCY_CODE); // Поле 4 | Код валюты операции.
        final String dateTimeHost = packet.getString(SaField.SAF_TRX_DATE_TIME_HOST); // Поле 6 | Оригинальная дата и время совершения операции YYYYMMDDHHMMSS на Хосте.

        result.setAmount(amount != null ? Integer.parseInt(amount) : 0);
        result.setCurrencyCode(currencyCode);
        result.setTimeStamp(dateTimeHost != null ? DateFormatter.getDate(dateTimeHost) : new Date());

        final String cardPAN = packet.getString(SaField.SAF_PAN); // Поле 10 | Маскированный номер карты PAN.

        if (!TextUtils.isEmpty(cardPAN)) {
            result.setCardPAN(cardPAN);
        }

        final String authCode = packet.getString(SaField.SAF_AUTH_CODE); // Поле 13 | Код авторизации. (Optional)

        if (!TextUtils.isEmpty(authCode)) {
            result.setAuthorizationId(authCode);
        }

        final String referenceNumber = packet.getString(SaField.SAF_RRN); // Поле 14 | Номер ссылки (RRN). (Optional)

        if (!TextUtils.isEmpty(referenceNumber)) {
            result.setRRN(referenceNumber);
        }

        final String responseCodeHost = packet.getString(SaField.SAF_RESPONSE_CODE); // Поле 15 | Код ответа от хоста.

        if (!TextUtils.isEmpty(responseCodeHost)) {
            try {
                result.setBankResponseCode(Integer.parseInt(responseCodeHost));
            } catch (NumberFormatException ex) {
                result.setBankResponseCode(TransactionStatus.UNKNOWN.getValue());
            }
        }

        final String additionalData = packet.getString(SaField.SAF_ADDITIONAL_RESPONSE_DATA); // Поле 19 | Дополнительные данные ответа. (Optional)

        if (!TextUtils.isEmpty(additionalData)) {
            result.setBankResponse(additionalData);
        }

        final Integer transactionId = packet.getInteger(SaField.SAF_TRX_ID); // Поле 23 | Идентификатор транзакции в коммуникационном сервере.
        final String terminalId = packet.getString(SaField.SAF_TERMINAL_ID); // Поле 27 | Идентификатор внешнего устройства.
        final String merchantId = packet.getString(SaField.SAF_MERCHANT_ID); // Поле 28 | Идентификатор продавца. (Not present in documentation)
        final Integer transactionStatusNumber = packet.getInteger(SaField.SAF_TRX_STATUS); // Поле 39 | Статус проведения транзакции.
        boolean isApproved = false;

        if (transactionStatusNumber != null) {
            final TransactionStatus transactionStatus = TransactionStatus.from(transactionStatusNumber);
            isApproved = TransactionStatus.APPROVED == transactionStatus;

        }

        result.setId(transactionId != null ? transactionId : 0);
        result.setTerminalId(terminalId);
        result.setMerchantId(merchantId);
        result.setApproved(isApproved);

        final String receiptData = packet.getString(SaField.SAF_RECEIPT_DATA); // Поле 90 | Данные для печати на чеке. (Optional)

        if (!TextUtils.isEmpty(receiptData)) {
            final ReceiptFieldParser receiptFieldParser = new ReceiptFieldParser(receiptData);
            receiptFieldParser.parse();

            if (receiptFieldParser.hasFields()) {
                final List<ReceiptField> fields = receiptFieldParser.getFields();
                final List<String> receiptValues = new ArrayList<>();

                ReceiptField receiptField = null;
                ReceiptField cashierReceiptField = null;
                ReceiptField issuerNameField = null;

                for (ReceiptField item : fields) {
                    if (item.getTag().equals(ReceiptTag.CARD_TYPE.getValue())) {
                        issuerNameField = item;
                    } else {
                        if (item.getTag().equals(ReceiptTag.RECEIPT.getValue())) {
                            receiptField = item;
                        } else if (item.getTag().equals(ReceiptTag.CASHIER_RECEIPT.getValue())) {
                            cashierReceiptField = item;
                        }

                        receiptValues.add(item.getValue());
                    }
                }

                final List<String> receiptLines = new LinkedList<>();

                for (String receipt : receiptValues) {
                    final String[] lines = receipt.split("\\r?\\n");
                    receiptLines.addAll(Arrays.asList(lines));
                }

                result.setReceipt(receiptLines);

                if (issuerNameField != null) {
                    result.setIssuerName(issuerNameField.getValue());
                }

                if ((receiptField != null) && (cashierReceiptField != null)) {
                    final ReceiptParser receiptParser = new ReceiptParser(receiptField);
                    receiptParser.parse();

                    final ReceiptParser cashierReceiptParser = new ReceiptParser(cashierReceiptField);
                    cashierReceiptParser.parse();

                    final Receipt receipt = receiptParser.getReceipt();
                    final Receipt cashierReceipt = receiptParser.getReceipt();

                    // Must be the same receipt number on receipt and cashier receipt.
                    if ((receipt != null) && (cashierReceipt != null)
                            && (receipt.getReceiptNumber().equals(cashierReceipt.getReceiptNumber()))) {
                        result.setInvoiceNumber(Integer.parseInt(receipt.getReceiptNumber()));
                    }
                }
            }
        } else {
            result.setReceipt(new ArrayList<>());
        }

        result.setApplicationName(""); // В будущем: Get correct value.

        return result;
    }

    /**
     * Метод для преобразования SA пакета.
     *
     * @param packet пакет для преобразования.
     * @return результат преобразования.
     */
    @NonNull
    public static String asString(@NonNull final SaPacket packet) {
        final FinancialTransactionResult result = from(packet);
        return getFinancialTransactionResultAsString(result);
    }

    /**
     * Метод для преобразования SA пакета.
     *
     * @param result результат транзакции для преобразования.
     * @return результат преобразования.
     * @see TransactionResult
     */
    @NonNull
    public static String getTransactionResultAsString(@NonNull final TransactionResult result) {
        final String lineSeparator = System.getProperty("line.separator");
        final StringBuilder sb = new StringBuilder();
        sb.trimToSize();

        sb.append("Tx ID: ");
        sb.append(result.getId());
        sb.append(", Date: ");
        sb.append(result.getTimeStamp());
        sb.append(", Approved: ");
        sb.append(result.isApproved());
        sb.append(", Terminal ID: ");
        sb.append(result.getTerminalId());
        sb.append(", Invoice Number: ");
        sb.append(result.getInvoiceNumber());
        sb.append(", Receipt: lines = ");
        sb.append(String.valueOf(result.getReceipt().size()));
        sb.append(lineSeparator);

        for (String s : result.getReceipt()) {
            sb.append(s);
            sb.append(lineSeparator);
        }

        sb.append("Bank response code: ");
        sb.append(result.getBankResponseCode());
        sb.append(", Bank Response: ");
        sb.append(result.getBankResponse());
        sb.trimToSize();

        return sb.toString();
    }

    /**
     * Метод для преобразования SA пакета.
     *
     * @param result результат транзакции для преобразования.
     * @return результат преобразования.
     * @see FinancialTransactionResult
     */
    @NonNull
    public static String getFinancialTransactionResultAsString(@NonNull final FinancialTransactionResult result) {
        final StringBuilder sb = new StringBuilder();

        sb.append(getTransactionResultAsString(result));
        sb.append(", Amount: ");
        sb.append(result.getAmount());
        sb.append(", PAN: ");
        sb.append(result.getCardPAN());
        sb.append(", RRN: ");
        sb.append(result.getRRN());
        sb.append(", Merchant ID: ");
        sb.append(result.getMerchantId());
        sb.append(", Auth ID: ");
        sb.append(result.getAuthorizationId());
        sb.append(", Issuer name: ");
        sb.append(result.getIssuerName());
        sb.append("Currency code: ");
        sb.append(result.getCurrencyCode());
        sb.append(", Application name: ");
        sb.append(result.getApplicationName());
        sb.trimToSize();

        return sb.toString();
    }

}