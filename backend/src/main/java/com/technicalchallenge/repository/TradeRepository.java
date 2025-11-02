package com.technicalchallenge.repository;

import com.technicalchallenge.model.Trade;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long>, JpaSpecificationExecutor<Trade> {
    // Existing methods
    List<Trade> findByTradeId(Long tradeId);

    @Query("SELECT MAX(t.tradeId) FROM Trade t")
    Optional<Long> findMaxTradeId();

    @Query("SELECT MAX(t.version) FROM Trade t WHERE t.tradeId = :tradeId")
    Optional<Integer> findMaxVersionByTradeId(@Param("tradeId") Long tradeId);

    // NEW METHODS for service layer compatibility
    Optional<Trade> findByTradeIdAndActiveTrue(Long tradeId);

    List<Trade> findByActiveTrueOrderByTradeIdDesc();

    @Query("SELECT t FROM Trade t WHERE t.tradeId = :tradeId AND t.active = true ORDER BY t.version DESC")
    Optional<Trade> findLatestActiveVersionByTradeId(@Param("tradeId") Long tradeId);


    // Method for searching trades by counterparty, book, trader, status, date ranges
    @Query("SELECT t FROM Trade t " +
            "JOIN Counterparty c ON t.counterparty.id = c.id " +
            "JOIN Book b ON t.book.id = b.id " +
            "JOIN ApplicationUser a ON t.traderUser.id = a.id " +
            "JOIN TradeStatus s ON t.tradeStatus.id = s.id " +
            "AND (:counterpartyName IS NULL OR LOWER(c.name) = LOWER(:counterpartyName)) AND (:bookName IS NULL OR LOWER(b.bookName) = LOWER(:bookName)) " +
            "AND (:trader IS NULL OR LOWER(a.firstName) = LOWER(:trader)) AND (:status IS NULL OR LOWER(s.tradeStatus) = LOWER(:status)) " +
            "AND (:tradeDateStart IS NULL OR t.tradeDate >= :tradeDateStart) AND (:tradeDateEnd IS NULL OR t.tradeDate <= :tradeDateEnd)")
    List<Trade> findByMultiCriteria(@Param("counterpartyName") String counterpartyName,
                                    @Param("bookName") String bookName,
                                    @Param("trader") String trader,
                                    @Param("status") String status,
                                    @Param("tradeDateStart")LocalDate tradeDateStart,
                                    @Param("tradeDateEnd")LocalDate tradeDateEnd);

    // Method for searching trades by settlement instructions. LIKE is used to ensure partial matches are found
    @Query("SELECT t FROM Trade t " +
            "JOIN AdditionalInfo a ON t.id = a.trade.id " +
            "WHERE LOWER(a.fieldValue) LIKE CONCAT('%', LOWER(:instructions), '%')" +
            "AND a.fieldName = 'settlementInstructions' ")
    List<Trade> findByAdditionalInfosIsLikeIgnoreCase(@Param("instructions") String instructions);

}
