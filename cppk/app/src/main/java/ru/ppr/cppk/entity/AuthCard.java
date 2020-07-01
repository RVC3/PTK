package ru.ppr.cppk.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.logger.Logger;
import ru.ppr.cppk.utils.Aes;
import ru.ppr.utils.CommonUtils;

public class AuthCard implements Parcelable {

    private static final String TAG = "AuthCard";


    private byte[] _roleArray = new byte[192];
    private byte[] _fioArray = new byte[48];
    private byte[] _ecpArray = new byte[64];
    private byte[] _passwordArray = new byte[16];
    private byte[] _secureArray = new byte[16];
    private byte[] _loginAndDate = new byte[32];
    private BscInformation _bscInformation;
    //    private String _rfidAttr;
    private byte[] _cardUID;

    private byte[] idECP = new byte[4];


    public AuthCard(byte[] roleArray,
                    byte[] fioArray,
                    byte[] ecpArray,
                    byte[] passwordArray,
                    byte[] secureArray,
                    byte[] loginAndDate,
                    BscInformation bscInformation,
                    byte[] cardUID) {

        _roleArray = roleArray;
        _fioArray = fioArray;
        _ecpArray = ecpArray;
        _passwordArray = passwordArray;
        _loginAndDate = loginAndDate;
        _secureArray = secureArray;
        _bscInformation = bscInformation;
        _cardUID = cardUID;
        System.arraycopy(secureArray, 1, idECP, 0, 4);
    }

    public AuthCard(Parcel in) {

        in.readByteArray(_roleArray);
        in.readByteArray(_fioArray);
        in.readByteArray(_loginAndDate);
        in.readByteArray(_secureArray);
        in.readByteArray(_ecpArray);
        in.readByteArray(_passwordArray);
        in.readByteArray(idECP);
        int cardUIDLength = in.readInt();
        _cardUID = new byte[cardUIDLength];
        in.readByteArray(_cardUID);
        _bscInformation = in.readParcelable(BscInformation.class.getClassLoader());
        Logger.info(TAG, "Correct");
    }

    public static final Parcelable.Creator<AuthCard> CREATOR = new Parcelable.Creator<AuthCard>() {

        public AuthCard createFromParcel(Parcel in) {
            return new AuthCard(in);
        }

        public AuthCard[] newArray(int size) {
            return new AuthCard[size];
        }
    };

    public byte[] getCardUID() {
        return _cardUID;
    }

    public BscInformation getBscInformation() {
        return _bscInformation;
    }

    /**
     * Возвращает время записи карты
     *
     * @return
     */
    public long getWriteTime() {
        byte[] tmp = new byte[8];
        System.arraycopy(_secureArray, 5, tmp, 0, 4);
        return ByteBuffer.wrap(tmp).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    /**
     * Возвращает срок до которого действует карта
     *
     * @return
     */
    public Date getEndTime() {
        return _bscInformation.getValidityTime();
    }

    /**
     * Производит расшифровку данных, содержащих имя учетной записи и даты начала и конца действия ролей
     *
     * @param pin ключ, которым будет производится декодирование данных
     * @return
     */
    public byte[] getLoginAndTime(String pin) {
        return decodeData(_loginAndDate, pin);
    }

    /**
     * Производит расшифровку пароля
     *
     * @param pin
     * @return
     */
    public byte[] getPassword(String pin) {
        return decodeData(_passwordArray, pin);
    }

    /**
     * Производит расшифровку ролей
     *
     * @param pin
     * @return
     */
    public byte[] getRole(String pin) {
        return decodeData(_roleArray, pin);
    }

    /**
     * Производит расшифровку ФИО
     *
     * @param pin
     * @return
     */
    public String getFio(String pin) {
        byte[] fio = decodeData(_fioArray, pin);
        String fioString = null;
        if (fio != null)
            fioString = new String(fio);
        return fioString;
    }

    /**
     * Производит расшифровку ЭЦП
     *
     * @param pin
     * @return
     */
    public byte[] getEcp(String pin) {
        return decodeData(_ecpArray, pin);
    }

    /**
     * Производит расшифровку ФИО, возвращает массив байтов
     *
     * @param pin
     * @return
     */
    public byte[] getBytesFio(String pin) {
        return decodeData(_fioArray, pin);
    }

    /**
     * Возвращает byte[8]. Уникальный идентификатор кристалла (4 байт) + 4 байта нулей
     *
     * @return
     */
    public byte[] getCristallNumber() {
        byte[] out = new byte[8];
        try {
            byte[] cristallSerialNumber = _bscInformation.getCardUID();
            System.arraycopy(cristallSerialNumber, 0, out, 0, cristallSerialNumber.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Возвращает byte[8] внешнего номер карты.
     *
     * @return
     */
    public byte[] getOuterNumberBytes() {
        return _bscInformation.getOuterNumberBytes();
    }

    /**
     * Возвращает служебные данные
     *
     * @return
     */
    public byte[] getSecurityData() {
        return _secureArray;
    }

    /**
     * Возвращает ид ключа эцп
     *
     * @return
     */
    public byte[] getIdEcp() {
        return idECP;
    }

    private byte[] decodeData(byte[] data, String pin) {

        byte[] decodedData = new byte[data.length];

        try {
            decodedData = Aes.decrypt(data, pin);
        } catch (InvalidKeyException e) {
            Logger.info(TAG, "Error while decode data. " + e.getMessage());
            return null;
        } catch (NoSuchAlgorithmException e) {
            Logger.info(TAG, "Error while decode data. " + e.getMessage());
            return null;
        } catch (NoSuchPaddingException e) {
            Logger.info(TAG, "Error while decode data. " + e.getMessage());
            return null;
        } catch (InvalidAlgorithmParameterException e) {
            Logger.info(TAG, "Error while decode data. " + e.getMessage());
            return null;
        } catch (InvalidKeySpecException e) {
            Logger.info(TAG, "Error while decode data. " + e.getMessage());
            return null;
        } catch (InvalidParameterSpecException e) {
            Logger.info(TAG, "Error while decode data. " + e.getMessage());
            return null;
        } catch (IllegalBlockSizeException e) {
            Logger.info(TAG, "Error while decode data. " + e.getMessage());
            return null;
        } catch (BadPaddingException e) {
            Logger.info(TAG, "Error while decode data. " + e.getMessage());
            return null;
        }
        return decodedData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(_roleArray);
        dest.writeByteArray(_fioArray);
        dest.writeByteArray(_loginAndDate);
        dest.writeByteArray(_secureArray);
        dest.writeByteArray(_ecpArray);
        dest.writeByteArray(_passwordArray);
        dest.writeByteArray(idECP);
        int cardUIDLength = _cardUID == null ? 0 : _cardUID.length;
        dest.writeInt(cardUIDLength);
        dest.writeByteArray(_cardUID);
        dest.writeParcelable(_bscInformation, PARCELABLE_WRITE_RETURN_VALUE);
    }

    @Override
    public String toString() {
        return "AuthCard{" +
                "\n_roleArray=" + CommonUtils.bytesToHexWithSpaces(_roleArray) +
                "\n, _fioArray=" + CommonUtils.bytesToHexWithSpaces(_fioArray) +
                "\n, _ecpArray=" + CommonUtils.bytesToHexWithSpaces(_ecpArray) +
                "\n, _passwordArray=" + CommonUtils.bytesToHexWithSpaces(_passwordArray) +
                "\n, _secureArray=" + CommonUtils.bytesToHexWithSpaces(_secureArray) +
                "\n, _loginAndDate=" + CommonUtils.bytesToHexWithSpaces(_loginAndDate) +
                "\n, _bscInformation=" + _bscInformation +
                "\n, _cardUID='" + CommonUtils.bytesToHexWithSpaces(_cardUID) + '\'' +
                "\n, idECP=" + CommonUtils.bytesToHexWithSpaces(idECP) +
                "\n}";
    }
}
