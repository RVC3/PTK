package ru.ppr.cppk.entity.event.model;

/**
 * Кассир
 */
public class Cashier {

    public static final String DEFAULT_OFFICIAL_CODE = "1";

    private long id;

    /**
     * имя учетной записи с карточки авторизации
     */
    private String login;
    /**
     * При открытии смены этот параметр = 1
     * При передачи смены этот параметр увеличивается на 1
     * Например Петров открывает смену - officialCode = 1
     * Петров передает смену Иванову - у Иванова officialCode = 2
     * и так далее пока кто-то не закроет смену
     */
    private String officialCode = DEFAULT_OFFICIAL_CODE;

    /**
     * ФИО считанное с авторизационной карты
     */
    private String fio;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public void setOfficialCode(String officialCode) {
        this.officialCode = officialCode;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cashier cashier = (Cashier) o;

        if (id != cashier.id) return false;
        if (login != null ? !login.equals(cashier.login) : cashier.login != null) return false;
        if (officialCode != null ? !officialCode.equals(cashier.officialCode) : cashier.officialCode != null)
            return false;
        return fio != null ? fio.equals(cashier.fio) : cashier.fio == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (login != null ? login.hashCode() : 0);
        result = 31 * result + (officialCode != null ? officialCode.hashCode() : 0);
        result = 31 * result + (fio != null ? fio.hashCode() : 0);
        return result;
    }
}
