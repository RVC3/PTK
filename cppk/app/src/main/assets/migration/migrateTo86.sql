delete from AuditTrailEvent where type = 7 and ExtEventId in (select _id from FineSaleEvent where status = 0 or status = 1)