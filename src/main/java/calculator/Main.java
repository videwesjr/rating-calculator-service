package calculator;

import calculator.config.DatabaseInitializer;
import calculator.service.RatingCalculatorService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.logging.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args) throws Exception {
        WireMockServer wireMockServer = new WireMockServer(3000);
        wireMockServer.start();

        WireMock.configureFor("localhost", 3000);
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/cpf_query"))
                .withQueryParam("cpf", WireMock.equalTo("94548325069"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                              "cpf": "94548325069",
                              "rating": "good"
                            }
                        """)));

        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")) {

            Logger liquibaseLogger = Logger.getLogger("liquibase");
            liquibaseLogger.setLevel(Level.OFF);
            DatabaseInitializer.runLiquibase(conn);

            String input = """
                {
                  "total_amount": 87.99,
                  "cpf": "94548325069",
                  "cc": {
                    "bin": "548110",
                    "last4": "2104"
                  }
                }
            """;

            RatingCalculatorService ratingCalculatorService = new RatingCalculatorService(conn);
            String result = ratingCalculatorService.calculateRating(input);
            System.out.println(result);
        } finally {
            wireMockServer.stop();
        }
    }
}
