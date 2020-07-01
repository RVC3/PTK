UPDATE BankTransactionCashRegisterEvent
    SET
        TransactionDateTime=
            (
                SELECT CreationTimestamp FROM Event WHERE Event._id=BankTransactionCashRegisterEvent.EventId
            )
    WHERE
        TransactionDateTime=1480453200000
        AND TransactionId=0;