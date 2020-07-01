UPDATE TicketEventBase SET WayType=0 WHERE WayType=1 AND TicketEventBase._id IN
(
    select TicketEventBaseId FROM TicketSaleReturnEventBase WHERE ParentTicketId>0

);