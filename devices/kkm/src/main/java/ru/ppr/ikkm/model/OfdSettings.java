package ru.ppr.ikkm.model;

/**
 * Класс - хранилище, для настроек ОФД
 *
 * @author Grigoriy Kashka
 */
public class OfdSettings {

    /**
     * ip адрес ОФД
     */
    private String ip;
    /**
     * TCP порт для доступа в ОФД
     */
    private int port;
    /**
     * Таймаут опроса ОФД, с
     */
    private int timeout;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }


    @Override
    public String toString() {
        return "OfdSettings{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", timeout=" + timeout +
                '}';
    }
}
