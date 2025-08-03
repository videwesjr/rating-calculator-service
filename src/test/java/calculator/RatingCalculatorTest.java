package calculator;

import calculator.model.*;
import calculator.service.RatingCalculatorService;
import calculator.config.DatabaseInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.*;

import java.sql.Connection;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class RatingCalculatorTest {

    private static WireMockServer wireMockServer;
    private static RatingCalculatorService calculator;
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void setupAll() throws Exception {
        wireMockServer = new WireMockServer(3000);
        wireMockServer.start();

        JdbcConnectionPool cp = JdbcConnectionPool.create("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        Connection connection = cp.getConnection();

        DatabaseInitializer.runLiquibase(connection);

        calculator = new RatingCalculatorService(connection);
    }

    @AfterAll
    static void teardownAll() {
        wireMockServer.stop();
    }

    @BeforeEach
    void resetMocks() {
        wireMockServer.resetAll();
    }

    @Test
    void shouldCalculateRating_whenCpfIsGoodAndCcHasAllStatuses() throws Exception {
        wireMockServer.stubFor(get(urlPathEqualTo("/cpf_query"))
                .withQueryParam("cpf", equalTo("94548325069"))
                .willReturn(okJson("""
                {
                    "cpf": "94548325069",
                    "rating": "good"
                }
            """)));

        var request = new TransactionRequest(
                87.99,
                "94548325069",
                new CreditCard("548110", "2104")
        );

        String json = mapper.writeValueAsString(request);

        String response = calculator.calculateRating(json);

        var features = mapper.readValue(response, TransactionFeatures.class);

        assertThat(features.TOTAL_AMOUNT()).isEqualTo(87.99);
        assertThat(features.CPF_RATING()).isEqualTo(0.0);
        assertThat(features.CC_SCORE()).isEqualTo(3.0);
    }

    @Test
    void shouldReturnNullCpfRating_whenRatingIsUnknown() throws Exception {
        wireMockServer.stubFor(get(urlPathEqualTo("/cpf_query"))
                .withQueryParam("cpf", equalTo("00000000000"))
                .willReturn(okJson("""
                {
                    "cpf": "00000000000",
                    "rating": "unknown"
                }
            """)));

        var request = new TransactionRequest(
                100.0,
                "00000000000",
                new CreditCard("548110", "2104")
        );

        String json = mapper.writeValueAsString(request);

        String response = calculator.calculateRating(json);

        var features = mapper.readValue(response, TransactionFeatures.class);

        assertThat(features.CPF_RATING()).isNull();
    }

    @Test
    void shouldReturnZeroScore_whenNoCcTransactions() throws Exception {
        wireMockServer.stubFor(get(urlPathEqualTo("/cpf_query"))
                .withQueryParam("cpf", equalTo("12345678900"))
                .willReturn(okJson("""
                {
                    "cpf": "12345678900",
                    "rating": "bad"
                }
            """)));

        var request = new TransactionRequest(
                50.0,
                "12345678900",
                new CreditCard("000000", "9999")
        );

        String json = mapper.writeValueAsString(request);

        String response = calculator.calculateRating(json);

        var features = mapper.readValue(response, TransactionFeatures.class);

        assertThat(features.CC_SCORE()).isEqualTo(0.0);
        assertThat(features.CPF_RATING()).isEqualTo(1.0);
    }
}
