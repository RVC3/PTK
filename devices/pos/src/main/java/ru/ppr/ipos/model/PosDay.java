package ru.ppr.ipos.model;

/**
 * @author Dmitry Vinogradov
 */
public class PosDay {

    /**
     * Идентификатор дня банковского терминала.
     */
    private int id;
    /**
     * Признак закрытия дня.
     */
    private boolean closed;

    @Override
    public String toString() {
        return "PosDay{" +
                "id=" + id +
                ", closed=" + closed +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
