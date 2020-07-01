package ru.ppr.core.dataCarrier.smartCard.entity;

/**
 * Информация об авторизационной карте.
 *
 * @author Aleksandr Brazhkin
 */
public class AuthCardData {
    /**
     * Пароль доступа к КО. Закрыто пин-кодом.
     */
    private byte[] password;
    /**
     * ЭЦП. Закрыто пин-кодом.
     */
    private byte[] eds;
    /**
     * Фамилия и инициалы сотрудника. Закрыто пин-кодом.
     */
    private byte[] fio;
    /**
     * Имя учетной записи сотрудника в системе. Закрыто пин-кодом.
     */
    private byte[] login;
    /**
     * Дата и время начала и окончания действия для учетной записи на данной БСК. Закрыто пин-кодом.
     */
    private byte[] validityPeriod;
    /**
     * Права доступа на КО, до 12 ролей. Закрыто пин-кодом.
     */
    private byte[] roles;

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public byte[] getEds() {
        return eds;
    }

    public void setEds(byte[] eds) {
        this.eds = eds;
    }

    public byte[] getFio() {
        return fio;
    }

    public void setFio(byte[] fio) {
        this.fio = fio;
    }

    public byte[] getLogin() {
        return login;
    }

    public void setLogin(byte[] login) {
        this.login = login;
    }

    public byte[] getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(byte[] validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public byte[] getRoles() {
        return roles;
    }

    public void setRoles(byte[] roles) {
        this.roles = roles;
    }
}
