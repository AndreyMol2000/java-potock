package ru.molchanov.demo.rate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RateRepository extends JpaRepository<Rate, Long> {

    Optional<Rate> findByCurrencyCodeAndRateDate(String currencyCode, LocalDate rateDate);

    List<Rate> findByCurrencyCodeIgnoreCaseOrderByUpdatedAtDesc(String currencyCode);
}