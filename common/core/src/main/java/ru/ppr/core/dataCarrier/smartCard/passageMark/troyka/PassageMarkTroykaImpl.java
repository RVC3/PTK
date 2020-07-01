package ru.ppr.core.dataCarrier.smartCard.passageMark.troyka;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import ru.ppr.core.manager.emito.EmitoSecurity;


import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkVersion;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.BasePassageMark;
import ru.ppr.utils.DateFormatOperations;

/**
 * Метка прохода
 * используется для стрелки и тройки
 *
 * @author isedoi
 */
public class PassageMarkTroykaImpl extends BasePassageMark implements PassageMarkTroyka {
    private int outcomeStation;
    private int trainType;
    private int ticketType;
    private int turniketNum;
    private boolean validImitovstavka;
    private int passMarkType;

    /**
     * Минуты с начала текущего года
     */
    private int intersectionTime;

    private long intersectionLongTime;
    private String intersectionStringTime;
    private final Calendar calendar = GregorianCalendar.getInstance();
    PassageMarkTroykaImpl() {
        super(PassageMarkVersion.V6, PassageMarkTroykaStructure.PASSAGE_MARK_SIZE);
    }

    /**
     * Конвертируем количество минут с начала текущего года
     */
    private void convertIntersectionTime() {
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        intersectionLongTime = calendar.getTimeInMillis() + TimeUnit.MINUTES.toMillis(intersectionTime);
        calendar.setTimeInMillis(intersectionLongTime);
        intersectionStringTime = DateFormatOperations.getDateddMMyyyyHHmm(calendar.getTime());
    }

    void setPassMarkType(int passMarkType) {
        this.passMarkType = passMarkType;
    }

    @Override
    public int getPassMarkType() {
        return passMarkType;
    }

    void setOutcomeStation(int outcomeStation) {
        this.outcomeStation = outcomeStation;
    }

    void setTrainType(int trainType) {
        this.trainType = trainType;
    }

    void setTicketType(int ticketType) {
        this.ticketType = ticketType;
    }

    void setTurniketNum(int turniketNum) {
        this.turniketNum = turniketNum;
    }

    void setValidImitovstavka(byte[] data, int imitovstavka) {
        EmitoSecurity emitoSecurity = new EmitoSecurity();
        int code_emito = emitoSecurity.emitoCreate(data);
        validImitovstavka = code_emito == imitovstavka;
    }

    void setIntersectionTime(int intersectionTime) {
        this.intersectionTime = intersectionTime;
        convertIntersectionTime();
    }

    @Override
    public int getOutComeStation() {
        return outcomeStation;
    }

    @Override
    public boolean isValidImitovstavka() {
        return validImitovstavka;
    }

    @Override
    public int getTrainType() {
        return trainType;
    }

    @Override
    public int getTicketType() {
        return ticketType;
    }

    @Override
    public int getTurniketNumber() {
        return turniketNum;
    }


    @Override
    public String getIntersectionTimeFormatted() {
        return intersectionStringTime;
    }

    @Override
    public long getIntersectionLongTime() {
        return intersectionLongTime;
    }

    @Override
    public boolean isCheckExitStation() {
        String code_str = Integer.toString(this.outcomeStation);
        String numbers = code_str.substring(Math.max(0, code_str.length() - 5));
        return numbers.equals("11111");
    }


    @Override
    public String toString() {
        return "PassageMarkTroykaImpl{" +
                "outcomeStation=" + outcomeStation +
                ", trainType=" + trainType +
                ", ticketType=" + ticketType +
                ", turniketNum=" + turniketNum +
                ", validImitovstavka=" + validImitovstavka +
                ", passMarkType=" + passMarkType +
                ", intersectionTime=" + intersectionTime +
                ", intersectionLongTime=" + intersectionLongTime +
                ", intersectionStringTime='" + intersectionStringTime + '\'' +
                '}';
    }
}
