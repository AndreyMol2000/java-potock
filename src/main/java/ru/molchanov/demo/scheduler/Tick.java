package ru.molchanov.demo.scheduler;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.molchanov.demo.cbr.CbrXmlParser;
import ru.molchanov.demo.cbr.ParsedRate;
import ru.molchanov.demo.rate.RateService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class Tick {

    private final WebClient webClient;
    private final RateService rateService;
    private final ExecutorService rateExecutor;

    private final AtomicInteger savedCounter = new AtomicInteger(0);

    private final Counter parseSuccess;
    private final Counter parseError;
    private final Counter dbSaves;

    private final Timer parseTimer;
    private final Timer schedulerTimer;

    private final CbrXmlParser parser = new CbrXmlParser();
    private final Tracer tracer;

    public Tick(WebClient webClient,
                RateService rateService,
                ExecutorService rateExecutor,
                MeterRegistry registry,
                Tracer tracer) {

        this.webClient = webClient;
        this.rateService = rateService;
        this.rateExecutor = rateExecutor;
        this.tracer = tracer;

        this.parseSuccess = registry.counter("cbr_parse_success_total");
        this.parseError = registry.counter("cbr_parse_error_total");
        this.dbSaves = registry.counter("rates_saved_total");

        this.parseTimer = registry.timer("cbr_parse_time");
        this.schedulerTimer = registry.timer("scheduler_cycle_time");
    }

    @Scheduled(fixedDelay = 60000)
    public void rateScheduler() {
        schedulerTimer.record(this::runSchedulerOnce);
    }

    private void runSchedulerOnce() {
        savedCounter.set(0);


        String xml;
        Span downloadSpan = tracer.nextSpan().name("cbr.download_xml").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(downloadSpan)) {
            downloadSpan.tag("http.url", "https://www.cbr.ru/scripts/XML_daily.asp");

            xml = webClient.get()
                    .uri("https://www.cbr.ru/scripts/XML_daily.asp")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (xml == null || xml.isBlank()) {
                parseError.increment();
                downloadSpan.tag("result", "empty");
                System.out.println("XML не получен или пустой");
                return;
            }

            downloadSpan.tag("result", "ok");
        } catch (Exception e) {
            parseError.increment();
            downloadSpan.error(e);
            System.out.println("Ошибка загрузки XML: " + e.getMessage());
            return;
        } finally {
            downloadSpan.end();
        }


        Map<String, ParsedRate> parsedMap;
        Span parseSpan = tracer.nextSpan().name("cbr.parse_xml").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(parseSpan)) {
            parsedMap = parseTimer.record(() -> parser.parseAll(xml));
            parseSpan.tag("valutes.count", String.valueOf(parsedMap.size()));
        } catch (Exception e) {
            parseError.increment();
            parseSpan.error(e);
            System.out.println("Ошибка парсинга XML: " + e.getMessage());
            return;
        } finally {
            parseSpan.end();
        }

        LocalDate date = LocalDate.now();
        String[] codes = {"USD", "EUR", "GBP", "CNY", "JPY"};


        Span saveSpan = tracer.nextSpan().name("cbr.save_db").start();

        ArrayList<Future<?>> futures = new ArrayList<>();

        for (String code : codes) {
            futures.add(rateExecutor.submit(() -> {

                ParsedRate parsed = parsedMap.get(code);
                if (parsed == null) {
                    parseError.increment();
                    System.out.println(code + " не найден");
                    return;
                }


                Span oneSaveSpan = tracer.nextSpan(saveSpan).name("cbr.save_one").start();
                try (Tracer.SpanInScope ws = tracer.withSpan(oneSaveSpan)) {
                    oneSaveSpan.tag("currency", code);

                    rateService.saveOrUpdate(
                            parsed.getCode(),
                            date,
                            parsed.getNominal(),
                            parsed.getValue()
                    );

                    dbSaves.increment();
                    parseSuccess.increment();

                } catch (Exception e) {
                    parseError.increment();
                    oneSaveSpan.error(e);
                    System.out.println("Ошибка сохранения " + code + ": " + e.getMessage());
                } finally {
                    oneSaveSpan.end();
                }

                int current = savedCounter.incrementAndGet();
                System.out.println("Сохранено записей в этом запуске: " + current);
                System.out.println(Thread.currentThread().getName()
                        + ": " + code + " сохранён: " + parsed.getValue());
            }));
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                parseError.increment();
                saveSpan.error(e);
                System.out.println("Ошибка ожидания задачи: " + e.getMessage());
            }
        }

        saveSpan.end();
    }
}