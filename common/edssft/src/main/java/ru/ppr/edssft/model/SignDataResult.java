package ru.ppr.edssft.model;

import java.util.Arrays;
import java.util.Date;

/**
 * Результат подписи данных.
 *
 * @author Aleksandr Brazhkin
 */
public class SignDataResult {
    /**
     * Флаг успешности выполнения операции
     */
    private boolean successful;
    /**
     * Описание результата
     */
    private String description;
    /**
     * Подписываемые данные
     */
    private byte[] data;
    /**
     * Подпись
     */
    private byte[] signature;
    /**
     * Номер ключа подписи
     */
    private long edsKeyNumber;
    /**
     * Время подписи
     */
    private Date signDateTime;

    public SignDataResult() {

    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public long getEdsKeyNumber() {
        return edsKeyNumber;
    }

    public void setEdsKeyNumber(long edsKeyNumber) {
        this.edsKeyNumber = edsKeyNumber;
    }

    public Date getSignDateTime() {
        return signDateTime;
    }

    public void setSignDateTime(Date signDateTime) {
        this.signDateTime = signDateTime;
    }

    @Override
    public String toString() {
        return "SignDataResult{" +
                "successful=" + successful +
                ", description='" + description + '\'' +
                ", data=" + Arrays.toString(data) +
                ", signature=" + Arrays.toString(signature) +
                ", edsKeyNumber=" + edsKeyNumber +
                ", signDateTime=" + signDateTime +
                '}';
    }
}