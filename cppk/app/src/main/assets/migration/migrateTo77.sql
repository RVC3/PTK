CREATE TABLE ProcessingFees (
    Tariff            REAL,
    FeeType           INTEGER,
    VersionId         INTEGER
);

INSERT INTO ProcessingFees (Tariff, FeeType, VersionId) values (50, 0, 63);
INSERT INTO ProcessingFees (Tariff, FeeType, VersionId) values (100, 0, 63);
INSERT INTO ProcessingFees (Tariff, FeeType, VersionId) values (50, 1, 63);
INSERT INTO ProcessingFees (Tariff, FeeType, VersionId) values (100, 1, 63);
INSERT INTO ProcessingFees (Tariff, FeeType, VersionId) values (50, 2, 63);
INSERT INTO ProcessingFees (Tariff, FeeType, VersionId) values (100, 2, 63);
INSERT INTO ProcessingFees (Tariff, FeeType, VersionId) values (100, 3, 63);
INSERT INTO ProcessingFees (Tariff, FeeType, VersionId) values (100, 4, 63);
INSERT INTO ProcessingFees (Tariff, FeeType, VersionId) values (200, 2, 113);

ALTER TABLE Fee ADD COLUMN FeeType INTEGER;


--судя по коду ПТК условие должно быть такое:
--if (TicketEventBase.TicketCategoryCode==1(не багаж))
--	варианты 0 2
--else {
--	варианты 1 2
--}

UPDATE Fee set FeeType =
(
    SELECT FeeType FROM ProcessingFees AS PF WHERE
    PF.Tariff = Fee.Total
--    AND VersionId <= -- очень медленная операция, падает с anr, поэтому исключаем
--        (
--            SELECT VersionId FROM Event WHERE _id =
--                (
--                    SELECT EventId FROM CPPKTicketSales WHERE TicketSaleReturnEventBaseId =
--                        (
--                            SELECT _id FROM TicketSaleReturnEventBase WHERE FeeId =  Fee._id
--                        )
--                )
--    )
     AND PF.FeeType == 2 OR
         CASE
           (
               SELECT TicketCategoryCode FROM TicketEventBase WHERE TicketEventBase._id =
                (
                    SELECT TicketEventBaseId FROM TicketSaleReturnEventBase WHERE FeeId = Fee._id
                )
            )
        WHEN 1 THEN 0
        WHEN 2 THEN 2
        END
    ORDER BY FeeType LIMIT 1
);

UPDATE Fee SET FeeType = 2 WHERE FeeType is null AND Total>0;

DROP TABLE ProcessingFees;
