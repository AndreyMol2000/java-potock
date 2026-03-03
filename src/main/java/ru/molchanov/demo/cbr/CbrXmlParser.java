package ru.molchanov.demo.cbr;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

public class CbrXmlParser {

    public ParsedRate parseOne(String xml, String targetCode) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
            );

            NodeList valutes = document.getElementsByTagName("Valute");

            for (int i = 0; i < valutes.getLength(); i++) {
                Element element = (Element) valutes.item(i);

                String code = element.getElementsByTagName("CharCode")
                        .item(0)
                        .getTextContent();

                if (code.equals(targetCode)) {

                    String nominalStr = element.getElementsByTagName("Nominal")
                            .item(0)
                            .getTextContent();

                    String valueStr = element.getElementsByTagName("Value")
                            .item(0)
                            .getTextContent()
                            .replace(",", ".");

                    int nominal = Integer.parseInt(nominalStr);
                    BigDecimal value = new BigDecimal(valueStr);

                    return new ParsedRate(code, nominal, value);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}