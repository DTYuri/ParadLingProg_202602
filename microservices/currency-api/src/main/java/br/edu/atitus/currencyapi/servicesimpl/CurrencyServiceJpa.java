package br.edu.atitus.currencyapi.servicesimpl;

import br.edu.atitus.currencyapi.clients.BcbQuoteResponse;
import br.edu.atitus.currencyapi.dtos.CurrencyResponse;
import br.edu.atitus.currencyapi.entities.CurrencyEntity;
import br.edu.atitus.currencyapi.repositories.CurrencyRepository;
import br.edu.atitus.currencyapi.services.CurrencyService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CurrencyServiceJpa implements CurrencyService {

    private final CurrencyRepository repository;
    private final BcbQuoteService bcbQuoteService;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${bcb.quote-date}")
    private String quoteDate;

    public CurrencyServiceJpa(
            CurrencyRepository repository,
            BcbQuoteService bcbQuoteService
    ) {
        this.repository = repository;
        this.bcbQuoteService = bcbQuoteService;
    }

    @Override
    public CurrencyResponse findBySourceCurrencyAndTargetCurrency(
            String sourceCurrency,
            String targetCurrency
    ) throws Exception {

        String source = sourceCurrency.trim().toUpperCase(Locale.ROOT);
        String target = targetCurrency.trim().toUpperCase(Locale.ROOT);

        String environment = "Currency API running in Port: " + serverPort;

        try {
            double rate = getBcbConversionRate(source, target);

            return new CurrencyResponse(
                    source,
                    target,
                    rate,
                    environment
            );
        } catch (Exception ex) {
            // Se o BCB falhar ou não houver cotação, usa o banco local.
        }

        CurrencyEntity currency = repository
                .findBySourceCurrencyAndTargetCurrency(source, target)
                .orElseThrow(() ->
                        new EntityNotFoundException("Cotação não encontrada")
                );

        return CurrencyResponse.fromEntity(currency, environment);
    }

    private double getBcbConversionRate(String source, String target) {
        if (source.equals(target)) {
            return 1.0;
        }

        double sourceInBrl = source.equals("BRL")
                ? 1.0
                : getBrlRate(source);

        double targetInBrl = target.equals("BRL")
                ? 1.0
                : getBrlRate(target);

        return sourceInBrl / targetInBrl;
    }

    private double getBrlRate(String currency) {
        BcbQuoteResponse response =
                bcbQuoteService.getDailyQuote(currency, quoteDate);

        if (response == null
                || response.value() == null
                || response.value().isEmpty()) {
            throw new IllegalStateException(
                    "Não existe cotação no BCB para " + currency
            );
        }

        BcbQuoteResponse.BcbQuote quote =
                response.value().get(response.value().size() - 1);

        if (quote.cotacaoVenda() == null || quote.cotacaoVenda() <= 0) {
            throw new IllegalStateException(
                    "Cotação de venda inválida para " + currency
            );
        }

        return quote.cotacaoVenda();
    }
}
