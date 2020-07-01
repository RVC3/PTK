package ru.ppr.nsi.entity;

/**
 * Группа льготы.
 */
public class ExemptionGroup {

    private String groupName = null;
    private int groupCode = -1;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(int groupCode) {
        this.groupCode = groupCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExemptionGroup that = (ExemptionGroup) o;

        return groupCode == that.groupCode;

    }

    @Override
    public int hashCode() {
        return groupCode;
    }
}
