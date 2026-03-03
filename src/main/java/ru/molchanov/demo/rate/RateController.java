package ru.molchanov.demo.rate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rates")
public class RateController {

    private final RateService rateService;

    public RateController(RateService rateService) {
        this.rateService = rateService;
    }

    @PostMapping
    public Rate createRate(@RequestBody Rate rate) {
        return rateService.saveRate(rate);
    }

    @GetMapping
    public List<Rate> getAllRates() {
        return rateService.findAll();
    }
    @GetMapping("/by-date-currency")
    public ResponseEntity<Rate> getByDateAndCurrency(
            @RequestParam String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Optional<Rate> rate = rateService.findByCurrencyCodeAndDate(code, date);

        return rate
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/filter")
    public List<Rate> filter(@RequestParam String code) {
        return rateService.findByCodeSortedByUpdatedAtDesc(code);
    }




}