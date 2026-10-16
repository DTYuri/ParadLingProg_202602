package br.edu.atitus.currencyapi.clients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BcbQuoteResponse(
        @JsonProperty("value")
        List<BcbQuote> value
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BcbQuote(
            Double cotacaoCompra,
            Double cotacaoVenda,
            String dataHoraCotacao
    ) {
    }
}
