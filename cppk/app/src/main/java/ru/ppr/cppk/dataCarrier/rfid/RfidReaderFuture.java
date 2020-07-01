package ru.ppr.cppk.dataCarrier.rfid;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.concurrent.Callable;

import ru.ppr.core.dataCarrier.findcardtask.FindCardTask;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.entity.BscInformation;
import ru.ppr.core.dataCarrier.findcardtask.FindCardTaskFactory;
import ru.ppr.cppk.legacy.BscReader;
import ru.ppr.cppk.logic.interactor.ReadLegacyBscInformationInteractor;

/**
 * Класс Future для поиска карты и создания экземпляра ридера для нее
 * Created by Артем on 15.03.2016.
 */
public class RfidReaderFuture implements Callable<Pair<BscReader, ru.ppr.cppk.dataCarrier.entity.BscInformation>> {

    FindCardTask findCardTask;

    /**
     * Создает Future для поиска карты и создания BscReader.
     * Future работает пока {@code Thread.currentThread.isInterrupted == true}
     * Для завершения работы по таймауту необходимо вызвать {@code Future#get(timeout)}
     *
     * @return {@code RfidReaderFuture<Pair<BscReader, BscInformation>>} -
     * если карта не была найдена то {@code BscReader == null},
     * если была ошибка чтения сервисных данных, то {@code BscInformation == null)
     **/
    @NonNull
    public static RfidReaderFuture createCallable(FindCardTaskFactory findCardTaskFactory) {
        return new RfidReaderFuture(findCardTaskFactory);
    }

    /**
     * @param findCardTaskFactory - фабрика {@link FindCardTask}
     */
    private RfidReaderFuture(FindCardTaskFactory findCardTaskFactory) {

        findCardTask = findCardTaskFactory.create();
    }

    /**
     * Прекратить работу
     */
    public void cancel() {
        if (findCardTask != null) {
            findCardTask.cancel();
        }
    }

    /**
     * Запускает поиск карты, пока кто-то не интеррапнет поток или не остановит работы установив флаг продолжения работы в false
     *
     * @return
     * @throws Exception
     * @see CardReader
     * @see BscInformation
     */
    @Override
    public Pair<BscReader, ru.ppr.cppk.dataCarrier.entity.BscInformation> call() throws Exception {

        CardReader cardReader = findCardTask.find();

        if (cardReader == null) {
            return new Pair<>(null, null);
        }

        ru.ppr.cppk.dataCarrier.entity.BscInformation legacyBscInformation = new ReadLegacyBscInformationInteractor().read(cardReader);
        BscReader bscReader = new BscReader(cardReader);

        return new Pair<>(bscReader, legacyBscInformation);
    }
}
