package io.payguard.apigateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.payguard.apigateway.filter.CorrelationId;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public Mono<Void> write(
            @NonNull ServerHttpResponse response,
            @NonNull ErrorResponse body
    ) {

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {

            byte[] json = objectMapper.writeValueAsBytes(body);

            return response.writeWith(
                    Mono.just(
                            response.bufferFactory().wrap(json)
                    )
            );

        } catch (JsonProcessingException ex) {

            return Mono.error(ex);

        }
    }

}