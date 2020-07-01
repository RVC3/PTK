package ru.ppr.inpas.lib.command.custom;

import android.support.annotation.NonNull;

import ru.ppr.inpas.lib.protocol.SaPacket;

public interface ICustomCommand {

    /**
     * Метод для получения результата выполнения пользовательской команды.
     *
     * @return результат выполнения пользовательской команды.
     * @see SaPacket
     */
    @NonNull
    SaPacket getResult();

    /**
     * Метод для инициализации выполнения пользовательской команды.
     */
    void execute();

}