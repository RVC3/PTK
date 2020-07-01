package ru.ppr.edssft;

import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.edssft.model.GetKeyInfoResult;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.edssft.model.VerifySignResult;

/**
 * Контроллер ЭЦП.
 *
 * @author Aleksandr Brazhkin
 */
public interface EdsChecker {

    boolean open();


    boolean close();

    /**
     * Подписывает данные.
     *
     * @param data         Данные
     * @param signDateTime Время подписи
     * @return Результат подписи данных
     */
    @NonNull
    SignDataResult signData(byte[] data, Date signDateTime);

    /**
     * Получает информацию по номеру ключа подписи
     *
     * @param edsKeyNumber Номер ключа подписи
     * @return Информация по ключу подписи
     */
    @NonNull
    GetKeyInfoResult getKeyInfo(long edsKeyNumber);

    /**
     * Проверяет подпись
     *
     * @param data         Подписанные для проверки
     * @param signature    Подпись
     * @param edsKeyNumber Номер ключа ЭЦП
     * @return Результат проверки подписи
     */
    @NonNull
    VerifySignResult verifySign(byte[] data, byte[] signature, long edsKeyNumber);

}
