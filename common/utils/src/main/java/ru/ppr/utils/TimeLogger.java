package ru.ppr.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.UUID;

import ru.ppr.utils.TimeLogger.LogEvent.EventType;

/**
 * Класс собирает информацию о времени выполнения операций
 *
 * @author G.Kashka
 */
public class TimeLogger {

    /**
     * смещение, которое накладывают методы данного класса
     */
    public static final int thisDept = 3;


    public static final boolean ENABLED = true;

    public static class LogEvent {

        public enum EventType {
            unknown,
            start_,
            single,
            finish
        }

        /**
         * id-события
         **/
        public UUID uuid = null;
        /**
         * любой текст, описания
         */
        public String descryption = null;
        /**
         * объект, содержащий названия класса и метода
         */
        public StackTraceElement stElement = null;
        /**
         * Тип события
         */
        public EventType type = EventType.unknown;
        /**
         * время фиксации события
         */
        public long time = 0;
        /**
         * уровень вложенности функций
         */
        public int level = 0;

        public String getSimpleClassName() {
            String s = stElement.getClassName();
            int n = s.lastIndexOf(".");
            return s.substring(n);
        }
    }

    private static volatile TimeLogger instance = null;

    /**
     * текущий лог
     */
    private ArrayList<LogEvent> log = new ArrayList<LogEvent>();

    /**
     * last UUID
     */
    public UUID uuid = null;

    public static TimeLogger getInstance() {
        TimeLogger localInstance = instance;
        if (localInstance == null) {
            synchronized (TimeLogger.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new TimeLogger();
                }
            }
        }
        return localInstance;
    }


    //******* Внешние методы ********//

    /**
     * добавит в лог событие начала операции
     */
    public static UUID addStartEvent() {
        return addStartEvent("");
    }

    /**
     * добавит в лог событие начала операции
     */
    public static UUID addStartEvent(String text) {
        if (!ENABLED)
            return null;
        LogEvent item = TimeLogger.getInstance().createLogEvent(text, LogEvent.EventType.start_);
        item.uuid = UUID.randomUUID();
        addEvent(item);
        return item.uuid;
    }

    /**
     * добавит в лог событие без начала и конца, просто событие с фиксацией времени инициации
     */
    public static UUID addSingleEvent(String text) {
        if (!ENABLED)
            return null;
        LogEvent item = TimeLogger.getInstance().createLogEvent(text, EventType.single);
        item.uuid = UUID.randomUUID();
        addEvent(item);
        return item.uuid;
    }

    /**
     * добавит в лог событие конца операции
     *
     * @params uuid - id-начала события
     */
    public static UUID addFinishEvent(UUID uuid, String text) {
        if (!ENABLED)
            return null;
        LogEvent finishEvent = TimeLogger.getInstance().createLogEvent(text, EventType.finish);
        finishEvent.uuid = uuid;
        addEvent(finishEvent);
        return finishEvent.uuid;
    }

    /**
     * очищает историю
     */
    public static void clearLogHistory() {
        TimeLogger.getInstance().log.clear();
    }

    /**
     * выводит в лог все события
     */
    public static void printAll() {
        TimeLogger tl = TimeLogger.getInstance();
        for (LogEvent event : tl.log) {
            tl.print(event);
        }
    }

    //*********************************//

    private static void addEvent(LogEvent newEvent) {
//		TimeLogger.getInstance().print(newEvent);
        TimeLogger.getInstance().log.add(newEvent);
    }

    /**
     * выводит в лог событие
     */
    private void print(LogEvent event) {
        StringBuilder sb = new StringBuilder("" + event.level);

        //добавим многоуровневое смещение
        for (int i = 0; i < event.level; i++) {
            sb.append("   ");
        }

        sb.append(event.type + " ");
        sb.append("(" + event.getSimpleClassName() + " ");
        sb.append(event.stElement.getMethodName() + ") ");
        if (event.type == EventType.single)
            sb.append(getUtcString(event.time) + " ");
        sb.append("\"" + event.descryption + "\" ");
        if (event.type == EventType.finish) {
            LogEvent startEvent = getEvent(event.uuid, EventType.start_);
            if (startEvent != null) {
                sb.append("time: " + getTimeString(event.time - startEvent.time));
            } else {
                sb.append("НЕ НАЙДЕНО СТАРТОВОЕ СОБЫТИЕ!!!");
            }
        }
//        Log.d(TimeLogger.class.getSimpleName(), sb.toString());
    }


    /**
     * Ищет в стеке последнее событие с таким id
     */
    public LogEvent getEvent(UUID uuid, EventType type) {
        int i = log.size() - 1;
        while (i >= 0) {
            LogEvent event = log.get(i);
            if (event.uuid == uuid && type == event.type) return event;
            i--;
        }
        return null;
    }

    public LogEvent createLogEvent(String text, EventType type) {
        LogEvent item = new LogEvent();
        StackTraceElement[] stes = Thread.currentThread().getStackTrace();
        item.stElement = stes[1 + thisDept];
        item.descryption = text;
        item.type = type;
        item.time = System.currentTimeMillis();
        item.level = 0;
        //вычисление уровня события
        if (log.size() > 0) {
            LogEvent lastEvent = log.get(log.size() - 1);
            item.level = lastEvent.level;
            if (lastEvent.type == EventType.start_ && item.type != EventType.finish)
                item.level = lastEvent.level + 1;
            else if (lastEvent.type != EventType.start_ && item.type == EventType.finish)
                item.level = lastEvent.level - 1;
        }
        return item;
    }

    private static String getTimeString(long time) {
        int s = (int) (time / 1000);
        int ms = (int) (time % 1000);
        String out = "";
        if (s > 0) out = s + " s ";
        if (ms > 0) out = out + ms + " ms";
        if (out.isEmpty()) out = "0 ms";
        return out;
    }

    /**
     * Возвращает время в UTC формате в виде строки. На вход принимает timestamp в миллисекундах
     *
     * @param datetimeInMilliseconds
     * @return
     */
    public static String getUtcString(long datetimeInMilliseconds) {
        return getSdfForUtc().format(datetimeInMilliseconds);
    }

    /**
     * Возвращает SimpleDataFormat для экспорта времени в UTC; pattern - yyyy-MM-dd'T'HH:mm:ss.SSS
     *
     * @return
     */
    private static SimpleDateFormat getSdfForUtc() {
        final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        final SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT);
        final TimeZone utc = TimeZone.getTimeZone("UTC");
        sdf.setTimeZone(utc);
        return sdf;
    }

}
