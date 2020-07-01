package ru.ppr.security.entity;

/**
 * Карта доступа.
 */
public class SecurityCard {

    private Integer id;
    private Integer userId;
    private String uidCard;
    private String serialNumber;
    private Long validFrom;
    private Long validTo;
    private String passwordHash;
    private String salt;
    private boolean isActive;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUidCard() {
        return uidCard;
    }

    public void setUidCard(String uidCard) {
        this.uidCard = uidCard;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Long getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Long validFrom) {
        this.validFrom = validFrom;
    }

    public Long getValidTo() {
        return validTo;
    }

    public void setValidTo(Long validTo) {
        this.validTo = validTo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
