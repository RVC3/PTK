package ru.ppr.cppk.utils;

import java.nio.ByteOrder;

import ru.ppr.utils.CommonUtils;

/**
 * Класс для проверки ролей для данного участка
 *
 * @author Artem U.
 */
public class RoleChecker {

    private static final int BYTE_IN_REGION = 2;
    private static final int BYTE_IN_ROLE = 2;
    private static final int BYTE_IN_BLOCK = 16;
    private static final int ROLE_COUNT_BLOCK = 12;
    private static final byte REGION_START_INDEX = 2;
    private static final int REGION_IN_ROLE = 7;

    /**
     * Получает ID роли
     *
     * @param roleArray расшифрованные, введеным пином, данные.
     * @return ID роли
     */
    public Integer getIdRole(byte[] roleArray, int currentRegionNumber) {

        byte[] tmpRoleDate = new byte[BYTE_IN_BLOCK];

        for (int i = 0; i < ROLE_COUNT_BLOCK; i++) {
            System.arraycopy(roleArray, BYTE_IN_BLOCK * i, tmpRoleDate, 0, BYTE_IN_BLOCK);
            Integer id = checkRegion(tmpRoleDate, currentRegionNumber);
            if (id != null) {
                return id;
            }
        }

        return null;
    }

    /**
     * Получает на вход одну из 12 ролей и сравнивает участки на котором действует данная роль с участком к которому привязан ПТК
     *
     * @param role одна из 12 ролей
     * @return ID роли, если она действует на данном участке
     */
    private Integer checkRegion(byte[] role, int currentRegionNumber) {

        byte[] idRoleArray = new byte[4];// инт 4 байта, поэтому длинна массива 4 байта
        System.arraycopy(role, 0, idRoleArray, 0, 2);
        int idRole = CommonUtils.convertByteToInt(idRoleArray, ByteOrder.LITTLE_ENDIAN);

        byte[] tmpArray = new byte[4];// инт 4 байта, поэтому длинна массива 4 байта
        for (int i = 0; i < REGION_IN_ROLE; i++) {
            System.arraycopy(role, REGION_START_INDEX + i * BYTE_IN_REGION, tmpArray, 0, BYTE_IN_REGION);
            int regionNumber = CommonUtils.convertByteToInt(tmpArray, ByteOrder.LITTLE_ENDIAN);
            if (regionNumber == currentRegionNumber) {
                return  idRole;
            }
        }
        return null;
    }
}
