UPDATE TicketBoarding SET ControlStationCode = (SELECT TicketData.DepartureStationCode FROM TicketData WHERE TicketData._id = TicketBoarding.TicketDataId)
WHERE TicketBoarding.ControlStationCode IS NULL;
