package ru.ppr.cppk.testUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Created by Артем on 11.01.2016.
 */
public class Utils {

    public static String getTextValue(Element element, String tagName){
        String textVal = null;
        NodeList nl = element.getElementsByTagName(tagName);
        if(nl != null && nl.getLength() > 0) {
            Element el = (Element)nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }

        return textVal;
    }

    public static int getIntValue(Element element, String tagName){
        return Integer.parseInt(getTextValue(element, tagName));
    }

    public static long getLongValue(Element element, String tagName){
        return Long.parseLong(getTextValue(element, tagName));
    }
}
