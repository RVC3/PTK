package ru.ppr.nsi.entity;

/**
 * Соответствие типа родительского ПД типу трансфера
 *
 * @author Dmitry Vinogradov
 */
public class TrainTicketTypeForTransferRegistration {
    /**
     * Код типа родительского ПД
     */
    private long trainTicketTypeCode;
    /**
     * Код типа трансфера
     */
    private long transferTicketTypeCode;
    /**
     * Направление родительского ПД
     */
    private Integer trainTicketWayTypeCode;

    public long getTrainTicketTypeCode() {
        return trainTicketTypeCode;
    }

    public void setTrainTicketTypeCode(long trainTicketTypeCode) {
        this.trainTicketTypeCode = trainTicketTypeCode;
    }

    public long getTransferTicketTypeCode() {
        return transferTicketTypeCode;
    }

    public void setTransferTicketTypeCode(long transferTicketTypeCode) {
        this.transferTicketTypeCode = transferTicketTypeCode;
    }

    public Integer getTrainTicketWayTypeCode() {
        return trainTicketWayTypeCode;
    }

    public void setTrainTicketWayTypeCode(Integer trainTicketWayTypeCode) {
        this.trainTicketWayTypeCode = trainTicketWayTypeCode;
    }
}
