package com.jhonelrios.insurance_quote;

import com.jhonelrios.insurance_quote.model.UsageType;
import com.jhonelrios.insurance_quote.model.VehicleData;
import com.jhonelrios.insurance_quote.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class IntegrationTest {
    @Autowired
    WebTestClient webClient;

    @Autowired
    JwtUtil jwtUtil;

    @Test
    void happyPathShouldWork() {
        String token = jwtUtil.generateToken("test-user");

        VehicleData data = new VehicleData("Toyota", "Yaris", 2020, UsageType.PERSONAL, 40);

        webClient.post()
                .uri("/api/quotes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(data)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.brand").isEqualTo("Toyota");
    }
}
