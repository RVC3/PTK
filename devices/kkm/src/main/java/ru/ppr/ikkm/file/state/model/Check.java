package ru.ppr.ikkm.file.state.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import ru.ppr.ikkm.IPrinter;

/**
 * Чек принтера
 * Created by Артем on 21.01.2016.
 */
public class Check {
    private long id;
    private IPrinter.DocType type;
    private IPrinter.PaymentType paymentMethod;
    private int spdnNumber;
    private Date printTime;
    private List<Item> items;
    private Shift shift;
    private BigDecimal total = BigDecimal.ZERO; // итог чека
    private BigDecimal payment = BigDecimal.ZERO; // сколько денег получили от покупателя

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal amount) {
        this.total = amount;
    }

    public BigDecimal getPayment() {
        return payment;
    }

    public void setPayment(BigDecimal payment) {
        this.payment = payment;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public IPrinter.DocType getType() {
        return type;
    }

    public void setType(IPrinter.DocType type) {
        this.type = type;
    }

    public IPrinter.PaymentType getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(IPrinter.PaymentType paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public int getSpdnNumber() {
        return spdnNumber;
    }

    public void setSpdnNumber(int spdnNumber) {
        this.spdnNumber = spdnNumber;
    }

    public Date getPrintTime() {
        return printTime;
    }

    public void setPrintTime(Date printTime) {
        this.printTime = printTime;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> goods) {
        this.items = goods;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Check check = (Check) o;

        if (id != check.id) return false;
        if (spdnNumber != check.spdnNumber) return false;
        if (type != check.type) return false;
        if (paymentMethod != check.paymentMethod) return false;
        if (!printTime.equals(check.printTime)) return false;
        if (!items.equals(check.items)) return false;
        if (!total.equals(check.total)) return false;
        return payment.equals(check.payment);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + type.hashCode();
        result = 31 * result + paymentMethod.hashCode();
        result = 31 * result + spdnNumber;
        result = 31 * result + printTime.hashCode();
        result = 31 * result + items.hashCode();
        result = 31 * result + total.hashCode();
        result = 31 * result + payment.hashCode();
        return result;
    }
}
