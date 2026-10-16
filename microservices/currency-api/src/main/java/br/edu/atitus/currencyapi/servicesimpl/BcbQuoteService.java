package br.edu.atitus.currencyapi.servicesimpl;

import br.edu.atitus.currencyapi.clients.BcbClient;
import br.edu.atitus.currencyapi.clients.BcbQuoteResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class BcbQuoteService {

    private final BcbClient bcbClient;

    public BcbQuoteService(BcbClient bcbClient) {
        this.bcbClient = bcbClient;
    }

    @Cacheable(
            cacheNames = "bcb-cotations",
            key = "#currency + '-' + #date",
            unless = "#result == null || #result.value() == null || #result.value().isEmpty()"
    )
    @Retry(name = "bcb-api")
    @CircuitBreaker(name = "bcb-api")
    public BcbQuoteResponse getDailyQuote(String currency, String date) {
        return bcbClient.getDailyQuote(
                "'" + currency + "'",
                "'" + date + "'",
                "json"
        );
    }
}
