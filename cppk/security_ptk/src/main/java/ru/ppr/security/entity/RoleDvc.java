package ru.ppr.security.entity;

/**
 * роль пользователя
 *
 * @author G.Kashka
 */
public class RoleDvc {

    /**
     * ID искусственно введенного на уровне ПО ПТК root пользователя
     */
    private static int rootRoleId = -100500;
    /**
     * ID искусственно введенного на уровне ПО ПТК root пользователя
     */
    private int id;
    private String name;
    private static String rootRoleName = "Root";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRoot() {
        return id==rootRoleId;
    }

    /**
     * Конструктор для Роли Root пользователя
     * @return
     */
    public static RoleDvc getRootRole() {
        RoleDvc roleDvc = new RoleDvc();
        roleDvc.setId(rootRoleId);
        roleDvc.setName(rootRoleName);
        return roleDvc;
    }

}