package ru.ppr.cppk.utils;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import ru.ppr.cppk.entity.settings.CommonSettings;

/**
 * Created by Александр on 08.06.2016.
 */
@RunWith(RobolectricTestRunner.class)
@SmallTest
public class CommonSettingsUtilsTest {

    @Test
    public void testDefault() {
        CommonSettings commonSettings = new CommonSettings();
        writeAndCompare(commonSettings);
    }

    @Test
    public void testAllowedStationsCodes() {
        CommonSettings commonSettings = new CommonSettings();
        commonSettings.setAllowedStationsCodes(new long[]{11, 23, 24});
        writeAndCompare(commonSettings);
    }

    private void writeAndCompare(CommonSettings commonSettingsOriginal) {
        File file = new File(getClass().getResource("").getFile(), "commonSettings.xml");
        try {
            CommonSettingsUtils.saveCommonSettingsToXmlFile(commonSettingsOriginal, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CommonSettings commonSettingsParsed = null;
        try {
            commonSettingsParsed = CommonSettingsUtils.loadCommonSettingsFromXmlFile(file);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(commonSettingsOriginal, commonSettingsParsed);
    }
}
