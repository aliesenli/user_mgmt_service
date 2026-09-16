package com.example.jwt.domain.module;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class ModuleClientImpl implements ModuleClient {

    private static final Logger log = LoggerFactory.getLogger(ModuleClientImpl.class);

    private final RestClient restClient;

    public ModuleClientImpl(@Value("${module.service.url}") String baseUrl) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(factory)
            .build();
    }

    @Override
    @CircuitBreaker(name = "moduleService", fallbackMethod = "fallbackIsModuleAvailable")
    @Retry(name = "moduleService")
    public boolean isModuleAvailable(UUID moduleId) {
        try {
            restClient.get()
                .uri("/api/v1/modules/{id}", moduleId)
                .retrieve()
                .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw e;
        }
    }

    public boolean fallbackIsModuleAvailable(UUID moduleId, Throwable t) {
        log.warn("Module service unavailable for moduleId={}: {}", moduleId, t.getMessage());
        throw new ModuleServiceUnavailableException("Module service is currently unavailable");
    }
}
