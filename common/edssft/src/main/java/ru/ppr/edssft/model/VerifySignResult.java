package ru.ppr.edssft.model;

/**
 * Результат проверки подписи.
 *
 * @author Aleksandr Brazhkin
 */
public class VerifySignResult {
    /**
     * Флаг успешности выполнения операции
     */
    private boolean successful;
    /**
     * Описание результата
     */
    private String description;
    /**
     * Флаг валидности ЭЦП
     */
    private boolean signValid;

    public VerifySignResult() {

    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSignValid() {
        return signValid;
    }

    public void setSignValid(boolean signValid) {
        this.signValid = signValid;
    }

    @Override
    public String toString() {
        return "VerifySignResult{" +
                "successful=" + successful +
                ", description='" + description + '\'' +
                ", signValid=" + signValid +
                '}';
    }
}
