package ru.ppr.cppk.logic.fiscalDocStateSync.base;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.logic.DocumentNumberProvider;
import ru.ppr.cppk.logic.builder.CheckBuilder;

/**
 * @author Dmitry Nevolin
 */
public abstract class FiscalDocStatusUpdater<T> {

    private final LocalDaoSession localDaoSession;
    private final DocumentNumberProvider documentNumberProvider;
    private final int spndNumber;
    private final Date printDateTime;

    public FiscalDocStatusUpdater(LocalDaoSession localDaoSession,
                                  DocumentNumberProvider documentNumberProvider,
                                  int spndNumber,
                                  Date printDateTime) {
        this.localDaoSession = localDaoSession;
        this.documentNumberProvider = documentNumberProvider;
        this.spndNumber = spndNumber;
        this.printDateTime = printDateTime;
    }

    public void updateToBroken() {
        localDaoSession.beginTransaction();

        try {
            updateToBrokenImpl();

            localDaoSession.setTransactionSuccessful();
        } finally {
            localDaoSession.endTransaction();
        }
    }

    public void updateToCheckPrinted() {
        localDaoSession.beginTransaction();

        try {
            Check check = new CheckBuilder()
                    .setDocumentNumber(documentNumberProvider.getNextDocumentNumber())
                    .setSnpdNumber(spndNumber)
                    .setPrintDateTime(printDateTime)
                    .build();

            localDaoSession.getCheckDao().insertOrThrow(check);

            updateToCheckPrintedImpl(check);

            localDaoSession.setTransactionSuccessful();
        } finally {
            localDaoSession.endTransaction();
        }
    }

    protected abstract void updateToBrokenImpl();
    protected abstract void updateToCheckPrintedImpl(Check check);

}
