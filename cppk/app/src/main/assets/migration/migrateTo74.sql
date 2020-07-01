UPDATE TicketTapeEvent SET CashRegisterWorkingShiftId = null WHERE TicketTapeEvent.CashRegisterWorkingShiftId is not null AND
(
    SELECT count(*) from CashRegisterWorkingShift WHERE _id=TicketTapeEvent.CashRegisterWorkingShiftId AND CashRegisterWorkingShift.Status=10
)>0;

UPDATE PrintReportEvent SET CashRegisterWorkingShiftId = null WHERE PrintReportEvent.CashRegisterWorkingShiftId is not null AND
(
    SELECT count(*) from CashRegisterWorkingShift WHERE _id=PrintReportEvent.CashRegisterWorkingShiftId AND CashRegisterWorkingShift.Status=10
)>0;