package com.sporty.betting.controller;

import com.sporty.betting.model.entity.Bet;
import com.sporty.betting.model.enums.BetStatus;
import com.sporty.betting.repository.BetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BetController.class)
@Tag("unit")
class BetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BetRepository betRepository;

    @Test
    @DisplayName("GET /api/v1/bets — returns all bets")
    void getAllBets_returnsList() throws Exception {
        Bet bet = Bet.builder()
                .betId("BET-001").userId("USER-001").eventId("EVT-001")
                .eventMarketId("MKT-001").eventWinnerId("TEAM-A")
                .betAmount(new BigDecimal("50.00")).status(BetStatus.PENDING)
                .build();

        when(betRepository.findAll()).thenReturn(List.of(bet));

        mockMvc.perform(get("/api/v1/bets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].betId").value("BET-001"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/v1/bets — returns empty list when no bets")
    void getAllBets_empty() throws Exception {
        when(betRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/bets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v1/bets/{id} — found returns 200")
    void getBetById_found() throws Exception {
        Bet bet = Bet.builder()
                .betId("BET-001").userId("USER-001").eventId("EVT-001")
                .eventMarketId("MKT-001").eventWinnerId("TEAM-A")
                .betAmount(new BigDecimal("50.00")).status(BetStatus.WON)
                .build();

        when(betRepository.findById("BET-001")).thenReturn(Optional.of(bet));

        mockMvc.perform(get("/api/v1/bets/BET-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.betId").value("BET-001"))
                .andExpect(jsonPath("$.data.status").value("WON"));
    }

    @Test
    @DisplayName("GET /api/v1/bets/{id} — not found returns 404")
    void getBetById_notFound() throws Exception {
        when(betRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/bets/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bet not found: UNKNOWN"));
    }
}
