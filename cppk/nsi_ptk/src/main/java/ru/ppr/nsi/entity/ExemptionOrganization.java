package ru.ppr.nsi.entity;

/**
 * Организация, выдавшая льготу.
 */
public class ExemptionOrganization {

    private String organizationName = null;
    private String organizationCode = null;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }
}
