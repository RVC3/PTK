package ru.ppr.cppk.entity.settings;

import ru.ppr.security.entity.RoleDvc;

/**
 * Created by григорий on 12.07.2016.
 */
public class LocalUser {

    private String name = "";
    private RoleDvc role = null;
    private String login = "";
    private String cardUid = null;

    public LocalUser() {

    }

    public LocalUser(String name, RoleDvc role, String login, String cardUid) {
        this.name=name;
        this.role=role;
        this.login=login;
        this.cardUid=cardUid;
    }

    /**
     * Создаст Пользователя с рут правами
     * @return
     */
    public static LocalUser getRootUser() {
        return new LocalUser("Иванов И.И.", RoleDvc.getRootRole(), "root", null);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoleDvc getRole() {
        return role;
    }

    public void setRole(RoleDvc role) {
        this.role = role;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getCardUid() {
        return cardUid;
    }

    public void setCardUid(String cardUid) {
        this.cardUid = cardUid;
    }
}
