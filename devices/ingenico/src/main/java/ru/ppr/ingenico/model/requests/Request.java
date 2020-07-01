package ru.ppr.ingenico.model.requests;

import android.support.annotation.NonNull;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ru.ppr.ingenico.utils.Arcus2Utils;
import ru.ppr.ingenico.utils.BCD;
import ru.ppr.ingenico.utils.BER;
import ru.ppr.ingenico.utils.BitUtils;
import ru.ppr.ipos.model.FinancialTransactionResult;
import ru.ppr.ipos.model.TransactionResult;
import ru.ppr.logger.Logger;

/**
 * Created by Dmitry Nevolin on 13.11.2015.
 */
public enum Request {

    TRACE("TRACE:"),
    PRINT("PRINT:"),
    PING("PING:"),
    STATUS("STATUS:"),
    STORE_RC("STORERC:"),
    I344("I344:"),
    DEVICE_OPEN("DEVICEOPEN:"),
    DEVICE_CLOSE("DEVICECLOSE:"),
    IO_CTL("IOCTL:"),
    CONNECT("CONNECT:"),
    DISCONNECT("DISCONNECT:"),
    WRITE("WRITE:"),
    READ("READ:"),
    MENU("MENU:"),
    AMOUNT_ENTRY("AMOUNTENTRY:"),
    DATA_ENTRY("DATAENTRY:"),
    WARNING("WARNING:"),
    CARD_REQ("CARDREQ:"),
    END_TR("ENDTR"),
    YES_NO("YESNO:"),
    BEGIN_TR("BEGINTR:"),
    OP_DET("OPDET:"),
    TIME_SYNC("TIMESYNC:"),
    TMS_ID("TMSID:"),
    TMS_SCRIPT("TMSSCRIPT:"),
    DISPLAY("DISPLAY:"),
    CLEAR_DISP("CLEARDISP:"),
    DISP_INV("DISPINV:"),
    GET_TAGS("GETTAGS:"),
    SET_TAGS("SETTAGS:"),
    SP_RESULT("SPRESULT:"),
    SP_REQUEST("SPREQUEST:"),
    KEY_REQ("KEYREQ:"),
    WRITE_BARCODE("WRBARCODE:"),
    OOB_SESSION("OOBSESSION:"),
    START("START"),
    CHECK("CHECK"),
    END("END"),
    CONTINUE("CONTINUE"),
    STAND_BY("STANDBY"),
    UNKNOWN("UNKNOWN");

    private static final String TAG = Logger.makeLogTag(Request.class);

    private byte[] header;

    Request(String request) {
        this.header = request.getBytes(Arcus2Utils.DEFAULT_CHARSET);
    }

    public static Body getBody(@NonNull byte[] bytes) {
        byte[] tmp = Arrays.copyOfRange(bytes, 3, bytes.length);

        for (Request request : values())
            if (request.isHeaderEqual(tmp))
                return new Body(request, Arrays.copyOfRange(tmp, request.header.length, tmp.length));

        return new Body(UNKNOWN, new byte[0]);
    }

    private boolean isHeaderEqual(@NonNull byte[] bytes) {
        if (header.length > bytes.length)
            return false;

        for (int i = 0; i < header.length; i++)
            if (header[i] != bytes[i])
                return false;

        return true;
    }

    public static class Body {

        Request type;
        byte[] bytes;

        private Body(Request type, byte[] bytes) {
            this.type = type;
            this.bytes = bytes;
        }

        public Request getType() {
            return type;
        }

        public byte[] getBytes() {
            return bytes;
        }

    }

    public static class Utils {

        public static class STORE_RC {

            public static int parse(Request.Body body) {
                int result = -1;

                if (body.getType() != Request.STORE_RC)
                    return result;

                try {
                    result = Integer.valueOf(new String(body.getBytes()));
                } catch (Exception exception) {
                    exception.printStackTrace();

                    result = -1;
                }

                return result;
            }

            public static boolean getApproved(int code) {
                return code == 0 || code == 1 || code == 2 || code == 3 || code == 4 ||
                        code == 5 || code == 6 || code == 7 || code == 8 || code == 9 ||
                        code == 10 || code == 880 || code == 881 ||
                        /* Код ошибки 963, который терминал возвращает при попытке отмены уже
                         * отменённой операции, в соответствии с https://aj.srvdev.ru/browse/CPPKPP-26671
                         * такой код ошибки должен быть расценен как успешная операция. */
                        code == 963;
            }

            public static String getMessage(int code) {
                switch (code) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                        return "УСПЕШНО";
                    case 50:
                        return "ОТКАЗ";
                    case 51:
                        return "КАРТА ПРОСРОЧЕНА";
                    case 52:
                        return "ЛИМИТ ПИН ИСЧЕРПАН";
                    case 53:
                        return "ЧУЖАЯ АДМ. КАРТА";
                    case 54:
                        return "НЕТ БЛОКА СЕКРЕТНОСТИ";
                    case 55:
                        return "НЕВЕРНАЯ ОПЕРАЦИЯ";
                    case 56:
                        return "ОПЕРАЦИЯ НЕ ПОДДЕРЖИВАЕТСЯ БАНКОМ";
                    case 57:
                        return "КАРТА ПОТЕРЯНА / УКРАДЕНА";
                    case 58:
                        return "НЕВЕРНЫЙ СТАТУС КАРТЫ";
                    case 59:
                        return "КАРТА ОГРАНИЧЕНА";
                    case 60:
                        return "НЕТ СЧЕТА В ФАЙЛЕ";
                    case 61:
                        return "НЕ НАЙДЕНА ЗАПИСЬ В ФАЙЛЕ";
                    case 62:
                        return "ОШИБКА ЗАПИСИ";
                    case 63:
                        return "НЕВЕРНЫЙ ТИП АВТОРИЗАЦИИ";
                    case 64:
                        return "ПЛОХАЯ ДОРОЖКА КАРТЫ";
                    case 65:
                        return "КОРРЕКЦИЯ ЗАПРЕЩЕНА";
                    case 66:
                        return "ОШИБКА ВЫДАЧИ ДЕНЕГ";
                    case 67:
                        return "НЕКОРРЕКТНАЯ ДАТА ОПЕРАЦИИ";
                    case 68:
                        return "ОШИБКА ФАЙЛА";
                    case 69:
                        return "НЕВЕРНЫЙ ФОРМАТ СООБЩЕНИЯ";
                    case 70:
                        return "НЕ НАЙДЕНА ЗАПИСЬ";
                    case 71:
                        return "НЕКОРРЕКТНЫЙ ТИП АВТОРИЗАЦИИ";
                    case 72:
                        return "КАРТА В ЧЕРНОМ СПИСКЕ";
                    case 73:
                        return "НЕКОРРЕКТНОЕ ПОЛЕ АВТОРИЗАЦИИ";
                    case 74:
                        return "НЕВОЗМОЖНО АВТОРИЗОВАТЬ";
                    case 75:
                        return "НЕКОРРЕКТНАЯ ДЛИНА НОМЕРА";
                    case 76:
                        return "НЕДОСТАТОЧНО СРЕДСТВ";
                    case 77:
                        return "ДОСТИГНУТ ЛИМИТ ПРЕДАВТОРИЗАЦИИ";
                    case 78:
                        return "ДУБЛИРОВАННАЯ ТРАНЗАКЦИЯ";
                    case 79:
                        return "ПРЕВЫШЕНИЕ ОНЛАЙН ЛИМИТА ВОЗВРАТА";
                    case 80:
                        return "ПРЕВЫШЕНИЕ ОФФЛАЙН  ЛИМИТА ВОЗВРАТА";
                    case 81:
                        return "ПРЕВЫШЕНИЕ КРЕДИТА ВОЗВРАТА";
                    case 82:
                        return "ПРЕВЫШЕНИЕ ЧИСЛА ПОЛЬЗОВАНИЙ";
                    case 83:
                        return "ПРЕВЫШЕНИЕ ЛИМИТА СУММЫ";
                    case 84:
                        return "КАРТА НЕДЕЙСТВИТЕЛЬНА";
                    case 85:
                        return "НЕВОЗМОЖНО ВЫДАТЬ БАЛАНС";
                    case 86:
                        return "ПРЕВЫШЕНИЕ ОФФЛАЙН ЛИМИТА";
                    case 87:
                        return "ПРЕВЫШЕНИЕ ЧИСЛА ИСПОЛЬЗОВАНИЙ КРЕДИТА";
                    case 88:
                        return "ПОЗВОНИТЕ В БАНК";
                    case 89:
                        return "КАРТА НЕ АКТИВНА";
                    case 90:
                        return "НЕТ МЕСТА В ПРФ ФАЙЛЕ";
                    case 91:
                        return "ПРОБЛЕМЫ С НЕГ ФАЙЛОМ";
                    case 92:
                        return "СУММА СНЯТИЯ ДЕНЕГ МАЛА";
                    case 93:
                        return "АРЕСТОВАННЫЙ СЧЕТ";
                    case 94:
                        return "ПРЕВЫШЕНИЕ ЛИМИТА";
                    case 95:
                        return "ПРЕВЫШЕНИЯ ЛИМИТА";
                    case 96:
                        return "ТРЕБУЕТСЯ ПИН";
                    case 97:
                        return "ОШИБКА КОНТР. СУММЫ";
                    case 98:
                        return "НЕТ ДОСТУПА К ПРОЦЕССИНГУ";
                    case 99:
                        return "ПЛОХОЙ ПБФ";
                    case 100:
                    case 130:
                    case 131:
                    case 132:
                    case 133:
                    case 134:
                        return "ПОЗВОНИТЕ В БАНК";
                    case 150:
                        return "ПРОДАВЕЦ НЕ ЗАРЕГИСТРИРОВАН";
                    case 200:
                        return "НЕПРАВИЛЬНЫЙ СЧЕТ";
                    case 201:
                        return "НЕПРАВИЛЬНЫЙ ПИН, ПОВТОРИТЕ";
                    case 202:
                        return "СУММА СНЯТИЯ МАЛА";
                    case 203:
                        return "ТРЕБУЕТСЯ АДМ. КАРТА";
                    case 204:
                        return "ОТКАЗ, ПОЗВОНИТЕ В БАНК";
                    case 205:
                        return "НЕКОРРЕКТНАЯ СУММА";
                    case 206:
                        return "НЕ НАЙДЕНА ЗАПИСЬ В КАФ ФАЙЛЕ";
                    case 207:
                        return "НЕКОРРЕКТНАЯ ДАТА ОПЕРАЦИИ, ПОВТОРИТЕ";
                    case 208:
                        return "НЕКОРРЕКТНЫЙ СРОК ДЕЙСТВИЯ КАРТЫ";
                    case 209:
                        return "НЕВЕРНЫЙ КОД ОПЕРАЦИИ";
                    case 251:
                        return "НЕХВАТКА СРЕДСТВ";
                    case 252:
                        return "ДЕБЕТЫЕ КАРТЫ НЕ ПОДДЕРЖИВАЮТСЯ";
                    case 400:
                        return "ОШИБКА ARQC";
                    case 401:
                        return "ОШИБКА БЕЗОПАСНОСТИ";
                    case 402:
                        return "ОШИБКА БЕЗОПАСНОСТИ";
                    case 403:
                        return "НЕ НАЙДЕН КЛЮЧ СМАРТ КАРТЫ";
                    case 404:
                        return "ОШИБКА ПРОВЕРКИ ATC";
                    case 405:
                        return "ОТКАЗ ПРИ ПРОВЕРКЕ CVR";
                    case 406:
                        return "ОТКАЗ ПРИ ПРОВЕРКЕ TVR";
                    case 407:
                        return "ОТКАЗ ПРИ ПРОВЕРКЕ ПРИЧИНЫ ОНЛАЙН";
                    case 408:
                        return "ОТКАЗ ПРИ ФОЛБЭК";
                    case 800:
                        return "ОШИБКА ФОРМАТА";
                    case 801:
                        return "ОШИБКА В ДАННЫХ";
                    case 802:
                        return "ОШИБКА В КОДЕ КАССИРА";
                    case 809:
                        return "ОШИБОЧНАЯ ОПЕРАЦИЯ ЗАКРЫТИЯ";
                    case 810:
                        return "ТАЙМАУТ ТРАНЗАКЦИИ";
                    case 811:
                        return "СИСТЕМНАЯ ОШИБКА";
                    case 820:
                        return "ОШИБОЧНЫЙ НОМЕР ТЕРМИНАЛА";
                    case 821:
                        return "ОШИБОЧНАЯ ДЛИНА ПАКЕТА";
                    case 870:
                        return "ПОЧТА ДОСТАВЛЕНА";
                    case 871:
                        return "ПОЧТА СОХРАНЕНА";
                    case 880:
                    case 881:
                        return "СООБЩЕНИЕ ПОЛУЧЕНО";
                    case 882:
                        return "ЗАГРУЗКА ПАРАМЕТРОВ ПРЕРВАНА";
                    case 889:
                    case 898:
                        return "ОШИБКА МАК";
                    case 899:
                        return "ОШИБКА ПОСЛЕДОВАТЕЛЬНОСТИ";
                    case 900:
                        return "ПРЕВЫШЕНИЕ ЧИСЛА ВВОДА ПИН";
                    case 901:
                        return "ПОСРОЧЕННАЯ КАРТА";
                    case 902:
                    case 903:
                        return "ОТБЕРИТЕ КАРТУ!";
                    case 904:
                        return "СУММА МАЛА";
                    case 905:
                        return "ПРЕВЫШЕНО КОЛИЧЕСТВО ПОЛЬЗОВАНИЙ КАРТЫ";
                    case 906:
                        return "АРЕСТОВАННЫЙ СЧЕТ";
                    case 907:
                        return "ПРЕВЫШЕНИЕ ЛИМИТА. ОТБЕРИТЕ КАРТУ.";
                    case 908:
                        return "СУММА БОЛЬШЕ МАКСИМУМА. ОТБЕРИТЕ КАРТУ";
                    case 909:
                        return "ОТБЕРИТЕ КАРТУ";
                    case 910:
                        return "ARQC ОТБЕРИТЕ КАРТУ";
                    case 911:
                        return "CVR ОТБЕРИТЕ КАРТУ";
                    case 912:
                        return "TVR ОТБЕРИТЕ КАРТУ";
                    case 950:
                        return "НЕ АДМ. КАРТА";
                    case 951:
                        return "НЕ ТА АДМ. КАРТА";
                    case 959:
                        return "АДМ. ТРАНЗАКЦИИ НЕ ПОДДЕРЖИВАЮТСЯ";
                    case 952:
                    case 953:
                    case 954:
                        return "АДМ. ОПЕРАЦИЯ РАЗРЕШЕНА";
                    case 955:
                    case 956:
                        return "ВОЗВРАТ ДЕНЕГ РАЗРЕШЕН";
                    case 957:
                        return "ВОЗВРАТ ДЕНЕГ РАЗРЕШЕН, ПЛОХОЙ ПРЕФИКС";
                    case 958:
                        return "ВОЗВРАТ ДЕНЕГ НЕ РАЗРЕШЕН";
                    case 960:
                    case 961:
                    case 962:
                        return "ВОЗВРАТ ДЕНЕГ РАЗРЕШЕН";
                    case 963:
                        return "УЖЕ ОТМЕНЕНА";
                    case 992:
                        return "ОПЕРАЦИЯ ПРЕРВАНА";
                    case 998:
                        return "ОШИБКА СВЯЗИ, ПОЗВОНИТЕ В БАНК";
                    case 999:
                        return "ПИН-ПАД НЕ ОТВЕЧАЕТ";
                    default:
                        return "НЕИЗВЕСТНЫЙ КОД: " + code;
                }
            }

        }

        public static class SET_TAGS {

            @SuppressWarnings("serial")
            private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("MMddyyyyHHmmss", Locale.US);

            public static TransactionResult getTransactionResult(Request.Body body) {
                if (body.getType() != Request.SET_TAGS)
                    return null;

                TransactionResult transactionResult = new TransactionResult();
                String time = "";
                String date = "";

                for (BER.Block block : BER.decode(body.getBytes())) {
                    int code = new BigInteger(1, block.getIdentifier()).intValue();

                    switch (code) {
                        //Invoice orderNumber
                        case 0x1F06:
                            transactionResult.setInvoiceNumber(BitUtils.convertToUInt16(block.getData(), 0));
                            // В будущем временно, пока не будет отмены по RRN
                            transactionResult.setId(transactionResult.getInvoiceNumber());
                            break;
                        //Terminal id
                        case 0x9F1C:
                            transactionResult.setTerminalId(new String(block.getData(), Arcus2Utils.DEFAULT_CHARSET));
                            break;
                        //Time
                        case 0x1F08:
                            time = BCD.toString(block.getData());
                            break;
                        //Date
                        case 0x1F09:
                            date = BCD.toString(block.getData());
                            break;
                        //Authorization id
                        case 0x89:
                            break;
                        //RRN
                        case 0x1F03:
                            break;
                        //Application name
                        case 0x50:
                            break;
                        //Issuer name
                        case 0x1F26:
                            break;
                        //Card pan
                        case 0x5A:
                            break;
                        //Amount
                        case 0x9F02:
                            break;
                        //Merchant id
                        case 0x1F47:
                            break;
                        // AID
                        case 0x4F:
                            break;
                        // TVR
                        case 0x95:
                            break;
                        // Expiry
                        case 0x5F24:
                        case 0x9F39:
                        case 0x5F20:
                        case 0x1F2d:
                        case 0x1F61:
                        case 0x9F21:
                        case 0x9a:
                            break;
                        default:
                            Logger.info(Request.class, "No response with code \"" + code + "\" is supported yet");
                    }
                }

                //https://aj.srvdev.ru/browse/CPPKPP-32711
                //Корчак Александр: "Если приходят нули в виде даты. Пишем текущее время."
                Date dateTime = new Date();
                try {
                    if (Integer.valueOf(date) != 0 && Integer.valueOf(time) != 0) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());
                        dateTime = (SIMPLE_DATE_FORMAT.parse(String.format("%04d", Integer.valueOf(date)) + calendar.get(Calendar.YEAR) + String.format("%06d", Integer.valueOf(time))));
                    }
                } catch (NumberFormatException exception) {
                    Logger.error(TAG, exception);
                } catch (ParseException exception) {
                    Logger.error(TAG, exception);
                }

                transactionResult.setTimeStamp(dateTime);

                return transactionResult;
            }

            public static FinancialTransactionResult getFinancialTransactionResult(Request.Body body) {
                if (body.getType() != Request.SET_TAGS)
                    return null;

                FinancialTransactionResult financialTransactionResult = new FinancialTransactionResult(getTransactionResult(body));
                financialTransactionResult.setMerchantId("");
                financialTransactionResult.setCurrencyCode("RUB");

                for (BER.Block block : BER.decode(body.getBytes())) {
                    switch (new BigInteger(1, block.getIdentifier()).intValue()) {
                        //RRN
                        case 0x1F03:
                            financialTransactionResult.setRRN(new String(block.getData(), Arcus2Utils.DEFAULT_CHARSET));
                            break;
                        //Application name
                        case 0x50:
                            financialTransactionResult.setApplicationName(new String(block.getData(), Arcus2Utils.DEFAULT_CHARSET));
                            break;
                        //Issuer name
                        case 0x1F26:
                            financialTransactionResult.setIssuerName(new String(block.getData(), Arcus2Utils.DEFAULT_CHARSET));
                            break;
                        //Card pan
                        case 0x5A:
                            financialTransactionResult.setCardPAN(BCD.toString(block.getData()));
                            break;
                        //Amount
                        case 0x9F02:
                            financialTransactionResult.setAmount(BCD.toInt(block.getData()));
                            break;
                        //Merchant id
                        case 0x1F47:
                            financialTransactionResult.setMerchantId(new String(block.getData(), Arcus2Utils.DEFAULT_CHARSET));
                            break;
                        //Authorization id
                        case 0x89:
                            financialTransactionResult.setAuthorizationId(new String(block.getData(), Arcus2Utils.DEFAULT_CHARSET));
                            break;
                    }
                }

                return financialTransactionResult;
            }

        }

        public static class PRINT {

            public static String parse(Request.Body body) {
                if (body.getType() != Request.PRINT)
                    return "";

                return new String(body.getBytes(), Arcus2Utils.TERMINAL_OUT_CHARSET);
            }

        }

    }

}
