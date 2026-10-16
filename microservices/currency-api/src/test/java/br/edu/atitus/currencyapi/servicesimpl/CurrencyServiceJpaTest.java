
package br.edu.atitus.currencyapi.servicesimpl;

import br.edu.atitus.currencyapi.clients.BcbQuoteResponse;
import br.edu.atitus.currencyapi.dtos.CurrencyResponse;
import br.edu.atitus.currencyapi.entities.CurrencyEntity;
import br.edu.atitus.currencyapi.repositories.CurrencyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrencyServiceJpaTest {

    private CurrencyRepository repository;
    private BcbQuoteService bcbQuoteService;
    private CurrencyServiceJpa service;

    @BeforeEach
    void setUp() {
        repository = mock(CurrencyRepository.class);
        bcbQuoteService = mock(BcbQuoteService.class);

        service = new CurrencyServiceJpa(repository, bcbQuoteService);

        ReflectionTestUtils.setField(service, "serverPort", "8100");
        ReflectionTestUtils.setField(
                service, "quoteDate", "10-15-2026"
        );
    }

    @Test
    void deveRetornarCotacaoDoBancoCentral() {
        BcbQuoteResponse resposta = new BcbQuoteResponse(
                List.of(new BcbQuoteResponse.BcbQuote(
                        5.00, 5.0521, "2026-10-15 13:05:33"
                ))
        );

        when(bcbQuoteService.getDailyQuote("USD", "10-15-2026"))
                .thenReturn(resposta);

        CurrencyResponse resultado =
                assertDoesNotThrow(() ->
                        service.findBySourceCurrencyAndTargetCurrency(
                                "usd", "brl"
                        )
                );

        assertEquals("USD", resultado.sourceCurrency());
        assertEquals("BRL", resultado.targetCurrency());
        assertEquals(5.0521, resultado.conversionRate(), 0.0001);

        verify(repository, never())
                .findBySourceCurrencyAndTargetCurrency(anyString(), anyString());
    }

    @Test
    void deveUsarBancoLocalQuandoBancoCentralFalhar() throws Exception {
        when(bcbQuoteService.getDailyQuote("USD", "10-15-2026"))
                .thenThrow(new RuntimeException("Falha simulada no BCB"));

        when(repository.findBySourceCurrencyAndTargetCurrency("USD", "ARS"))
                .thenReturn(Optional.of(
                        new CurrencyEntity(1L, "USD", "ARS", 1510.45)
                ));

        CurrencyResponse resultado =
                service.findBySourceCurrencyAndTargetCurrency(
                        "usd", "ars"
                );

        assertEquals("USD", resultado.sourceCurrency());
        assertEquals("ARS", resultado.targetCurrency());
        assertEquals(1510.45, resultado.conversionRate(), 0.001);
    }

    @Test
    void deveLancarErroQuandoCotacaoNaoExistirEmNenhumaFonte()
            throws Exception {
        when(bcbQuoteService.getDailyQuote("ZZZ", "10-15-2026"))
                .thenThrow(new RuntimeException("Moeda não disponível"));

        when(repository.findBySourceCurrencyAndTargetCurrency("ZZZ", "BRL"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> service.findBySourceCurrencyAndTargetCurrency(
                        "zzz", "brl"
                )
        );
    }
}
