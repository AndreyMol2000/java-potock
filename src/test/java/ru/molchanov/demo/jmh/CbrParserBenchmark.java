package ru.molchanov.demo.jmh;

import org.openjdk.jmh.annotations.*;
import org.w3c.dom.*;
import ru.molchanov.demo.cbr.ParsedRate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class CbrParserBenchmark {

    private String xml;

    @Setup(Level.Trial)
    public void setup() {
        xml = """
                <?xml version="1.0" encoding="windows-1251"?>
                <ValCurs Date="05.03.2026" name="Foreign Currency Market">
                  <Valute ID="R01235">
                    <CharCode>USD</CharCode>
                    <Nominal>1</Nominal>
                    <Value>90,1234</Value>
                  </Valute>
                  <Valute ID="R01239">
                    <CharCode>EUR</CharCode>
                    <Nominal>1</Nominal>
                    <Value>98,5678</Value>
                  </Valute>
                  <Valute ID="R01035">
                    <CharCode>GBP</CharCode>
                    <Nominal>1</Nominal>
                    <Value>115,1111</Value>
                  </Valute>
                  <Valute ID="R01375">
                    <CharCode>CNY</CharCode>
                    <Nominal>1</Nominal>
                    <Value>12,3456</Value>
                  </Valute>
                  <Valute ID="R01820">
                    <CharCode>JPY</CharCode>
                    <Nominal>100</Nominal>
                    <Value>60,2222</Value>
                  </Valute>
                </ValCurs>
                """;
    }

    private NodeList getValutes(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
        );
        return document.getElementsByTagName("Valute");
    }

    private ParsedRate parseValute(Element element) {
        String code = element.getElementsByTagName("CharCode").item(0).getTextContent();

        int nominal = Integer.parseInt(
                element.getElementsByTagName("Nominal").item(0).getTextContent()
        );

        String valueStr = element.getElementsByTagName("Value").item(0).getTextContent()
                .replace(",", ".");
        BigDecimal value = new BigDecimal(valueStr);

        return new ParsedRate(code, nominal, value);
    }

    // 1) for
    @Benchmark
    public Map<String, ParsedRate> parseAll_for() throws Exception {
        NodeList valutes = getValutes(xml);
        Map<String, ParsedRate> result = new HashMap<>();

        for (int i = 0; i < valutes.getLength(); i++) {
            Element element = (Element) valutes.item(i);
            ParsedRate rate = parseValute(element);
            result.put(rate.getCode(), rate);
        }

        return result;
    }

    // 2) stream
    @Benchmark
    public Map<String, ParsedRate> parseAll_stream() throws Exception {
        NodeList valutes = getValutes(xml);
        Map<String, ParsedRate> result = new HashMap<>();

        IntStream.range(0, valutes.getLength())
                .mapToObj(i -> (Element) valutes.item(i))
                .map(this::parseValute)
                .forEach(r -> result.put(r.getCode(), r));

        return result;
    }

    // 3) parallel stream
    @Benchmark
    public Map<String, ParsedRate> parseAll_parallel() throws Exception {
        NodeList valutes = getValutes(xml);

        return IntStream.range(0, valutes.getLength())
                .parallel()
                .mapToObj(i -> (Element) valutes.item(i))
                .map(this::parseValute)
                .collect(HashMap::new,
                        (m, r) -> m.put(r.getCode(), r),
                        Map::putAll);
    }
}