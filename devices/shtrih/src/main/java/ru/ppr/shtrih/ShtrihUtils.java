package ru.ppr.shtrih;

import com.shtrih.fiscalprinter.command.CashRegister;
import com.shtrih.fiscalprinter.command.DeviceMetrics;
import com.shtrih.fiscalprinter.command.FSCommunicationStatus;
import com.shtrih.fiscalprinter.command.FSDateTime;
import com.shtrih.fiscalprinter.command.FSStatusInfo;
import com.shtrih.fiscalprinter.command.LongPrinterStatus;
import com.shtrih.fiscalprinter.command.OperationRegister;
import com.shtrih.fiscalprinter.command.PrinterDate;
import com.shtrih.fiscalprinter.command.PrinterTime;
import com.shtrih.fiscalprinter.command.ShortPrinterStatus;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Утилиты для драйвера принтера Штрих-Мобайл-ПТК
 *
 * @author Aleksandr Brazhkin
 */
class ShtrihUtils {

    static String deviceMetricsToString(DeviceMetrics deviceMetrics) {

        return "DeviceMetrics{" +
                "deviceType=" + deviceMetrics.getDeviceType() +
                ", deviceSubType=" + deviceMetrics.getDeviceSubType() +
                ", protocolVersion=" + deviceMetrics.getProtocolVersion() +
                ", protocolSubVersion=" + deviceMetrics.getProtocolSubVersion() +
                ", model=" + deviceMetrics.getModel() +
                ", language=" + deviceMetrics.getLanguage() +
                ", deviceName='" + deviceMetrics.getDeviceName() + '\'' +
                "}";
    }

    static String shortPrinterStatusToString(ShortPrinterStatus shortPrinterStatus) {
        return "LongPrinterStatus{" +
                "mode=" + shortPrinterStatus.getMode() +
                ", flags=" + shortPrinterStatus.getFlags() +
                ", submode=" + shortPrinterStatus.getSubmode() +
                ", FMResultCode=" + shortPrinterStatus.getFMResultCode() +
                ", EJResultCode=" + shortPrinterStatus.getEJResultCode() +
                ", receiptOperations=" + shortPrinterStatus.getReceiptOperations() +
                ", batteryVoltage=" + shortPrinterStatus.getBatteryVoltage() +
                ", powerVoltage=" + shortPrinterStatus.getPowerVoltage() +
                ", operatorNumber=" + shortPrinterStatus.getOperatorNumber() +
                "}";
    }

    static String longPrinterStatusToString(LongPrinterStatus longPrinterStatus) {
        return "LongPrinterStatus{" +
                "operatorNumber=" + longPrinterStatus.getOperatorNumber() +
                ", flags=" + longPrinterStatus.getFlags() +
                ", mode=" + longPrinterStatus.getMode() +
                ", submode=" + longPrinterStatus.getSubmode() +
                ", firmwareVersion='" + longPrinterStatus.getFirmwareVersion() + '\'' +
                ", firmwareBuild=" + longPrinterStatus.getFirmwareBuild() +
                ", firmwareDate=" + printerDateToString(longPrinterStatus.getFirmwareDate()) +
                ", logicalNumber=" + longPrinterStatus.getLogicalNumber() +
                ", documentNumber=" + longPrinterStatus.getDocumentNumber() +
                ", portNumber=" + longPrinterStatus.getPortNumber() +
                ", fmFirmwareVersion='" + longPrinterStatus.getFmFirmwareVersion() + '\'' +
                ", fmFirmwareBuild=" + longPrinterStatus.getFmFirmwareBuild() +
                ", fmFirmwareDate=" + printerDateToString(longPrinterStatus.getFmFirmwareDate()) +
                ", date=" + printerDateToString(longPrinterStatus.getDate()) +
                ", time=" + printerDateToString(longPrinterStatus.getTime()) +
                ", fmFlags=" + longPrinterStatus.getFmFlags() +
                ", serialNumber=" + longPrinterStatus.getSerialNumber() +
                ", dayNumber=" + longPrinterStatus.getDayNumber() +
                ", freeRecordInFM=" + longPrinterStatus.getFreeRecordInFM() +
                ", registrationNumber=" + longPrinterStatus.getRegistrationNumber() +
                ", freeRegistration=" + longPrinterStatus.getFreeRegistration() +
                ", fiscalID=" + longPrinterStatus.getFiscalID() +
                ", mode=" + longPrinterStatus.getMode() +
                "}";
    }

    static String printerDateToString(PrinterDate printerDate) {
        return "PrinterDate{" +
                "day=" + printerDate.getDay() +
                ", month=" + printerDate.getMonth() +
                ", year=" + printerDate.getYear() +
                "}";
    }

    static String printerDateToString(PrinterTime printerTime) {
        return "PrinterDate{" +
                "hour=" + printerTime.getHour() +
                ", min=" + printerTime.getMin() +
                ", sec=" + printerTime.getSec() +
                "}";
    }

    static String cashRegisterToString(CashRegister cashRegister) {
        return "CashRegister{" +
                "number=" + cashRegister.getNumber() +
                ", value=" + cashRegister.getValue() +
                "}";
    }

    static String operationRegisterToString(OperationRegister operationRegister) {
        return "OperationRegister{" +
                "number=" + operationRegister.getNumber() +
                ", value=" + operationRegister.getValue() +
                "}";
    }

    static String fsStatusInfoToString(FSStatusInfo fsStatusInfo) {
        return "OperationRegister{" +
                "status=" + fsStatusInfo.getStatus() +
                ", docType=" + fsStatusInfo.getDocType() +
                ", isDocReceived=" + fsStatusInfo.isDocReceived() +
                ", isDayOpened=" + fsStatusInfo.isDayOpened() +
                ", flags=" + fsStatusInfo.getFlags() +
                ", dateTime=" + fsStatusInfo.getDateTime() +
                ", fsSerial=" + fsStatusInfo.getFsSerial() +
                ", docNumber=" + fsStatusInfo.getDocNumber() +
                "}";
    }

    static String fsCommunicationStatusToString(FSCommunicationStatus fsCommunicationStatus) {
        return "FSCommunicationStatus{" +
                "unsentDocumentsCount=" + fsCommunicationStatus.getUnsentDocumentsCount() +
                ", firstUnsentDocumentNumber=" + fsCommunicationStatus.getFirstUnsentDocumentNumber() +
                ", firstUnsentDocumentDateTime=" + fsCommunicationStatus.getFirstUnsentDocumentDateTime() +
                ", communicationState=" + fsCommunicationStatus.getCommunicationState() +
                ", messageReadingStatus=" + fsCommunicationStatus.getMessgeReadingStatus() +
                "}";
    }

    static Date fSDateTimeToDate(FSDateTime fsDateTime) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.YEAR, fsDateTime.getYear());
        //http://agile.srvdev.ru/browse/CPPKPP-34870
        calendar.set(Calendar.MONTH, fsDateTime.getMonth() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, fsDateTime.getDay());
        calendar.set(Calendar.HOUR_OF_DAY, fsDateTime.getHours());
        calendar.set(Calendar.MINUTE, fsDateTime.getMinutes());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
