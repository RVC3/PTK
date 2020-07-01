drop table if exists FixedLossSumMap;
create temp table FixedLossSumMap as
	select ExemptionId, CPPKTicketSales.FullTicketPrice - Price.Full as LossSum from TicketSaleReturnEventBase
	inner join TicketEventBase on TicketEventBaseId = TicketEventBase._id
	inner join Price on PriceId = Price._id
	inner join CPPKTicketSales on TicketSaleReturnEventBase._id = TicketSaleReturnEventBaseId
	where ExemptionId not null and WayType = 1;
update Exemption set LossSum = 
(
	select LossSum from FixedLossSumMap 
	where _id = ExemptionId
) 
where _id in (select ExemptionId from FixedLossSumMap);
drop table if exists FixedLossSumMap;