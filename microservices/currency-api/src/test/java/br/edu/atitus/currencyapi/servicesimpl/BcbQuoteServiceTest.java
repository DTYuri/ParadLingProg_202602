
package br.edu.atitus.currencyapi.servicesimpl;

import br.edu.atitus.currencyapi.clients.BcbClient;
import br.edu.atitus.currencyapi.clients.BcbQuoteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BcbQuoteServiceTest {

    private BcbClient bcbClient;
    private BcbQuoteService service;

    @BeforeEach
    void setUp() {
        bcbClient = mock(BcbClient.class);
        service = new BcbQuoteService(bcbClient);
    }

    @Test
    void deveRetornarCotacaoDoBancoCentral() {
        BcbQuoteResponse resposta = new BcbQuoteResponse(
                List.of(new BcbQuoteResponse.BcbQuote(
                        5.00, 5.0521, "2026-10-15 13:05:33"
                ))
        );

        when(bcbClient.getDailyQuote("'USD'", "'10-15-2026'", "json"))
                .thenReturn(resposta);

        BcbQuoteResponse resultado =
                service.getDailyQuote("USD", "10-15-2026");

        assertNotNull(resultado);
        assertEquals(1, resultado.value().size());
        assertEquals(
                5.0521,
                resultado.value().get(0).cotacaoVenda(),
                0.0001
        );

        verify(bcbClient, times(1))
                .getDailyQuote("'USD'", "'10-15-2026'", "json");
    }

    @Test
    void deveRetornarRespostaVaziaQuandoNaoHouverCotacao() {
        BcbQuoteResponse resposta =
                new BcbQuoteResponse(List.of());

        when(bcbClient.getDailyQuote("'ZZZ'", "'10-15-2026'", "json"))
                .thenReturn(resposta);

        BcbQuoteResponse resultado =
                service.getDailyQuote("ZZZ", "10-15-2026");

        assertNotNull(resultado);
        assertTrue(resultado.value().isEmpty());
    }

    @Test
    void devePropagarErroDoCliente() {
        when(bcbClient.getDailyQuote("'USD'", "'10-15-2026'", "json"))
                .thenThrow(new RuntimeException("Falha simulada"));

        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> service.getDailyQuote("USD", "10-15-2026")
        );

        assertEquals("Falha simulada", erro.getMessage());
    }
}
