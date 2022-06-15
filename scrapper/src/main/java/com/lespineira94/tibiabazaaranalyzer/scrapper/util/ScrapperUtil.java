package com.lespineira94.tibiabazaaranalyzer.scrapper.util;

import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ScrapperUtil {

    public static boolean equalsWithAttribute(final HtmlElement htmlElement, final String attributeName, final String valueToCompare) {
        final String attributeValue = htmlElement.getAttribute(attributeName);
        return equalsAttributeValue(attributeValue, valueToCompare);
    }

    public static String getDivTextValueByClassName(final HtmlElement element, final String className) {
        String textValue = StringUtils.EMPTY;

        final HtmlElement div = element.getElementsByTagName("div").get(0);
        final boolean equalsAttributeValue = equalsAttributeValue("class", className);

        if (equalsAttributeValue) {
            textValue = div.getTextContent();
        }

        return textValue;
    }

    private static boolean equalsAttributeValue(final String attributeValue, final String valueToCompare) {
        return attributeValue != null && !attributeValue.isEmpty() && attributeValue.equalsIgnoreCase(valueToCompare);
    }


    public static List<String> getCharacterInfoStringListByXPathExpression(final HtmlPage htmlPage, final String xpathExpresion) {
        final List<String> stringInfoList = new ArrayList<>();

        final List<Object> xpatrh = htmlPage.getByXPath(xpathExpresion);
        xpatrh.forEach(auctionCharacterNameElement -> {
            if (auctionCharacterNameElement instanceof DomAttr) {
                final String domAttrText = ((DomAttr) auctionCharacterNameElement).getTextContent();
                stringInfoList.add(domAttrText);
            } else {
                final String domTextContent = ((DomText) auctionCharacterNameElement).getTextContent();
                stringInfoList.add(domTextContent);
            }

        });

        return stringInfoList;
    }
}
