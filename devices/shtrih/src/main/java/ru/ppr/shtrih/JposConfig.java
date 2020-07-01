package ru.ppr.shtrih;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.IOException;

import jpos.config.JposEntry;
import jpos.config.JposEntryRegistry;
import jpos.loader.JposServiceLoader;
import jpos.util.JposPropertiesConst;
import ru.ppr.utils.FileUtils2;

/**
 * Настройки драйвера.
 *
 * @author Aleksandr Brazhkin
 */
class JposConfig {

    private static final String XML_CONFIG_FILE_NAME = "jpos.xml";
    private static final String DEVICE_NAME = "ShtrihFptr";

    static void configure(Context context, File workingDir, String portName) throws IOException {

        File xmlConfigFile = new File(workingDir, XML_CONFIG_FILE_NAME);
        FileUtils2.copyFileFromAssets(context, XML_CONFIG_FILE_NAME, xmlConfigFile);

        Uri xmlConfigFileUri = Uri.fromFile(xmlConfigFile);

        System.setProperty(JposPropertiesConst.JPOS_POPULATOR_FILE_URL_PROP_NAME, xmlConfigFileUri.toString());
        System.setProperty(JposPropertiesConst.JPOS_REG_POPULATOR_CLASS_PROP_NAME, "jpos.config.simple.xml.SimpleXmlRegPopulator");

        JposEntryRegistry registry = JposServiceLoader.getManager().getEntryRegistry();

        if (registry.hasJposEntry(DEVICE_NAME)) {
            JposEntry jposEntry = registry.getJposEntry(DEVICE_NAME);
            if (jposEntry != null) {
                jposEntry.addProperty("portName", portName);
                jposEntry.modifyPropertyValue("portName", portName);
            }
        }
    }
}
