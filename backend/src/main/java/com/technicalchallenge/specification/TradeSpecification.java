package com.technicalchallenge.specification;

import com.technicalchallenge.dto.TradeFilterDTO;
import com.technicalchallenge.model.Trade;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Objects;

public class TradeSpecification {

    public static Specification<Trade> hasCounterpartyName(String providedCounterpartyName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("counterparty").get("name")), providedCounterpartyName.toLowerCase());
    }

    public static Specification<Trade> hasBookName(String providedBookName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("book").get("bookName")), providedBookName.toLowerCase());
    }

    public static Specification<Trade> hasTrader(String providedTrader) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("traderUser").get("firstName")), providedTrader.toLowerCase());
    }

    public static Specification<Trade> hasStatus(String providedStatus) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("tradeStatus").get("tradeStatus")), providedStatus.toLowerCase());
    }

    public static Specification<Trade> hasTradeDateBetween(LocalDate providedDateStart, LocalDate providedDateEnd) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("tradeDate"), providedDateStart, providedDateEnd);
    }

    public static Specification<Trade> getSpecification(TradeFilterDTO tradeFilterDTO) {
        Specification<Trade> spec = Specification.where(null);

        if (Objects.nonNull(tradeFilterDTO.getCounterpartyName())) {
            spec =  spec.and(hasCounterpartyName(tradeFilterDTO.getCounterpartyName()));
        }

        if (Objects.nonNull(tradeFilterDTO.getBookName())) {
            spec = spec.and(hasBookName(tradeFilterDTO.getBookName()));
        }

        if (Objects.nonNull(tradeFilterDTO.getTrader())) {
            spec = spec.and(hasTrader(tradeFilterDTO.getTrader()));
        }

        if(Objects.nonNull(tradeFilterDTO.getStatus())) {
            spec = spec.and(hasStatus(tradeFilterDTO.getStatus()));
        }

        if(Objects.nonNull(tradeFilterDTO.getTradeDateStart()) && Objects.nonNull(tradeFilterDTO.getTradeDateEnd())) {
            spec = spec.and(hasTradeDateBetween(tradeFilterDTO.getTradeDateStart(), tradeFilterDTO.getTradeDateEnd()));
        }

        return spec;
    }


}
