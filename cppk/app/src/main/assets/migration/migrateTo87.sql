ALTER TABLE CashRegister ADD COLUMN FNSerial TEXT;
UPDATE CashRegister SET FNSerial=EKLZNumber WHERE Model like "%ШТРИХ%";
UPDATE CashRegister SET EKLZNumber=null WHERE Model like "%ШТРИХ%";