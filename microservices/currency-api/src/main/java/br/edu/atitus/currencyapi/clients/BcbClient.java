package br.edu.atitus.currencyapi.clients;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(
        url = "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata"
)
public interface BcbClient {

    @GetExchange("/CotacaoMoedaDia(moeda=@moeda,dataCotacao=@dataCotacao)")
    BcbQuoteResponse getDailyQuote(
            @RequestParam("@moeda") String currency,
            @RequestParam("@dataCotacao") String date,
            @RequestParam("$format") String format
    );
}
