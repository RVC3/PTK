package ru.ppr.core.manager.eds;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * Фильтр файлов для экспорта из директории /transport/out.
 * http://agile.srvdev.ru/browse/CPPKPP-43478
 * Согласно данным мониторинга на ТСН от КО периодически прилетают запросы типа file_request (являющиеся спамом)
 * и в виду своего большого количества затормаживают обработку запросов на регистрацию ключей.
 *
 * @author Aleksandr Brazhkin
 */
public class TransportOutDirExportFileFilter implements FilenameFilter {

    //http://agile.srvdev.ru/browse/CPPKPP-43883
    private final Pattern pattern = Pattern.compile("\\d+_\\d+_\\d{17}_PublicKeyRegistrationRequest");

    @Override
    public boolean accept(File dir, String name) {
        return pattern.matcher(name).matches();
    }
}
