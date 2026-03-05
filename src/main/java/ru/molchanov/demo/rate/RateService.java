package ru.molchanov.demo.rate;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RateService {

    private final RateRepository rateRepository;

    public RateService(RateRepository rateRepository) {
        this.rateRepository = rateRepository;
    }

    public Rate saveRate(Rate rate) {
        rate.setUpdatedAt(Instant.now());
        return rateRepository.save(rate);
    }

    public Optional<Rate> findByCurrencyCodeAndDate(String code, LocalDate rateDate) {
        return rateRepository.findByCurrencyCodeAndRateDate(code, rateDate);
    }

    public List<Rate> findAll() {
        return rateRepository.findAll();
    }

    public List<Rate> findByCodeSortedByUpdatedAtDesc(String code) {
        return rateRepository.findByCurrencyCodeIgnoreCaseOrderByUpdatedAtDesc(code);
    }

    // ✅ метод который предотвращает дубли
    public Rate saveOrUpdate(String code, LocalDate date, int nominal, BigDecimal value) {

        Rate rate = rateRepository
                .findByCurrencyCodeAndRateDate(code, date)
                .orElseGet(Rate::new);

        rate.setCurrencyCode(code);
        rate.setRateDate(date);
        rate.setNominal(nominal);
        rate.setValue(value);
        rate.setUpdatedAt(Instant.now());

        return rateRepository.save(rate);
    }
}