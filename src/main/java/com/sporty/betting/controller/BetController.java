package com.sporty.betting.controller;

import com.sporty.betting.model.dto.ApiResponse;
import com.sporty.betting.model.entity.Bet;
import com.sporty.betting.repository.BetRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bets")
@RequiredArgsConstructor
@Tag(name = "Bets", description = "Query bets and their settlement status")
public class BetController {

    private final BetRepository betRepository;

    @Operation(summary = "List all bets", description = "Returns every bet in the system with its current status")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Bet>>> getAllBets() {
        List<Bet> bets = betRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Bets retrieved successfully", bets));
    }

    @Operation(summary = "Get bet by ID", description = "Returns a single bet by its unique identifier")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bet found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bet not found")
    })
    @GetMapping("/{betId}")
    public ResponseEntity<ApiResponse<Bet>> getBetById(
            @Parameter(description = "Unique bet identifier", example = "BET-001")
            @PathVariable String betId) {
        return betRepository.findById(betId)
                .map(bet -> ResponseEntity.ok(ApiResponse.ok("Bet found", bet)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Bet not found: " + betId)));
    }
}
