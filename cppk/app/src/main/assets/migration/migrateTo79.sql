UPDATE SmartCard
SET TypeCode = ( CASE
                     WHEN
                            ( SELECT s2.TypeCode AS code
                             FROM SmartCard s2
                             WHERE SmartCard.OuterNumber=s2.OuterNumber
                               AND SmartCard.CrystalSerialNumber=s2.CrystalSerialNumber
                               AND s2.TypeCode>0
                             LIMIT 1) IS NOT NULL THEN
                            ( SELECT s2.TypeCode AS code
                             FROM SmartCard s2
                             WHERE SmartCard.OuterNumber=s2.OuterNumber
                               AND SmartCard.CrystalSerialNumber=s2.CrystalSerialNumber
                               AND s2.TypeCode>0
                             LIMIT 1)
                     ELSE 0
                 END )
WHERE SmartCard.TypeCode<0;