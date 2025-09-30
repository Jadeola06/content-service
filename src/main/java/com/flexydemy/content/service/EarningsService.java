package com.flexydemy.content.service;

import com.flexydemy.content.dto.MonthlyEarningDTO;
import com.flexydemy.content.dto.TutorDashboardDTO;
import com.flexydemy.content.dto.TutorEarningsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EarningsService {
    private final WebClient.Builder webClientBuilder;

    @Value("${external.payment-service.base-url}")
    private String paymentServiceUrl;

    public EarningsService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }


    public BigDecimal getTotalEarnings(String tutorId, String token) {
        return BigDecimal.valueOf(24.32);
//        try {
//            return webClientBuilder.build()
//                    .get()
//                    .uri(paymentServiceUrl + "/{tutorId}/total", tutorId)
//                    .headers(headers -> headers.setBearerAuth(token))
//                    .retrieve()
//                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
//                            clientResponse -> clientResponse.bodyToMono(String.class)
//                                    .flatMap(errorBody -> Mono.error(new RuntimeException("Call failed: " + errorBody))))
//                    .bodyToMono(BigDecimal.class)
//                    .doOnError(error -> {
//                        log.info("Call to get earnings failed: " + error.getMessage());
//                    })
//                    .block();
//        } catch (Exception e) {
//            log.error("Error during GET call to all earnings: {}", e.getMessage());
//            throw new RuntimeException("Error fetching total earnings: " + e.getMessage(), e);
//        }
    }

    public TutorEarningsDTO getEarningsBreakdown(String tutorId, String token) {
//        try {
//            return webClientBuilder.build()
//                    .get()
//                    .uri(paymentServiceUrl + "/{tutorId}/breakdown", tutorId)
//                    .headers(headers -> headers.setBearerAuth(token))
//                    .retrieve()
//                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
//                            clientResponse -> clientResponse.bodyToMono(String.class)
//                                    .flatMap(errorBody -> Mono.error(new RuntimeException("Call failed: " + errorBody))))
//                    .bodyToMono(TutorEarningsDTO.class)
//                    .doOnError(error -> {
//                        log.info("Call to get earnings failed: " + error.getMessage());
//                    })
//                    .block();
//        } catch (Exception e) {
//            log.error("Error during GET call to overall split earnings: {}", e.getMessage());
//            throw new RuntimeException("Error fetching total earnings: " + e.getMessage(), e);
//        }
        TutorEarningsDTO dto = new TutorEarningsDTO();
        dto.setPayoutEarnings(BigDecimal.valueOf(24598.01));
        dto.setSessionEarnings(BigDecimal.valueOf(99295.12));
        dto.setPayoutEarnings(BigDecimal.valueOf(124793.13));
        return dto;
    }

    public List<MonthlyEarningDTO> getMonthlyBreakdown(String tutorId, String token) {
//        try {
//            return webClientBuilder.build()
//                    .get()
//                    .uri(paymentServiceUrl + "/{tutorId}/monthly", tutorId)
//                    .headers(headers -> headers.setBearerAuth(token))
//                    .retrieve()
//                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
//                        clientResponse -> clientResponse.bodyToMono(String.class)
//                                .flatMap(errorBody -> Mono.error(new RuntimeException("Call failed: " + errorBody))))
//                    .bodyToFlux(MonthlyEarningDTO.class).collectList()
//                    .doOnError(error -> {
//                        log.info("Call to get earnings failed: " + error.getMessage());
//                    })
//                    .block();
//        } catch (Exception e) {
//            log.error("Error during GET call to monthly earnings: {}", e.getMessage());
//            throw new RuntimeException("Error fetching total earnings: " + e.getMessage(), e);
//        }
        //Mock for now
        List<MonthlyEarningDTO> earnings = new ArrayList<>();

        earnings.add(new MonthlyEarningDTO(1, "January", new BigDecimal("1200.50"), new BigDecimal("300.00")));
        earnings.add(new MonthlyEarningDTO(2, "February", new BigDecimal("950.75"), new BigDecimal("150.00")));
        earnings.add(new MonthlyEarningDTO(3, "March", new BigDecimal("1100.00"), new BigDecimal("200.00")));
        earnings.add(new MonthlyEarningDTO(4, "April", new BigDecimal("1050.25"), new BigDecimal("250.00")));
        earnings.add(new MonthlyEarningDTO(5, "May", new BigDecimal("1300.00"), new BigDecimal("400.00")));
        earnings.add(new MonthlyEarningDTO(6, "June", new BigDecimal("1250.00"), new BigDecimal("350.00")));

        return earnings;
    }
}
