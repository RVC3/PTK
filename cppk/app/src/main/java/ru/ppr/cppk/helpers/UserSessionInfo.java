package ru.ppr.cppk.helpers;

import ru.ppr.cppk.entity.settings.LocalUser;

/**
 * In-memory хранилище информации о текущем авторизованном пользователе.
 *
 * @author Aleksandr Brazhkin
 */
public class UserSessionInfo {

    /**
     * Пин-код текущего пользователя
     */
    private String currentUserPin = null;
    /**
     * Текущий пользователь
     */
    private LocalUser currentUser;

    public UserSessionInfo() {
        // Для обратной совместимости
        currentUser = new LocalUser();
    }

    public LocalUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(LocalUser currentUser) {
        this.currentUser = currentUser;
    }

    public String getCurrentUserPin() {
        return currentUserPin;
    }

    public void setCurrentUserPin(String currentUserPin) {
        this.currentUserPin = currentUserPin;
    }
}
