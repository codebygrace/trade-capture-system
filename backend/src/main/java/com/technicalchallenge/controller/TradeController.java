package com.technicalchallenge.controller;

import com.technicalchallenge.dto.DailySummaryDTO;
import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeFilterDTO;
import com.technicalchallenge.dto.TradeSummaryDTO;
import com.technicalchallenge.exception.TradeValidationException;
import com.technicalchallenge.exception.UserPrivilegeValidationException;
import com.technicalchallenge.mapper.TradeMapper;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.service.TradeService;
import com.technicalchallenge.service.TradeReportingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/trades")
@Validated
@Tag(name = "Trades", description = "Trade management operations including booking, searching, and lifecycle management")
public class TradeController {
    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);

    @Autowired
    private TradeService tradeService;
    @Autowired
    private TradeMapper tradeMapper;
    @Autowired
    TradeReportingService tradeReportingService;

    @GetMapping
    @Operation(summary = "Get all trades",
               description = "Retrieves a list of all trades in the system. Returns comprehensive trade information including legs and cashflows.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all trades",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<TradeDTO> getAllTrades() {
        logger.info("Fetching all trades");
        return tradeService.getAllTrades().stream()
                .map(tradeMapper::toDto)
                .toList();
    }

    // Handler for trade search by counterparty, book, trader, status, trade date ranges
    @Operation(summary = "Search trades",
            description = "Retrieves a list of all trades matching search criteria in the system. Returns comprehensive trade information including legs and cashflows.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved trades",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TradeDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public List<TradeDTO> getTradesBySearch(@RequestParam(required = false) String counterpartyName, @RequestParam(required = false) String bookName, @RequestParam(required = false) String trader, @RequestParam(required = false) String status, @RequestParam(required = false) LocalDate tradeDateStart, @RequestParam(required = false) LocalDate tradeDateEnd) {
        logger.info("Fetching trades matching query");
        return tradeService.getTradesByMultiCriteria(counterpartyName, bookName, trader,status, tradeDateStart,tradeDateEnd).stream()
                .map(tradeMapper::toDto)
                .toList();
    }

    // Handler for trade filter by counterparty, book, trader, status, trade date ranges
    @Operation(summary = "Filter trades",
            description = "Retrieves pages of all trades matching filter criteria in the system. Returns comprehensive trade information including legs and cashflows.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved trades",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TradeDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/filter")
    public Page<TradeDTO> getAllTradesByFilter(@ModelAttribute TradeFilterDTO tradeFilterDTO, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size ) {
        Pageable pageable = PageRequest.of(page, size);
        return tradeService.getAllTradesByFilter(tradeFilterDTO,pageable).map(tradeMapper::toDto);
    }

    @Operation(summary = "Query trades",
            description = "Retrieves pages of all trades matching query criteria in the system. Returns comprehensive trade information including legs and cashflows.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved trades",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TradeDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/rsql")
    public Page<TradeDTO> getTradesByRsqlQuery(@RequestParam(value = "query", required = false) String query, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size ) {
        Pageable pageable = PageRequest.of(page, size);
        return tradeService.getTradesByRsqlQuery(query,pageable).map(tradeMapper::toDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trade by ID",
               description = "Retrieves a specific trade by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade found and returned successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "404", description = "Trade not found"),
        @ApiResponse(responseCode = "400", description = "Invalid trade ID format")
    })
    public ResponseEntity<TradeDTO> getTradeById(
            @Parameter(description = "Unique identifier of the trade", required = true)
            @PathVariable(name = "id") Long id) {
        logger.debug("Fetching trade by id: {}", id);
        return tradeService.getTradeById(id)
                .map(tradeMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Handler for Trader's personal trades
    @GetMapping("/my-trades")
    @Operation(summary = "Get my trades",
            description = "Retrieves a list of all trades for current application user. Returns comprehensive trade information including legs and cashflows.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all trades for the user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TradeDTO.class))),
            @ApiResponse(responseCode = "401", description = "Login required to view trades"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges to view trades"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<TradeDTO>> getMyTrades(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching all trades for: {} ", userDetails.getUsername());
        List<Trade> trades = tradeReportingService.getTradesByTrader(userDetails);
        List<TradeDTO> responseDTO = trades.stream().map(tradeMapper::toDto).toList();
        return ResponseEntity.ok(responseDTO);
    }

    // Handler for Book-level trade aggregation
    @GetMapping("/book/{id}/trades")
    @Operation(summary = "Get trades by book",
            description = "Retrieves a list of all trades for book matching the ID provided. Returns comprehensive trade information including legs and cashflows.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all trades for the book",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TradeDTO.class))),
            @ApiResponse(responseCode = "401", description = "Login required to view trades"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges to view trades"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<TradeDTO>> getTradesByBook(@PathVariable(name = "id") Long id) {
        logger.info("Fetching all trades for book with ID: {} ", id);
        List<Trade> trades = tradeReportingService.getTradesByBookId(id);
        List<TradeDTO> responseDTO = trades.stream().map(tradeMapper::toDto).toList();
        return ResponseEntity.ok(responseDTO);
    }

    // Handler for trade portfolio summaries
    @GetMapping("/summary")
    @Operation(summary = "Get trade summary statistics",
            description = "Retrieves trade summary statistics for an authenticated user. This only displays statistical data for trades they own.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved trade summary statistics"),
            @ApiResponse(responseCode = "401", description = "Authentication required to view data"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges to view data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TradeSummaryDTO> getSummary(@AuthenticationPrincipal UserDetails userDetails) {

        TradeSummaryDTO tradeSummaryDTO = new TradeSummaryDTO();
        tradeSummaryDTO.setTotalTradesByStatus(tradeReportingService.totalTradesByStatus(userDetails));
        tradeSummaryDTO.setTotalNotionalByCurrency(tradeReportingService.totalNotionalAmountsByCurrency(userDetails));
        tradeSummaryDTO.setTradesByTypeByCounterparty(tradeReportingService.totalTradesByTradeTypeAndCounterparty(userDetails));
        return ResponseEntity.ok(tradeSummaryDTO);
    }

    // Handler for daily summary
    @GetMapping("/daily-summary")
    public ResponseEntity<DailySummaryDTO> getDailySummary(@AuthenticationPrincipal UserDetails userDetails) {
        DailySummaryDTO dailySummaryDTO = new DailySummaryDTO();
        dailySummaryDTO.setTradeCountToday(tradeReportingService.tradeCountForDate(userDetails, LocalDate.now()));
        dailySummaryDTO.setTradeCountYesterday(tradeReportingService.tradeCountForDate(userDetails, LocalDate.now().minusDays(1)));

        return ResponseEntity.ok(dailySummaryDTO);
    }

    @PostMapping
    @Operation(summary = "Create new trade",
               description = "Creates a new trade with the provided details. Automatically generates cashflows and validates business rules.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Trade created successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid trade data or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges to create a trade"),
        @ApiResponse(responseCode = "500", description = "Internal server error during trade creation")
    })
    public ResponseEntity<?> createTrade(
            @Parameter(description = "Trade details for creation", required = true)
            @Valid @RequestBody TradeDTO tradeDTO) {
        logger.info("Creating new trade: {}", tradeDTO);
        try {
            Trade trade = tradeMapper.toEntity(tradeDTO);
            tradeService.populateReferenceDataByName(trade, tradeDTO);
            Trade savedTrade = tradeService.saveTrade(trade, tradeDTO);
            TradeDTO responseDTO = tradeMapper.toDto(savedTrade);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (UserPrivilegeValidationException e) {
            logger.error("Insufficient user privileges", e);
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (TradeValidationException e) {
            logger.error("Error creating trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Invalid trade: " + e.getErrors());
        } catch (Exception e) {
            logger.error("Error creating trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating trade: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update existing trade",
               description = "Updates an existing trade with new information. Subject to business rule validation and user privileges.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade updated successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "404", description = "Trade not found"),
        @ApiResponse(responseCode = "400", description = "Invalid trade data or business rule violation"),
        @ApiResponse(responseCode = "403", description = "Insufficient privileges to update trade")
    })
    public ResponseEntity<?> updateTrade(
            @Parameter(description = "Unique identifier of the trade to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated trade details", required = true)
            @Valid @RequestBody TradeDTO tradeDTO) {
        logger.info("Updating trade with id: {}", id);
        try {
            if(!Objects.equals(id,tradeDTO.getTradeId())) {
                return  ResponseEntity.badRequest().body("Trade ID in path must match Trade ID in request body");
            }
            tradeDTO.setTradeId(id); // Ensure the ID matches
            Trade amendedTrade = tradeService.amendTrade(id, tradeDTO);
            TradeDTO responseDTO = tradeMapper.toDto(amendedTrade);
            return ResponseEntity.ok(responseDTO);
        } catch (UserPrivilegeValidationException e) {
            logger.error("Insufficient user privileges", e);
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating trade: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete trade",
               description = "Deletes an existing trade. This is a soft delete that changes the trade status.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Trade not found"),
        @ApiResponse(responseCode = "400", description = "Trade cannot be deleted in current status"),
        @ApiResponse(responseCode = "403", description = "Insufficient privileges to delete trade")
    })
    public ResponseEntity<?> deleteTrade(
            @Parameter(description = "Unique identifier of the trade to delete", required = true)
            @PathVariable Long id) {
        logger.info("Deleting trade with id: {}", id);
        try {
            tradeService.deleteTrade(id);
            return ResponseEntity.ok().body("Trade cancelled successfully");
        } catch (Exception e) {
            logger.error("Error deleting trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error deleting trade: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/terminate")
    @Operation(summary = "Terminate trade",
               description = "Terminates an existing trade before its natural maturity date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade terminated successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "404", description = "Trade not found"),
        @ApiResponse(responseCode = "400", description = "Trade cannot be terminated in current status"),
        @ApiResponse(responseCode = "403", description = "Insufficient privileges to terminate trade")
    })
    public ResponseEntity<?> terminateTrade(
            @Parameter(description = "Unique identifier of the trade to terminate", required = true)
            @PathVariable Long id) {
        logger.info("Terminating trade with id: {}", id);
        try {
            Trade terminatedTrade = tradeService.terminateTrade(id);
            TradeDTO responseDTO = tradeMapper.toDto(terminatedTrade);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            logger.error("Error terminating trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error terminating trade: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel trade",
               description = "Cancels an existing trade by changing its status to cancelled")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade cancelled successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "404", description = "Trade not found"),
        @ApiResponse(responseCode = "400", description = "Trade cannot be cancelled in current status"),
        @ApiResponse(responseCode = "403", description = "Insufficient privileges to cancel trade")
    })
    public ResponseEntity<?> cancelTrade(
            @Parameter(description = "Unique identifier of the trade to cancel", required = true)
            @PathVariable Long id) {
        logger.info("Cancelling trade with id: {}", id);
        try {
            Trade cancelledTrade = tradeService.cancelTrade(id);
            TradeDTO responseDTO = tradeMapper.toDto(cancelledTrade);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            logger.error("Error cancelling trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error cancelling trade: " + e.getMessage());
        }
    }
}
