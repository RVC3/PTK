package ru.ppr.chit.domain.model.local;

import java.util.Date;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;

/**
 * Станция контроля
 *
 * @author Dmitry Nevolin
 */
public class ControlStation implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Код станции
     */
    private Long code;
    /**
     * Время отправления со станции в UTC или null, если это конечная станция
     */
    private Date departureDate;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    @Override
    public String toString() {
        return "ControlStation{" +
                "id=" + id +
                ", code=" + code +
                ", departureDate=" + departureDate +
                '}';
    }
}
