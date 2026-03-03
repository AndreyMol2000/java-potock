package ru.molchanov.demo.scheduler;

import ru.molchanov.demo.cbr.CbrXmlParser;
import ru.molchanov.demo.cbr.ParsedRate;
import ru.molchanov.demo.rate.RateService;
import ru.molchanov.demo.rate.Rate;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class Tick {
    private final WebClient webClient;
    private final RateService rateService;
    private final ExecutorService rateExecutor;
    private final AtomicInteger savedCounter = new AtomicInteger(0);

    public Tick(WebClient webClient,
                RateService rateService,
                ExecutorService rateExecutor) {

        this.webClient = webClient;
        this.rateService = rateService;
        this.rateExecutor = rateExecutor;
    }

    @Scheduled(fixedRate = 60000)
    public void RateScheduler() {
        savedCounter.set(0);
        String xml = webClient
                .get()
                .uri("https://www.cbr.ru/scripts/XML_daily.asp")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (xml == null) {
            System.out.println("XML не получен");
            return;
        }

        CbrXmlParser parser = new CbrXmlParser();

        String[] codes = {"USD", "EUR", "GBP", "CNY", "JPY"};

        for (String code : codes) {
            rateExecutor.submit(() -> {
                ParsedRate parsed = parser.parseOne(xml, code);

                if (parsed == null) {
                    System.out.println(code + " не найден");
                    return;
                }

                Rate rate = new Rate();
                rate.setCurrencyCode(parsed.getCode());
                rate.setNominal(parsed.getNominal());
                rate.setRateDate(LocalDate.now());
                rate.setValue(parsed.getValue());

                rateService.saveRate(rate);
                int current = savedCounter.incrementAndGet();
                System.out.println("Сохранено записей в этом запуске: " + current);

                System.out.println(Thread.currentThread().getName() + ": " + code + " сохранён: " + parsed.getValue());
            });
        }

    }}