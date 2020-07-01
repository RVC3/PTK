package ru.ppr.core.dataCarrier.findcardtask.authstrategy.statickey;

import android.support.annotation.IntRange;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.StaticKeyAccessRule;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Реализация алгоритма авторизации c использованием конкретных ключей.
 *
 * @author Aleksandr Brazhkin
 */
public class DefaultStaticKeyAuthorizationStrategy implements StaticKeyAuthorizationStrategy {

    //private static final byte[] KEY_READ_SECTOR_2 = {0x74, (byte) 0xf5, 0x19, (byte) 0x95, (byte) 0xba, 0x2a};
    //private static final byte[] KEY_READ_WRITE_ALL = {(byte) 0xA5, (byte) 0xA4, (byte) 0xA3, (byte) 0xA2, (byte) 0xA1, (byte) 0xA0};
    private static final String TAG = "StaticKeyAuthorization";


    private List<StaticKeyAccessRule> staticKeyAccess = null;
    private static final int maxStaticKeys = 25;
    private int index = 0;
    private boolean lastAuthSuccess = false;
    private int currentSectorNum;
    private boolean currentForRead;
    private StaticKeyAccessRule currentStaticKeyAccessRule;

    private final List<List<StaticKeyAccessRule>> read_keys;
    private final List<List<StaticKeyAccessRule>> write_keys;

    /* Перечисление двух типов ключей.
    Может быть:

    Типа А чтение
    Типа B запись
     */
    public enum KeyName {
        KEY_A(0xA),
        KEY_B(0xB);

        private final int id;
        KeyName(int value) { this.id = value; }
        public int getValue() { return id; }
    }

    /**
     * Представим есть документация с перечислением ключей (в Word, Excel и прочем виде)
     * По быстрому преобразовать из тесктового вида в местный можно
     * функцией по двум параметрам
     * @param k сам ключ в текстовом Hex формате
     * @param keyName одно из двух значений перечисления
     * @return на выходе имеем структуру с зашитым ключ-тип. Сектор, куда обращаться,
     * определяется позже
     */
    private StaticKeyAccessRule transformKey(String k, KeyName keyName){

        if (k==null || k.length() !=12)
            return null;

        Log.d(TAG, "transformKey() called with: k = [" + k + "], keyName = [" + keyName + "]");
        byte [] b = new byte[6];
        for (int i = 1; i <= 6; i++) {
            int index = k.length() - i*2;
            b[i - 1] = (byte)Integer.parseInt(k.substring(index, index + 2), 16);
        }
        StaticKeyAccessRule staticKeyAccessRule = new StaticKeyAccessRule();
        staticKeyAccessRule.setKey(b);
        staticKeyAccessRule.setKeyName(keyName.getValue());
        return staticKeyAccessRule;
    }


    /**
     *
     * Функция нужна для заполнения таблицы с ключами
     * На входе имеем ключ для доступа А, ключ для записи B
     * И сектор, куда по данным ключам будем ломиться
     * @param a ключ А чтение
     * @param b ключ B запись
     * @param index Номер сектора для авторизации ключём
     */
    private void addKeys(String a, String b, @IntRange(from=0, to=maxStaticKeys-1) int index){
        if (read_keys == null || write_keys == null) {
            Log.e(TAG, "addKeys: read_keys == null && write_keys == null");
            return;
        }

        read_keys.get(index).add(0, transformKey(a, KeyName.KEY_A));
        write_keys.get(index).add(0, transformKey(b, KeyName.KEY_B));

    }

    /**
     * Ключи для последних секторов, применяемых в БСК CPPK
     */
    public void addCPPKKeys(){
        Logger.debug(TAG, "add CPPK Keys");
        addKeys("FB1E9AE7F9AA","4B351439BA5C", 7);
        addKeys("FB1E9AE7F9AA","4B351439BA5C", 8);
        addKeys("FB1E9AE7F9AA","4B351439BA5C", 9);
    }

    /**
     * Ключи для последних секторов, применяемых в БСК тройка
     */
    public void addTroykaKeys(){
        Logger.debug(TAG, "add Troyka Keys");
        addKeys("2AA05ED1856F","EAAC88E5DC99", 15);
        //addKeys("403D706BA880","B39D19A280DF", 17);
        addKeys("69A32F1C2F19", "6B8BD9860763", 9);
        addKeys("2735FC181807","BF23A53C1F63", 1);
        addKeys("AE3D65A3DAD4","0F1C63013DBA", 7);
        addKeys("A73F5DC1D333","E35173494A81", 8);
    }


    /**
     * Последние ключи для различных вариантов карт СКМ
     */
    public void addSKM4KKeys(){
        Logger.debug(TAG, "add SKM4K Keys");
        addKeys("2ABA9519F574",	"CB9A1F2D7368",17);
        addKeys("84FD7F7A12B6",	"C7C0ADB3284F",18);

        addKeys("A1A2A3A4A5A6",	"B1B2B3B4B5B6",17);
        addKeys("A1A2A3A4A5A6",	"B1B2B3B4B5B6",18);

    }

    /**
     *  Для доступа к СКМ есть так называемый ключ по умолчанию.
     * Поэтому когда обращаемся в определённый сектор, есть вариант, что сработает
     * один из стандартных ключей
     */
    private void recreateDefaultKeys(){
        /// добавление универсальных ключей на весь диапазон
        for (int i = 0; i < maxStaticKeys; i++) {
            read_keys.add( new ArrayList<StaticKeyAccessRule>(){{
                add(transformKey("A0A1A2A3A4A5", KeyName.KEY_A));
            }});

            write_keys.add( new ArrayList<StaticKeyAccessRule>(){{
                add(transformKey("B0B1B2B3B4B5", KeyName.KEY_B));
            }});
        }
    }

    /**
     * Для каждой карты создаём таблицу с возможными ключами доступа к секторам
     */
    public DefaultStaticKeyAuthorizationStrategy() {
        read_keys = new ArrayList<>(maxStaticKeys);
        write_keys = new ArrayList<>(maxStaticKeys);

        recreateDefaultKeys();

        addKeys("359EE3161A08","BFE63D3E7336",2);
        addKeys("2ABA9519F574","CB9A1F2D7368", 2);
        addKeys("2AA05ED1856F","EAAC88E5DC99", 2);
        addKeys("2AA05ED1856F","EAAC88E5DC99", 6);
        addKeys("2AA05ED1856F","EAAC88E5DC99", 5);


        // распечатка в логе
        Logger.debug(TAG, "DefaultStaticKeyAuthorizationStrategy: KEYS A");
        for (List<StaticKeyAccessRule> l : read_keys){
            StringBuilder b = new StringBuilder();
            for (StaticKeyAccessRule k: l){
                b.append(Arrays.toString(k.getKey())).append("; ");
            }
            Log.d(TAG, b.toString());
        }

        Logger.debug(TAG, "DefaultStaticKeyAuthorizationStrategy: KEYS B");
        for (List<StaticKeyAccessRule> l : write_keys){
            StringBuilder b = new StringBuilder();
            for (StaticKeyAccessRule k: l){
                b.append(Arrays.toString(k.getKey())).append("; ");
            }
            Log.d(TAG, b.toString());
        }

    }


    @Override
    public StaticKeyAccessRule getKey(int sectorNum, boolean forRead) {

        Logger.debug(TAG, "getKey() called with: sectorNum = [" + sectorNum + "], forRead = [" +
                forRead + "] currentSectorNum=" +currentSectorNum + "; currentForRead==" + currentForRead);

        if (lastAuthSuccess) {
            if (sectorNum == currentSectorNum
                    && forRead == currentForRead) {

                // Текущий ключ удовлетворяет требованиям, вернем его же.
                return currentStaticKeyAccessRule;
            }
        }

        lastAuthSuccess = true;

        if (currentSectorNum != sectorNum || currentForRead != forRead) {
            // Условия изменилсь, нужно загрузить свежие данные.
            staticKeyAccess = getStaticKeySchemeRules(sectorNum, forRead);
            index = 0;
        }
        else{
            Logger.debug(TAG, "getKey: remove last key for sector " + sectorNum);
            staticKeyAccess.remove(currentStaticKeyAccessRule);
            index--;
        }

        currentSectorNum = sectorNum;
        currentForRead = forRead;

        if (index >= 0 && index < staticKeyAccess.size()) {
            currentStaticKeyAccessRule = staticKeyAccess.get(index);
            return currentStaticKeyAccessRule;
        }

        return null;
    }

    /**
     * Обратная связь для авторизации
     * Помечаем, сработал ли текущий ключ
     * при доступе к сектору
     *
     * @param success {@code true}, если авторизация прошла успешно, {@code false} иначе.
     */
    @Override
    public void setLastStaticKeyStatus(boolean success) {
        Log.d(TAG, "setLastStaticKeyStatus() called with: success = [" + success + "]");
        lastAuthSuccess = success;
        if (!success) {
            index++;
        }
    }

    /**
     * Функция получения списка возможных ключей доступа к карте.
     *
     * @param numSector - номер сектора на карте, обязательный
     * @param read      - флаг для чего нужны ключи для чтения или для записи. Обязательный.
     */
    private List<StaticKeyAccessRule> getStaticKeySchemeRules(int numSector, boolean read) {
        if (numSector < maxStaticKeys) {
            if (read)
                return read_keys.get(numSector);
            else
                return write_keys.get(numSector);
        }
        else
            return read_keys.get(0);
    }
}
