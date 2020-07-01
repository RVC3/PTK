package ru.ppr.cppk.logic.builder;

import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.cppk.entity.event.model.Check;

/**
 * Билдер сущности {@link Check}.
 *
 * @author Aleksandr Brazhkin
 */
public class CheckBuilder {

    private Integer documentNumber;
    private Integer snpdNumber;
    private Date printDateTime;
    private String additionalInfo;

    public CheckBuilder setDocumentNumber(Integer documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public CheckBuilder setSnpdNumber(Integer snpdNumber) {
        this.snpdNumber = snpdNumber;
        return this;
    }

    public CheckBuilder setPrintDateTime(Date printDateTime) {
        this.printDateTime = printDateTime;
        return this;
    }

    public CheckBuilder setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }

    @NonNull
    public Check build() {

        if (documentNumber == null) {
            throw new NullPointerException("OrderNumber is null");
        }
        if (snpdNumber == null) {
            throw new NullPointerException("SnpdNumber is null");
        }
        if (printDateTime == null) {
            throw new NullPointerException("PrintDateTime is null");
        }

        Check check = new Check();
        check.setOrderNumber(documentNumber);
        check.setSnpdNumber(snpdNumber);
        check.setPrintDateTimeInMillis(printDateTime);
        check.setAdditionalInfo(additionalInfo);

        return check;
    }
}
